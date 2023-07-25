package com.barcode.redis.demo.service;

import java.sql.Timestamp;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.barcode.redis.demo.dto.BarcodeDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BarcodeService {

	private final RedisTemplate<String, String> redisTemplate;
	private final Random randomCode;
	private static final String BARCODE_PREFIX = "barcode:";
	private static final long EXPIRATION_SECONDS = 5 * 60; // 5 minute
	private static final long BUFFER_SECONDS = 2 * 60; // 2 minute

	public BarcodeService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.randomCode = new Random(); // Initialize randomCode in the constructor
	}

	/**
	 * Generates a new barcode for the given userId and stores DTO in Redis with an expiration time.
	 *
	 * @param userId The unique identifier of the user.
	 * @return The DTO class containing the userID, generated barcode value, physical/logical expiry, and status of the barcode.
	 */
	public BarcodeDTO generateBarcode(String userId) {
		
	    // Generate the barcode by combining the user's hash code with a random long value,
	    // and make it 24 characters long.
		 long currentTimestamp = System.currentTimeMillis();

		    String barcode = StringUtils.rightPad(
		            String.valueOf(userId.hashCode() & Integer.MAX_VALUE)
		                    + String.valueOf(randomCode.nextLong() & Long.MAX_VALUE)
		                    + String.valueOf(currentTimestamp),
		            24, "0").substring(0, 24);

		String key = BARCODE_PREFIX + barcode;

		// Calculate the total expiration time for physical expiration, including the buffer.
		long physicalExpirationTime = EXPIRATION_SECONDS + BUFFER_SECONDS;

		// Calculate the total logical expiration time, without the buffer.
		long logicalExpirationTime = EXPIRATION_SECONDS;

		// Link the barcode to the userID and set the expiration time for the barcode.
		redisTemplate.opsForValue().set(key, barcode, physicalExpirationTime, TimeUnit.SECONDS);

		// Calculate the expiration timestamps (current time + totalExpirationTime)
		long physicalExpiryMillis = System.currentTimeMillis() + (physicalExpirationTime * 1000);
		long logicalExpiryMillis = System.currentTimeMillis() + (logicalExpirationTime * 1000);

		// Create the BarcodeDTO instance with the barcode value and expiration timestamps
		BarcodeDTO barcodeDTO = new BarcodeDTO();
		barcodeDTO.setUserId(userId);
		barcodeDTO.setCode(barcode);
		barcodeDTO.setPhysicalExpireAt(new Timestamp(physicalExpiryMillis));
		barcodeDTO.setLogicalExpireAt(new Timestamp(logicalExpiryMillis));
		barcodeDTO.setStatus(true);

		// Convert BarcodeDTO to JSON string
		ObjectMapper objectMapper = new ObjectMapper();
		String barcodeDTOJson;
		try {
			barcodeDTOJson = objectMapper.writeValueAsString(barcodeDTO);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}

		// Save the entire BarcodeDTO JSON string to Redis
		String dtoKey = key + ":dto";
		redisTemplate.opsForValue().set(dtoKey, barcodeDTOJson, physicalExpirationTime, TimeUnit.SECONDS);

		return barcodeDTO;
	}

	/**
	 * Gets the barcode information associated with the given barcodeCode.
	 *
	 * @param code The unique identifier of the barcode.
	 * @return The BarcodeDTO object containing the barcode information.
	 */
	public BarcodeDTO getBarcodeByCode(String code) {
		String key = BARCODE_PREFIX + code;
		String barcodeValue = redisTemplate.opsForValue().get(key);

		if (barcodeValue != null) {
			// Get the entire BarcodeDTO JSON string from Redis
			String dtoKey = key + ":dto";
			String barcodeDTOJson = redisTemplate.opsForValue().get(dtoKey);

			// Convert JSON string back to BarcodeDTO object
			ObjectMapper objectMapper = new ObjectMapper();
			BarcodeDTO barcodeDTO;
			try {
				barcodeDTO = objectMapper.readValue(barcodeDTOJson, BarcodeDTO.class);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				return null;
			}

			// Set the current status from isBarcodeValid
			boolean currentStatus = isBarcodeValid(code);
			barcodeDTO.setStatus(currentStatus);
			// Set PhysicalExpireAt to null (as it is not necessary to be shown when get)
			barcodeDTO.setPhysicalExpireAt(null);
			return barcodeDTO;
		}

		return null; // Return null if the barcode is not found in Redis.
	}

	/**
	 * Marks a barcode as "used" based on the provided barcode value.
	 * If the barcode is expired, it will still be set to "used".
	 *
	 * @param code The unique identifier of the barcode.
	 */
	public void markBarcodeAsUsed(String code) {
		String key = BARCODE_PREFIX + code;
		String status = "used";
		Long expiration = redisTemplate.getExpire(key, TimeUnit.SECONDS);

		if (expiration != null && expiration > 0) {
			redisTemplate.opsForValue().set(key, status, expiration, TimeUnit.SECONDS);
		} else {
			// If the barcode is already expired or not found, directly set it to "used".
			redisTemplate.opsForValue().set(key, status);
		}
	}

	/**
	 * Checks if the barcode associated with the given barcodeCode is still valid.
	 * A barcode is considered valid if it has not expired and has not been marked as "used".
	 *
	 * @param code The unique identifier of the barcode.
	 * @return True if the barcode is valid (not expired and not marked as "used"), false otherwise.
	 */
	public boolean isBarcodeValid(String code) {
		String key = BARCODE_PREFIX + code;
		String status = redisTemplate.opsForValue().get(key);

		if (status != null && status.equals("used")) {
			// If the barcode is marked as "used", return false
			return false;
		} else {
			// Check if the barcode has not expired
			Long expiration = redisTemplate.getExpire(key, TimeUnit.SECONDS);
			return expiration != null && expiration > 0;
		}
	}

}
