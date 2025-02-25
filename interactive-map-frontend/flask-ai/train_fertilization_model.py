import pandas as pd
import numpy as np
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, BatchNormalization, Dropout
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from tensorflow.keras.callbacks import EarlyStopping
import joblib

# Load dataset
df = pd.read_csv("fertilization_data.csv")

# Convert planting_date to tree age
df["planting_date"] = pd.to_datetime(df["planting_date"])
df["estimated_age"] = (pd.to_datetime("today") - df["planting_date"]).dt.days // 365

# Drop planting_date column (now we use estimated_age)
features = ['species_numeric', 'estimated_age', 'temperature', 'humidity', 'normalized_soil_moisture']
X = df[features]
y = df['days_until_next_fertilization']

# Normalize features using StandardScaler
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# Save the scaler for use during predictions
joblib.dump(scaler, "fertilization_scaler.pkl")

# Split dataset into training & testing sets
X_train, X_test, y_train, y_test = train_test_split(X_scaled, y, test_size=0.2, random_state=42)

# Define the Neural Network Model
model = Sequential([
    Dense(64, activation='relu', input_shape=(5,)),  # More neurons for deeper learning
    BatchNormalization(),
    Dense(32, activation='relu'),
    Dropout(0.2),
    Dense(16, activation='relu'),
    Dense(1, activation='linear')  # Output: predicted fertilization days
])

# Compile with lower learning rate for stable training
model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=0.0003),  # Lower learning rate
    loss='mse',  
    metrics=['mae']
)

# Early stopping to prevent overfitting
early_stop = EarlyStopping(monitor='val_loss', patience=10, restore_best_weights=True)

# Train the model with more epochs for better learning
model.fit(X_train, y_train, epochs=200, batch_size=16, validation_data=(X_test, y_test), callbacks=[early_stop])

# Save trained model
model.save("fertilization_schedule_model.h5")

print("✅ Model training complete and saved as 'fertilization_schedule_model.h5'")
