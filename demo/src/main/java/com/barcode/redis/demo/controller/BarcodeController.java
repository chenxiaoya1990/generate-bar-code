package com.barcode.redis.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.barcode.redis.demo.service.BarcodeService;

@RestController
public class BarcodeController {

	private final BarcodeService barcodeService;

	@Autowired
	public BarcodeController(BarcodeService barcodeService) {
		this.barcodeService = barcodeService;
	}

	/**
	 * Generates a new barcode for the given userId and returns the generated barcode value.
	 *
	 * @param userId The unique identifier of the user.
	 * @return The generated barcode value.
	 */
	@GetMapping("/generate-barcode/{userId}")
	public String generateBarcode(@PathVariable String userId) {
		return barcodeService.generateBarcode(userId);
	}

	/**
	 * Retrieves the barcode image associated with the given userId in Base64 format.
	 *
	 * @param userId The unique identifier of the user.
	 * @return The Base64 encoded barcode image.
	 */
	@GetMapping("/get-barcode/{userId}")
	public String getBarcode(@PathVariable String userId) {
		String barcodeBase64 = barcodeService.getBarcodeByUserId(userId);
		return "<img src='data:image/png;base64," + barcodeBase64 + "'/>";
	}

	/**
	 * Updates the status of a barcode to "used" based on the provided userID's barcode value.
	 * If the barcode is expired, it will still be set to "used".
	 *
	 * @param barcode The barcode value to update the status for.
	 */
	@GetMapping("/update-barcode-status/{userId}")
	public void updateBarcodeStatus(@PathVariable String userId) {
		barcodeService.updateBarcodeStatus(userId);
	}

	/**
	 * Checks if the barcode associated with the given userId is still valid.
	 *
	 * @param userId The unique identifier of the user.
	 * @return True if the barcode is valid (not expired), false otherwise.
	 */
	@GetMapping("/is-barcode-valid/{userId}")
	public boolean isBarcodeValid(@PathVariable String userId) {
		return barcodeService.isBarcodeValid(userId);
	}
}
