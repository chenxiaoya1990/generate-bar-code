package com.barcode.redis.demo.service;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

@Service
public class BarcodeService {

	private final RedisTemplate<String, String> redisTemplate;
	private static final String BARCODE_PREFIX = "barcode:";
	private static final long EXPIRATION_SECONDS = 5 * 60; // 5 minutes
	private static final long BUFFER_SECONDS = 2 * 60; // 2 minutes

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
		   // Generate a random 24-digit barcode value
	    String barcodeValue = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 24);
	    String key = BARCODE_PREFIX + userId;

	    // Generate the barcode image in PNG format.
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    Code128Writer barcodeWriter = new Code128Writer();
	    BitMatrix bitMatrix = barcodeWriter.encode(barcodeValue, BarcodeFormat.CODE_128, 200, 80);

	    // Create a new BufferedImage with enough space to include the barcode and the generated number below it
	    BufferedImage barcodeImage = new BufferedImage(200, 120, BufferedImage.TYPE_INT_RGB);
	    Graphics2D graphics = barcodeImage.createGraphics();
	    graphics.setColor(java.awt.Color.WHITE);
	    graphics.fillRect(0, 0, 200, 120);

	    // Draw the barcode onto the new image
	    graphics.setColor(java.awt.Color.BLACK);
	    for (int i = 0; i < 200; i++) {
	        for (int j = 0; j < 80; j++) {
	            if (bitMatrix.get(i, j)) {
	                graphics.fillRect(i, j, 1, 1);
	            }
	        }
	    }

	    // Draw the generated barcode value below the barcode
	    Font font = new Font("Arial", Font.PLAIN, 14);
	    graphics.setFont(font);
	    graphics.drawString(barcodeValue, 10, 100); 

	    try {
	        // Write the image to the ByteArrayOutputStream in PNG format
	        ImageIO.write(barcodeImage, "png", outputStream);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    // Convert the barcode image to a Base64 encoded string
	    String barcodeBase64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());

	    // Link the barcode to the generated barcode value and set the expiration time for the barcode
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
	 * Updates the status of a barcode to "used" based on the provided userID's barcode value.
	 * If the barcode is expired, it will still be set to "used".
	 *
	 * @param barcode The  user's barcode value to update the status for.
	 */
	public void updateBarcodeStatus(String userId) {
		String key = BARCODE_PREFIX + userId;
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
	 * A barcode is considered valid if it is not expired and has not been marked as "used".
	 *
	 * @param userId The unique identifier of the user.
	 * @return True if the barcode is valid (not expired and not used), false otherwise.
	 */
	public boolean isBarcodeValid(String userId) {
	    String key = BARCODE_PREFIX + userId;
	    Long expiration = redisTemplate.getExpire(key, TimeUnit.SECONDS);
	    String status = redisTemplate.opsForValue().get(key);

	    // Check if the barcode is not expired and not marked as "used"
	    return expiration != null && expiration > 0 && !"used".equalsIgnoreCase(status);
	}

}
