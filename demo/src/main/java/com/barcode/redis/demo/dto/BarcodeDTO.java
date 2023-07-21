package com.barcode.redis.demo.dto;

import java.sql.Timestamp;

public class BarcodeDTO {
	
	private String code;
    private Timestamp expireAt;
    
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Timestamp getExpireAt() {
		return expireAt;
	}
	public void setExpireAt(Timestamp expireAt) {
		this.expireAt = expireAt;
	}


}
