# generate-bar-code

## Project Title
RESTful API: Barcode Generator with Redis

## Description
This project is a barcode generator that allows users to generate barcodes and store them in Redis with an expiration time. 
It uses the Code 128 barcode format and provides a simple RESTful API to interact with the barcode generation and status update.

### Dependencies

* com.google.zxing

### Features
* Generate a new barcode for a given user ID and store it in Redis and the barcode will be expired after 7min or after being used.
* Retrieve the barcode image associated with a user ID in Base64 format.
* Update the status of a barcode to "used" based on the provided barcode value. Expired barcodes will also be set to "used".
* Check if a barcode associated with a user ID is still valid (not expired).

### Technologies Used
* Java Spring Boot for the backend application.
* Redis for storing and managing barcode data.
* Google ZXing library for generating Code 128 barcodes.

## Getting Started
To use this barcode generator, follow these steps:

* 1.Install and configure Redis on your local machine (https://redis.io/download/) or use AWS Elasticache Redis.
* 2.Clone this repository to your local machine and import it into your IDE（e.g. Eclipse）
* 3.Set up your Spring Boot project with Redis configuration in application.properties.
* 4.Build and run the Spring Boot application.


### API Endpoints
* GET /generate-barcode/{userId}: Generates a new barcode for the given user ID and stores it in Redis. Returns the generated barcode value.

* GET /get-barcode/{userId}: Retrieves the barcode image associated with the given user ID in Base64 format.

* GET /update-barcode-status/{userId}: Updates the status of a barcode to "used" based on the provided barcode value. If the barcode is expired, it will still be set to "used".

* GET /is-barcode-valid/{userId}: Checks if the barcode associated with the given user ID is still valid (not expired). Returns true if the barcode is valid, false otherwise.


## Authors
xiaoya

