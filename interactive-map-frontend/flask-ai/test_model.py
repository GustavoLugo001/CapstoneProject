import numpy as np
import tensorflow as tf
import joblib  # Import joblib to load the scaler

# Load the trained AI model
custom_objects = {
    "mse": tf.keras.losses.MeanSquaredError()
}
model = tf.keras.models.load_model("fertilization_schedule_model.h5", custom_objects=custom_objects)

# Load the saved scaler for feature scaling consistency
scaler = joblib.load("fertilization_scaler.pkl")

# Define test cases to analyze model predictions
test_cases = [
    {"name": "Original", "species": 0, "age": 5, "temp": 49, "humidity": 50, "moisture": 30.5 / 100},
    {"name": "Lower Temperature (35°C)", "species": 0, "age": 5, "temp": 35, "humidity": 50, "moisture": 30.5 / 100},
    {"name": "Higher Soil Moisture (0.6)", "species": 0, "age": 5, "temp": 49, "humidity": 50, "moisture": 0.6},
    {"name": "Younger Tree (Age 2)", "species": 0, "age": 2, "temp": 49, "humidity": 50, "moisture": 30.5 / 100}
]

# Run tests
for case in test_cases:
    # Prepare input for AI with correct scaling
    input_data = np.array([[case["species"], case["age"], case["temp"], case["humidity"], case["moisture"]]])

    # Scale the input using the saved scaler
    input_scaled = scaler.transform(input_data)

    # Get prediction from the AI model
    prediction = model.predict(input_scaled)

    # Print prediction result
    print(f"Test Case: {case['name']} -> Predicted Fertilization Days: {prediction[0][0]}")
