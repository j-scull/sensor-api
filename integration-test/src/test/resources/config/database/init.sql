
USE database;

CREATE TABLE IF NOT EXISTS sensor_info (
    sensorId VARCHAR(36) NOT NULL PRIMARY KEY,
    location VARCHAR(36) NOT NULL,
    creationTime TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS temperature_and_humidity (
    sensorId VARCHAR(36) NOT NULL,
    temperature int NOT NULL,
    humidity int NOT NULL,
    time DATETIME NOT NULL,
    PRIMARY KEY (sensorId, time)
);