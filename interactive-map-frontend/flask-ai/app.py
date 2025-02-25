import mysql.connector
import numpy as np
import tensorflow as tf
from flask import Flask, request, jsonify
from flask_cors import CORS
from datetime import date, timedelta, datetime
import requests
import smtplib
from email.mime.text import MIMEText

app = Flask(__name__)  # Define app first
CORS(app)  # Now CORS(app) works because app is defined

NOAA_API_BASE = "https://api.weather.gov/points/"
HEADERS = {"User-Agent": "InteractiveTreeMapApp (your@email.com)"}

custom_objects = {
    "mse": tf.keras.losses.MeanSquaredError()
}

def get_db_connection():
    """Establish a connection to the MySQL database"""
    return mysql.connector.connect(
        host="localhost",        
        user="admin_user",    
        password="secure_password123", 
        database="interactive_map"  
    )

# Load AI models
try:
    watering_model = tf.keras.models.load_model("water_schedule_model.h5", custom_objects=custom_objects)
    fertilization_model = tf.keras.models.load_model("fertilization_schedule_model.h5", custom_objects=custom_objects)
    print("✅ AI Models Loaded Successfully!")
except Exception as e:
    print(f"❌ Error loading AI models: {e}")
    watering_model = None
    fertilization_model = None

def get_soil_moisture(lat, lon):
    url = f"https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&hourly=soil_moisture_9_27cm"
    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()
        soil_values = data.get("hourly", {}).get("soil_moisture_9_27cm", [])
        if soil_values:
            return soil_values[0]  # Return the first available value
    # Fallback default if data is missing
    return 30.5/100

def get_humidity(lat, lon):
    url = f"https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&hourly=relativehumidity_2m"
    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()
        humidity_values = data.get("hourly", {}).get("relativehumidity_2m", [])
        if humidity_values:
            # Return the first available humidity value
            return float(humidity_values[0])
    # Fallback default humidity if no data is available
    return 50.0
def get_weather_by_location(lat, lon):
    """Fetch weather data from NOAA API using GPS coordinates."""
    try:
        api_url = f"https://api.weather.gov/points/{lat},{lon}"
        response = requests.get(api_url, headers=HEADERS)

        if response.status_code != 200:
            print(f"NOAA API Error: {response.status_code}, Response: {response.text}")
            return None, None, None

        data = response.json()
        forecast_url = data["properties"].get("forecast")

        if not forecast_url:
            print("Error: 'forecast' key missing in NOAA response")
            return None, None, None

        forecast_response = requests.get(forecast_url, headers=HEADERS)
        if forecast_response.status_code != 200:
            print(f"Forecast API Error: {forecast_response.status_code}, Response: {forecast_response.text}")
            return None, None, None

        forecast_data = forecast_response.json()
        periods = forecast_data.get("properties", {}).get("periods", [])

        if not periods:
            print("Error: No forecast periods found in response.")
            return None, None, None

        first_period = periods[0]

        temperature = first_period.get("temperature")
        
        humidity = get_humidity(lat, lon)

        temperature_val = temperature  # from the NOAA API
        if temperature_val is None:
            temperature = 20.0
        else:
            temperature = float(temperature_val)


        conditions = first_period.get("shortForecast")

        print(f"Extracted Weather Data: Temp={temperature}, Humidity={humidity}, Conditions={conditions}")
        return temperature, humidity, conditions

    except Exception as e:
        print(f"Weather API Error: {str(e)}")
        return None, None, None

def get_species_mapping():
    """Fetch all unique species from the database and assign numeric IDs."""
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT DISTINCT species FROM trees")
    species_list = cursor.fetchall()
    cursor.close()
    conn.close()
    
    return {species[0]: i for i, species in enumerate(species_list)}

def send_email_notification(email, subject, message):
    """Send an email notification."""
    sender_email = "your_email@gmail.com"
    sender_password = "your_email_password"

    msg = MIMEText(message)
    msg["Subject"] = subject
    msg["From"] = sender_email
    msg["To"] = email

    try:
        with smtplib.SMTP_SSL("smtp.gmail.com", 465) as server:
            server.login(sender_email, sender_password)
            server.sendmail(sender_email, email, msg.as_string())
        print(f"✅ Email sent to {email}")
    except Exception as e:
        print(f"❌ Failed to send email: {e}")


