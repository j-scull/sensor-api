
USE database;

CREATE TABLE IF NOT EXISTS sensor_info (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sensorId VARCHAR(36) NOT NULL,
    location VARCHAR(36) NOT NULL,
    creationTime DATETIME(3) NOT NULL
) AUTO_INCREMENT = 1;

CREATE TABLE IF NOT EXISTS temperature_and_humidity (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sensorId VARCHAR(36) NOT NULL,
    temperature int NOT NULL,
    humidity int NOT NULL,
    time DATETIME(3) NOT NULL
) AUTO_INCREMENT = 1;