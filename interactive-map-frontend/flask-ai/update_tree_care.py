import mysql.connector
import requests
import numpy as np
import tensorflow as tf
from datetime import date, timedelta, datetime

# Connect to MySQL
db = mysql.connector.connect(
    host="localhost",
    user="admin_user",
    password="secure_password123",
    database="interactive_map"
)
cursor = db.cursor()

# Load AI Models
custom_objects = {"mse": tf.keras.losses.MeanSquaredError()}  # Register the custom loss function
watering_model = tf.keras.models.load_model("water_schedule_model.h5", custom_objects=custom_objects)
fertilization_model = tf.keras.models.load_model("fertilization_schedule_model.h5", custom_objects=custom_objects)


# Species Mapping for AI (Dynamically Fetched)
def get_species_mapping():
    """Fetch unique species from the database and assign numeric IDs."""
    cursor.execute("SELECT DISTINCT species FROM trees")
    species_list = cursor.fetchall()
    return {species[0]: i for i, species in enumerate(species_list)}

species_mapping = get_species_mapping()

# Function to Fetch Weather from NOAA API, including Rain Forecast
def get_weather_by_location(lat, lon):
    """Fetch weather data including precipitation from NOAA API using GPS coordinates."""
    try:
        headers = {"User-Agent": "InteractiveTreeMapApp (your@email.com)"}  
        api_url = f"https://api.weather.gov/points/{lat},{lon}"
        response = requests.get(api_url, headers=headers)

        if response.status_code != 200:
            print(f"NOAA API Error: {response.status_code}, Response: {response.text}")
            return None, None, None

        data = response.json()
        forecast_url = data["properties"].get("forecast")

        if not forecast_url:
            print("Error: 'forecast' key missing in NOAA response")
            return None, None, None

        # Fetch Forecast Data
        forecast_response = requests.get(forecast_url, headers=headers)
        if forecast_response.status_code != 200:
            print(f"Forecast API Error: {forecast_response.status_code}, Response: {forecast_response.text}")
            return None, None, None

        forecast_data = forecast_response.json()
        periods = forecast_data.get("properties", {}).get("periods", [])

        if not periods:
            print("Error: No forecast periods found in response.")
            return None, None, None

        first_period = periods[0]  # Get the first available forecast period

        temperature = first_period.get("temperature")
        humidity = first_period.get("relativeHumidity", {}).get("value", 50)  # Default humidity to 50%
        precipitation = first_period.get("probabilityOfPrecipitation", {}).get("value", 0)  # Default to 0%

        print(f"Extracted Weather Data: Temp={temperature}, Humidity={humidity}, Rain Chance={precipitation}%")
        return temperature, humidity, precipitation

    except Exception as e:
        print(f"Weather API Error: {str(e)}")
        return None, None, None

# Function to Calculate Tree Age
def calculate_tree_age(planting_date, height, species):
    """Determine tree age based on planting date if available, otherwise estimate from height."""
    if planting_date and planting_date != "NULL":  
        try:
            if isinstance(planting_date, str):  # Convert only if it's a string
                planting_date = datetime.strptime(planting_date, "%Y-%m-%d").date()
            age = (datetime.today().date() - planting_date).days 
        except ValueError:
            print(f"⚠ Invalid planting date format: {planting_date}")
            age = None
    else:
        age = None  

    if age is None:
        growth_rates = {"Mango": 0.5, "Avocado": 0.4, "Apple": 0.6, "Pine": 0.8, "Cherry": 0.5}
        age = round(height / growth_rates.get(species, 0.5))  

    return max(age, 1)  # Ensure at least 1 year old


# Fetch Trees from Database
cursor.execute("SELECT id, species, height, planting_date, latitude, longitude, soil_moisture_level FROM trees")
trees = cursor.fetchall()

for tree_id, species, height, planting_date, latitude, longitude, soil_moisture in trees:
    species_numeric = species_mapping.get(species, -1)  
    normalized_soil_moisture = soil_moisture / 100.0  

    # Fetch Weather Data from NOAA
    temperature, humidity, precipitation = get_weather_by_location(latitude, longitude)

    if temperature is None:
        temperature = 25  
    if humidity is None:
        humidity = 50  
    if precipitation is None:
        precipitation = 0  

    print(f"Processing Tree ID {tree_id}: Species={species}, Temp={temperature}, Humidity={humidity}, Soil Moisture={soil_moisture}, Rain Chance={precipitation}%")

    # Calculate tree age
    estimated_age = calculate_tree_age(planting_date, height, species)

    # Predict Watering Needs
    watering_features = np.array([[species_numeric, estimated_age, temperature, humidity, normalized_soil_moisture]])
    next_watering_days = int(watering_model.predict(watering_features)[0][0])

   # Adjust Watering Based on Rain Forecast
    if "next_watering_days" not in locals():
        next_watering_days = 0  # Ensure it's initialized

    if precipitation > 70:  
        next_watering_days += 3  # Heavy rain → delay watering by 3 days
    elif precipitation > 50:  
        next_watering_days += 2  # Moderate rain → delay watering by 2 days
    elif precipitation > 30:  
        next_watering_days += 1  # Light rain, but delay slightly

    # Ensure watering doesn't get too delayed
    next_watering_days = max(1, next_watering_days)
    next_watering = date.today() + timedelta(days=next_watering_days)
    print(f"🌧️ Adjusted Watering Days for Tree {tree_id}: {next_watering_days} (Rain Chance: {precipitation}%)")


    # Predict Fertilization Needs
    fertilization_features = np.array([[species_numeric, estimated_age, temperature, humidity, normalized_soil_moisture]])
    next_fertilization_days = int(fertilization_model.predict(fertilization_features)[0][0])

    # Adjust Fertilization Based on Soil Moisture
    if normalized_soil_moisture < 0.3:  
        next_fertilization_days -= 2  
    elif normalized_soil_moisture > 0.6:  
        next_fertilization_days += 2  

    next_fertilization_days = max(1, next_fertilization_days)
    next_fertilization = date.today() + timedelta(days=next_fertilization_days)

    print(f"🌿 Predicted Fertilization Days for Tree {tree_id}: {next_fertilization_days}")
    # Update Database
    cursor.execute("""
        UPDATE trees 
        SET next_watering_date = %s, next_fertilization_date = %s, temperature = %s, humidity = %s
        WHERE id = %s
    """, (next_watering, next_fertilization, temperature, humidity, tree_id))
    db.commit()

print("🌱 AI Predictions Updated Successfully for All Trees!")
db.close()
