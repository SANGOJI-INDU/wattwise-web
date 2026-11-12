-- Optional: the app creates these tables automatically on startup.
CREATE TABLE IF NOT EXISTS appliances (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  power_watts DOUBLE NOT NULL,
  hours_per_day DOUBLE NOT NULL,
  quantity INT NOT NULL DEFAULT 1
);
CREATE TABLE IF NOT EXISTS calculation_history (
  id INT AUTO_INCREMENT PRIMARY KEY,
  calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  daily_energy_kwh DOUBLE NOT NULL,
  daily_cost DOUBLE NOT NULL,
  monthly_energy_kwh DOUBLE NOT NULL,
  monthly_cost DOUBLE NOT NULL,
  tariff_per_kwh DOUBLE NOT NULL
);
