CREATE DATABASE IF NOT EXISTS attendance_system;

USE attendance_system;

CREATE TABLE IF NOT EXISTS employees (
    employee_number INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    rfid_tag VARCHAR(20) NOT NULL UNIQUE,
    photo_path VARCHAR(255) DEFAULT 'default_user.jpg'
);

INSERT INTO employees (name, rfid_tag, photo_path) VALUES
('Prince Pagaran', '3749745460', 'images/prince_pagaran.jpg'),
('John Lawrence Castillo', '4180459236', 'images/john_castillo.jpg'),
('Athien Capuli', '3749819732', 'images/athien_capuli.jpg'),
('Ryan Yamat', '3750353956', 'images/ryan_yamat.jpg'),
('Ferdinand Masi', '4180965348', 'images/ferdinand_masi.jpg');

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_number INT,
    date DATE,
    time_in TIME,
    time_out TIME,
    FOREIGN KEY (employee_number) REFERENCES employees(employee_number)
);

CREATE TABLE IF NOT EXISTS admin (
    username VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

INSERT INTO admin (username, password) VALUES
('admin', 'admin123');
