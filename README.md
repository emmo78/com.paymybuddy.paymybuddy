# com.paymybuddy.paymybuddy
The purpose of this application is to 

## Model Class Diagram UML

![UML Model Classes](./UML/payMyBuddyUML-ModelClasses.svg)

## MLD

![MLD](./UML/payMyBuddyUML-MLD.svg)

## SQL - MPD

CREATE DATABASE paymybuddy;  
USE paymybuddy;  

CREATE TABLE registered(  
   email VARCHAR(320) NOT NULL,  
   password CHAR(60) NOT NULL,  
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
   description VARCHAR(25),  
   email_sender VARCHAR(320),  
   email_receiver VARCHAR(320),  
   PRIMARY KEY(transaction_id),  
   FOREIGN KEY(email_sender) REFERENCES registered(email) ON DELETE SET NULL,  
   FOREIGN KEY(email_receiver ) REFERENCES registered(email) ON DELETE SET NULL  
);  

CREATE TABLE role(   
	role_id INT AUTO_INCREMENT,   
	role_name VARCHAR(10) NOT NULL,   
	PRIMARY KEY(role_id)   
);

CREATE TABLE registered_role(  
	granted_role INT NOT NULL,  
	email_role VARCHAR(320) NOT NULL,  
	PRIMARY KEY(granted_role, email_role),  
	FOREIGN KEY(granted_role) REFERENCES role(role_id),  
	FOREIGN KEY(email_role) REFERENCES registered(email) 
);

INSERT INTO role (role_name)  
VALUES ('ROLE_USER');  

--------------For Testing---------------

use paymybuddy;

INSERT INTO `paymybuddy`.`registered`
 (`email`, `password`, `first_name`, `last_name`, `birth_date`, `iban`, `balance`) 
VALUES 
 ('aaa@aaa.com', '$2y$10$rfi4JiEqe8hpCv1sTb4vzul5Vlj3XB81xD.pBZxPnTfF84e4C.4me', 'Aaa', 'AAA', 19910121, 'AA010123456789', 100), -- "aaaPasswd"
 ('bbb@bbb.com', '$2y$10$w8ep0Ezq5XziuiRR5rn79usHZo65HtWryR1bmzVOEWLnLndkMGz7m', 'Bbb', 'BBB', 19920222, 'BB020123456789', 200), -- "bbbPasswd"
 ('ccc@ccc.com', '$2y$10$EmIfscKBh0XzLvUaz3TngOb0bakl0qYA/cgRzgrN4qeFCZ9zd7DJu', 'Ccc', 'CCC', 19930323, 'CC030123456789', 300), -- "cccPasswd"
 ('ddd@ddd.com', '$2y$10$ZPDghQCwWC67GWlRn2Qdc.ct43NwK.tpNL1r1dcCl8t6Vqr.QpPQa', 'Ddd', 'DDD', 19940424, 'DD040123456789', 400), -- "dddPasswd"
 ('eee@eee.com', '$2y$10$J6D/NxgLZHcHP3Hzdt/ruO7lLz4iHZ9GsX7I7rJ2c1G1yzKxRGL6m', 'Eee', 'EEE', 19950525, 'EE050123456789', 500); -- "dddPasswd"

INSERT INTO `paymybuddy`.`registered_role`
 (`granted_role`, `email_role`)
VALUES
 ('1', 'aaa@aaa.com'),
 ('1', 'bbb@bbb.com'),
 ('1', 'ccc@ccc.com'),
 ('1', 'ddd@ddd.com'),
 ('1', 'eee@eee.com');

INSERT INTO `paymybuddy`.`connection`
 (`email_add`, `email_added`)
VALUES
 ('bbb@bbb.com', 'aaa@aaa.com'),
 ('ccc@ccc.com', 'aaa@aaa.com'),
 ('aaa@aaa.com', 'bbb@bbb.com'),
 ('ccc@ccc.com', 'bbb@bbb.com'),
 ('bbb@bbb.com', 'ccc@ccc.com'),
 ('ddd@ddd.com', 'ccc@ccc.com'),
 ('aaa@aaa.com', 'ddd@ddd.com');




--------------After Testing---------------
TRUNCATE `paymybuddy`.`connection`;
TRUNCATE `paymybuddy`.`registered_role`;
SET FOREIGN_KEY_CHECKS = 0; 
TRUNCATE `paymybuddy`.`registered`;
SET FOREIGN_KEY_CHECKS = 1;
TRUNCATE `paymybuddy`.`transaction`;
ALTER TABLE `paymybuddy`.`transaction` AUTO_INCREMENT = 1;


## Prerequisites

### What things you need to install the software

- Java 11
- apache Maven
- Spring Boot

### Properties : ./src/main/resources/application.properties :

does not contain SGBD login and passwd saved in "./db.properties" put in "./.gitignore" 

### Log4J2 : ./src/main/resources/log4j2-spring.xml
log file  = ./logs/PayMyBuddy-log4j2.log with RollBack

## Installing

A step by step series of examples that tell you how to get a development env running:

1.Install Java:

https://adoptium.net/temurin/releases?version=11

2.Install Maven:

https://maven.apache.org/install.html

3.Install Spring

https://spring.io/tools
or Eclipse Marketplace

## Testing

The app has unit tests and integration tests written.

To run the tests from maven, go to the folder that contains the pom.xml file and execute the below commands :

- $ mvn clean		→ clean ./target
- $ mvn test		→ run Unit Tests
- $ mvn verify		→ run Unit Tests, SIT and AIT
- $ mvn package		→ build .jar + Jacoco report in ./target/site/jacoco/index.html  
					(run : $ java -jar target/alerts-0.0.1-SNAPSHOT.jar)
- $ mvn site 		→ put project reports in ./target/site/index.html  
					( JavaDocs, SpotBugs, Surefire & Failsafe Reports, Jacoco & JaCoCo IT Reports)
- $ mvn surefire-report:report → surefire report in	./target/site/ surefire-report
- https://sonarcloud.io/summary/overall?id=com.paymybuddy.paymybuddy






