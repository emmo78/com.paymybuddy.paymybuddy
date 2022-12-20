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






