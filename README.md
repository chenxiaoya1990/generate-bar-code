# generate-bar-code

## Project Title
RESTful API: Barcode Generator with Redis

## Description
This project is a barcode generator that allows users to generate barcodes and store them in Redis with an expiration time. 
It uses the Code 128 barcode format and provides a simple RESTful API to interact with barcode generation and status updates.

### Features
* Generate a new barcode for a given user ID and store it in Redis. The barcode will be automatically expired after 7 minutes or after being used.
* Retrieve the barcode.
* Update the status of a barcode to "used" based on the provided barcode value. Expired barcodes will also be set to "used".
* Check if a barcode associated with a user ID is still valid (not expired).

### Technologies Used
* Java Spring Boot for the backend application.
* Redis for storing and managing barcode data.

## Getting Started
To use this barcode generator, follow these steps:

* 1.Install and configure Redis on your local machine (https://redis.io/download/) or use AWS Elasticache Redis.
* 2.Clone this repository to your local machine and import it into your IDE（e.g. Eclipse）
* 3.Set up your Spring Boot project with Redis configuration in application.properties.
* 4.Build and run the Spring Boot application.


### API Endpoints
* POST /generate-barcode: Generates a new barcode for the given user ID and stores it in Redis. Returns the generated barcode value and expiration timestamp in JSON format.
  
* GET /get-barcode/{barcodeCode}: Retrieves the barcode image associated with the given barcode code in Base64 format.
  
* POST /update-barcode-status: Updates the status of a barcode to "used" based on the provided barcode code. If the barcode is expired, it will still be set to "used".
  
* GET /is-barcode-valid/{barcodeCode}: Checks if the barcode associated with the given barcode code is still valid (not expired). Returns true if the barcode is valid, false otherwise.

## Redis Memory Usage estimation Part

The purpose of this part is to estimate the required AWS Elasticache Redis capacity to handle the storage and retrieval of barcodes during a weekday's peak time.

1. The average size of the generated barcodes is as follows:

   - BarcodeDTO: around 170 bytes

2. Since the peak period processes around 400 requests per second, the number of requests per 7 minutes would be:

   400 requests/second * 60 seconds/minute * 7 minutes = 168,000 requests

3. Calculate the amount of data to be stored per hour:

   (168,000 requests) * (170 bytes/request) = 28,560,000 bytes = 27,890.625 KB = 27.237 GB

4. To conclude, it is recommended to set AWS Elasticache Redis with a memory of at least 27.3 GB to handle the storage and retrieval of barcodes during the peak time.
  
## Authors
xiaoya

