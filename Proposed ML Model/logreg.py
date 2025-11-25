import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, confusion_matrix
from sklearn.preprocessing import StandardScaler

# Load simulation data
df = pd.read_csv("caregiver_sim_data.csv")  

# Define features and label
features = ['stressLevel', 'sleep_hours', 'workload', 'qoc', 'financialStrain']
X = df[features]
y = df['enteredSeekingSupportNext2Weeks']

# Scale features 
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# Split into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(X_scaled, y, test_size=0.2, random_state=42)

# Train the model
model = LogisticRegression()
model.fit(X_train, y_train)

# Evaluate
y_pred = model.predict(X_test)
print("Classification Report:\n", classification_report(y_test, y_pred))
print("Confusion Matrix:\n", confusion_matrix(y_test, y_pred))

# Predict on entire dataset
df['predicted_risk'] = model.predict_proba(X_scaled)[:, 1]  

# Save predictions to a CSV file
df[['caregiver_id', 'week', 'predicted_risk']].to_csv("ml_predictions.csv", index=False)