def predict_tree_height(planting_date, current_height, growth_rate=0.5):
    """
    Predicts the height of a tree based on its planting date, current height, and growth rate.
    
    If current_height is None, it defaults to 3.5 feet as the initial height.
    The formula is:
        Height = Initial Height + (Growth Rate × Age)
    where Age is the difference (in years) between today and the planting_date.
    
    planting_date can be a string in "YYYY-MM-DD" format or a datetime.date object.
    growth_rate is in feet per year (default 0.5).
    """
    # Use default initial height if current height is missing
    if current_height is None:
        initial_height = 3.5
    else:
        try:
            initial_height = float(current_height)
        except (ValueError, TypeError):
            initial_height = 3.5

    # Ensure planting_date is a date object
    if isinstance(planting_date, str):
        try:
            planting_date = datetime.strptime(planting_date, "%Y-%m-%d").date()
        except ValueError:
            print(f"Invalid planting_date format: {planting_date}. Using today as planting date.")
            planting_date = date.today()
    elif not isinstance(planting_date, date):
        # If planting_date is not a string or date, use today as a fallback
        planting_date = date.today()

    # Calculate the age in years (using 365.25 to account for leap years)
    age_years = (date.today() - planting_date).days / 365.25

    # Calculate predicted height
    predicted_height = initial_height + (growth_rate * age_years)
    return predicted_height


