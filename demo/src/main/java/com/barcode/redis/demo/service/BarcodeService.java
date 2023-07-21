package com.barcode.redis.demo.service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

@Service
public class BarcodeService {

	private final RedisTemplate<String, String> redisTemplate;
	private static final String BARCODE_PREFIX = "barcode:";
	private static final long EXPIRATION_SECONDS = 60; // 1 minutes
	private static final long BUFFER_SECONDS = 60; // 1 minute

	@Autowired
	public BarcodeService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * Generates a new barcode for the given userId and stores it in Redis.
	 *
	 * @param userId The unique identifier of the user.
	 * @return The generated barcode value.
	 */
	public String generateBarcode(String userId) {
		String barcodeValue = UUID.randomUUID().toString();
		String key = BARCODE_PREFIX + userId;

		// Generate the barcode image in PNG format.
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Code128Writer barcodeWriter = new Code128Writer();
		BitMatrix bitMatrix = barcodeWriter.encode(barcodeValue, BarcodeFormat.CODE_128, 200, 80);
		try {
			MatrixToImageWriter.writeToStream(bitMatrix, "png", outputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Convert the barcode image to a Base64 encoded string.
		String barcodeBase64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
		// Link the barcode to the userID and set the expiration time for the barcode.
		redisTemplate.opsForValue().set(key, barcodeBase64, EXPIRATION_SECONDS, TimeUnit.SECONDS);

		return barcodeValue;
	}

	/**
	 * Gets the barcode image (in Base64 format) associated with the given userId.
	 *
	 * @param userId The unique identifier of the user.
	 * @return The Base64 encoded barcode image.
	 */
	public String getBarcodeByUserId(String userId) {
		String key = BARCODE_PREFIX + userId;
		return redisTemplate.opsForValue().get(key);
	}

	/**
	 * Updates the status of a barcode to "used" based on the provided barcode value.
	 * If the barcode is expired, it will still be set to "used".
	 *
	 * @param barcode The barcode value to update the status for.
	 */
	public void updateBarcodeStatus(String barcode) {
		String key = BARCODE_PREFIX + barcode;
		String status = "used";
		Long expiration = redisTemplate.getExpire(key, TimeUnit.SECONDS);

		if (expiration != null && expiration > 0) {
			redisTemplate.opsForValue().set(key, status, expiration, TimeUnit.SECONDS);
		} else {
			// If the barcode is already expired, directly set it to "used".
			redisTemplate.opsForValue().set(key, status);
		}
	}

	/**
	 * Checks if the barcode associated with the given userId is still valid.
	 *
	 * @param userId The unique identifier of the user.
	 * @return True if the barcode is valid (not expired), false otherwise.
	 */
	public boolean isBarcodeValid(String userId) {
		String key = BARCODE_PREFIX + userId;
		Long expiration = redisTemplate.getExpire(key, TimeUnit.SECONDS);
		return expiration != null && expiration > 0;
	}
}
