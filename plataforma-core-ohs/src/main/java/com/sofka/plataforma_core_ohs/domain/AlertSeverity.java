package com.sofka.plataforma_core_ohs.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AlertSeverity {

	INFO("Info"),
	WARNING("Warning"),
	ERROR("Error");

	private final String wireValue;

	AlertSeverity(String wireValue) {
		this.wireValue = wireValue;
	}

	@JsonValue
	public String getWireValue() {
		return wireValue;
	}
}