@app.route("/predict_tree_care", methods=["POST"])
def predict_tree_care():
    try:
        data = request.json
        tree_id = data.get("tree_id")

        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM trees WHERE id = %s", (tree_id,))
        tree = cursor.fetchone()
        
        # Fetch user email from database
        cursor.execute("SELECT email FROM users WHERE id = %s", (tree["owner_id"],))
        user = cursor.fetchone()
        user_email = user["email"] if user else None

        cursor.close()
        conn.close()

        if not tree:
            return jsonify({"error": "Tree not found in database"}), 404

        lat, lon = tree["latitude"], tree["longitude"]

        temperature, humidity, conditions = get_weather_by_location(lat, lon)

        soil_moisture_level = get_soil_moisture(lat, lon)
        
        
       # Fetch the current height and planting_date from the tree record.
        # Assume that the tree record has a column "planting_date" (as a string or datetime)
        # and "height" (which may be NULL).

        planting_date = tree.get("planting_date")
        current_height = tree.get("height")  # May be None

        # Predict the new height using a default growth rate (for example, 0.5 ft/year).
        predicted_height = predict_tree_height(planting_date, current_height, growth_rate=0.5)



        last_fertilization_date = tree.get("last_fertilization_date")
        last_watering_date = tree.get("last_watering_date")
        
        # Convert to date if they are datetime objects
        if last_fertilization_date and isinstance(last_fertilization_date, datetime):
            last_fertilization_date = last_fertilization_date.date()
        if last_watering_date and isinstance(last_watering_date, datetime):
            last_watering_date = last_watering_date.date()


        species_mapping = get_species_mapping()
        species_numeric = species_mapping.get(tree["species"], -1)


        fertilization_input = np.array([[species_numeric, predicted_height, temperature, humidity, soil_moisture_level]], dtype=np.float32)
        watering_input = np.array([[species_numeric, predicted_height, temperature, humidity, soil_moisture_level]], dtype=np.float32)


        predicted_fertilization_days = int(fertilization_model.predict(fertilization_input)[0][0])
        predicted_watering_days = int(watering_model.predict(watering_input)[0][0])
        today = datetime.today().date()

        next_fertilization_date = (
            last_fertilization_date + timedelta(days=predicted_fertilization_days)
            if last_fertilization_date 
            else (today + timedelta(days=predicted_fertilization_days))
        )
        days_remaining_fertilization = max((next_fertilization_date - today).days, 0)

        # Adjust watering schedule based on rain probability
        if "Rain" in conditions:
            precipitation_chance = 80  # Default if 'Rain' is detected in forecast
        else:
            precipitation_chance = 0  # No rain expected

        # Modify watering schedule dynamically based on rain conditions
        if precipitation_chance > 70 and soil_moisture_level > 50:  
            predicted_watering_days += 3  # Heavy rain → delay watering by 3 days
        elif precipitation_chance > 50 and soil_moisture_level > 40:  
            predicted_watering_days += 2  # Moderate rain → delay by 2 days
        elif precipitation_chance > 30 and soil_moisture_level < 30:  
            predicted_watering_days += 1  # Light rain, dry soil → slight delay

        # NEW: If the soil is very dry, reduce the watering interval (water sooner)
        # normalized_soil_moisture is soil_moisture / 100.0, so 0.20 corresponds to 20% moisture.
        if soil_moisture_level < 0.20:
            # Subtract, say, 5 days from the prediction, but don't let it drop below 1 day.
            predicted_watering_days = max(1, predicted_watering_days - 10)

        # Ensure the watering days stay within a safe range (for example, between 3 and 14)
        predicted_watering_days = max(1, min(predicted_watering_days, 7))

        next_watering_date = (
            last_watering_date + timedelta(days=predicted_watering_days)
            if last_watering_date 
            else (today + timedelta(days=predicted_watering_days))
        )
        print(f"🤖 AI Predicted Watering Date Before DB Update: {next_watering_date}")
        days_remaining_watering = max((next_watering_date - today).days, 0)
        print(f"🌱 Debugging Watering Update for Tree {tree_id}")
        print(f"   - Last Watered Date: {last_watering_date}")
        print(f"   - Predicted Watering Days: {predicted_watering_days}")
        print(f"   - AI-Calculated Next Watering Date: {next_watering_date}")
        print(f"   - Today's Date: {date.today()}")

        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)

        # Debug: Print the AI-predicted values before updating
        print(f"🔄 Attempting to update DB → Next Watering={next_watering_date}, Next Fertilization={next_fertilization_date}")

        # Store AI-predicted watering & fertilization dates in the database
        update_query = """
            UPDATE trees 
            SET next_watering_date = %s, 
                next_fertilization_date = %s,
                height = %s,
                soil_moisture_level = %s,
                temperature = %s,
                humidity = %s
            WHERE id = %s
        """
        cursor.execute(update_query, (next_watering_date, next_fertilization_date, predicted_height, soil_moisture_level, temperature, humidity,  tree_id))
        conn.commit()

        # Debug: Print number of rows affected
        print(f"✅ Rows affected: {cursor.rowcount}")

        # Fetch the updated values after saving
        cursor.execute("SELECT next_watering_date, next_fertilization_date FROM trees WHERE id = %s", (tree_id,))
        updated_tree = cursor.fetchone()

        cursor.close()
        conn.close()

        # Debug: Confirm if the update query worked
        if updated_tree:
            print(f"✅ Post-Update Fetch → Next Watering={updated_tree['next_watering_date']}, Next Fertilization={updated_tree['next_fertilization_date']}")

        warning_messages = []
        
        if days_remaining_fertilization == 0:
            warning_messages.append("⚠️ Fertilization is overdue! Apply fertilizer ASAP.")
        
        if days_remaining_watering == 0:
            warning_messages.append("⚠️ Watering is overdue! Water the plant now.")

        soil_quality_message = None
        if soil_moisture_level < .20:
            soil_quality_message = "⚠️ Soil is too dry! Consider watering soon."
        elif soil_moisture_level > .80:
            soil_quality_message = "⚠️ Soil is too wet! Avoid overwatering."
        
        if soil_quality_message:
            warning_messages.append(soil_quality_message)

        response = {
            "tree_id": tree_id,
            "temperature": temperature,
            "humidity": humidity,
            "conditions": conditions,
            "soil_moisture_level": soil_moisture_level,
            "predicted_next_fertilization_days": days_remaining_fertilization,
            "next_fertilization_date": next_fertilization_date.strftime("%Y-%m-%d"),
            "predicted_next_watering_days": days_remaining_watering,
            "next_watering_date": next_watering_date.strftime("%Y-%m-%d"),
            "notifications": warning_messages if warning_messages else None
        }

        if user_email and warning_messages:
            send_email_notification(user_email, "Tree Care Alert", "\n".join(warning_messages))

        return jsonify(response)

    except Exception as e:
        return jsonify({"error": "Internal Server Error", "details": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, ssl_context=("cert.pem", "key.pem"), debug=True)
