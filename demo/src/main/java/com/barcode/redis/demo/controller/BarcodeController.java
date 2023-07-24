package com.barcode.redis.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.barcode.redis.demo.dto.BarcodeDTO;
import com.barcode.redis.demo.service.BarcodeService;

@RestController
public class BarcodeController {

	private final BarcodeService barcodeService;

	@Autowired
	public BarcodeController(BarcodeService barcodeService) {
		this.barcodeService = barcodeService;
	}

	/**
	 * Generates a new barcode for the given userId and returns the generated BarcodeDTO containing the barcode value
	 * and expiration timestamp.
	 *
	 * @param request A request object containing the user ID in JSON format.
	 * @return The generated BarcodeDTO containing the barcode value and expiration timestamp.
	 */
	@PostMapping("/generate-barcode")
	public BarcodeDTO generateBarcode(@RequestBody Map<String, String> request) {
		String userId = request.get("userId");
		return barcodeService.generateBarcode(userId);
	}

	/**
	 * Retrieves the barcode information associated with the given barcode code.
	 *
	 * @param code The unique identifier of the barcode.
	 * @return The BarcodeDTO containing the barcode information.
	 */
	@GetMapping("/get-barcode/{code}")
	public BarcodeDTO getBarcode(@PathVariable String code) {
		return barcodeService.getBarcodeByCode(code);
	}

	/**
	 * Marks a barcode as "used" based on the provided barcode code.
	 * If the barcode is expired, it will still be set to "used".
	 *
	 * @param request A request object containing the barcode code in JSON format.
	 */
	@PostMapping("/update-barcode-status")
	public void updateBarcodeStatus(@RequestBody Map<String, String> request) {
		String barcodeCode = request.get("code");
		barcodeService.markBarcodeAsUsed(barcodeCode);
	}

	/**
	 * Checks if the barcode associated with the given barcode code is still valid.
	 * A barcode is considered valid if it has not expired and has not been marked as "used".
	 *
	 * @param code The unique identifier of the barcode.
	 * @return True if the barcode is valid (not expired and not marked as "used"), false otherwise.
	 */
	@GetMapping("/is-barcode-valid/{code}")
	public boolean isBarcodeValid(@PathVariable String code) {
		return barcodeService.isBarcodeValid(code);
	}
}
