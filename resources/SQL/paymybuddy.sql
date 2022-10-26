CREATE DATABASE paymybuddy; 
USE paymybuddy; 

CREATE TABLE registered( 
	email VARCHAR(320) NOT NULL, 
	password VARCHAR(25) NOT NULL, 
	first_name VARCHAR(30) NOT NULL, 
	last_name VARCHAR(30) NOT NULL, 
	birth_date DATE NOT NULL, 
	iban VARCHAR(34), 
	balance REAL, 
	PRIMARY KEY(email) 
); 

CREATE TABLE connection( 
	email_add VARCHAR(320) NOT NULL, 
	email_added VARCHAR(320) NOT NULL, 
	PRIMARY KEY(email_add, email_added), 
	FOREIGN KEY(email_add) REFERENCES registered(email), 
	FOREIGN KEY(email_added) REFERENCES registered(email) 
); 

CREATE TABLE transaction( 
	transaction_id BIGINT AUTO_INCREMENT, 
	date_time DATETIME NOT NULL, 
	amount DOUBLE NOT NULL,
	fee DOUBLE NOT NULL,
	email_sender VARCHAR(320), 
	email_receiver VARCHAR(320), 
	PRIMARY KEY(transaction_id), 
	FOREIGN KEY(email_sender) REFERENCES registered(email) ON DELETE SET NULL, 
	FOREIGN KEY(email_receiver ) REFERENCES registered(email) ON DELETE SET NULL 
); 
