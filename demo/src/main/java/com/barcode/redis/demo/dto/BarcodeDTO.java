package com.barcode.redis.demo.dto;

import java.sql.Timestamp;

public class BarcodeDTO {
	private String userId;
	private String code;
	private Timestamp logicalExpireAt;
	private Timestamp physicalExpireAt;
	private Boolean status;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Timestamp getLogicalExpireAt() {
		return logicalExpireAt;
	}

	public void setLogicalExpireAt(Timestamp logicalExpireAt) {
		this.logicalExpireAt = logicalExpireAt;
	}

	public Timestamp getPhysicalExpireAt() {
		return physicalExpireAt;
	}

	public void setPhysicalExpireAt(Timestamp physicalExpireAt) {
		this.physicalExpireAt = physicalExpireAt;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

}
