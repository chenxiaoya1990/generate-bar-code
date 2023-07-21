package com.barcode.redis.demo.service;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.barcode.redis.demo.dto.BarcodeDTO;

@Service
public class BarcodeService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BARCODE_PREFIX = "barcode:";
    private static final long EXPIRATION_SECONDS = 5 * 60; // 5 minutes
    private static final long BUFFER_SECONDS = 2 * 60; // 2 minutes

    public BarcodeService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Generates a new barcode for the given userId and stores it in Redis with an expiration time.
     * The expiration time includes the buffer.
     *
     * @param userId The unique identifier of the user.
     * @return The DTO class containing the generated barcode value and expiration timestamp.
     */
    public BarcodeDTO generateBarcode(String userId) {
        String barcode = UUID.randomUUID().toString();
        String key = BARCODE_PREFIX + barcode;
  
        // Calculate the total expiration time, including the buffer.
        long totalExpirationTime = EXPIRATION_SECONDS + BUFFER_SECONDS;

        // Link the barcode to the userID and set the expiration time for the barcode.
        redisTemplate.opsForValue().set(key, barcode, totalExpirationTime, TimeUnit.SECONDS);

        // Calculate the expiration timestamp (current time + totalExpirationTime)
        long expireAtMillis = System.currentTimeMillis() + (totalExpirationTime * 1000);
        Timestamp expireAtTimestamp = new Timestamp(expireAtMillis);

        // Create the BarcodeDTO instance with the barcode value and expiration timestamp
        BarcodeDTO barcodeDTO = new BarcodeDTO();
        barcodeDTO.setCode(barcode);
        barcodeDTO.setExpireAt(expireAtTimestamp);

        return barcodeDTO;
    }

    /**
     * Gets the barcode value associated with the given barcodeCode.
     *
     * @param code The unique identifier of the barcode.
     * @return The barcode value.
     */
    public String getBarcodeByCode(String code) {
        String key = BARCODE_PREFIX + code;
        return redisTemplate.opsForValue().get(key);
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
