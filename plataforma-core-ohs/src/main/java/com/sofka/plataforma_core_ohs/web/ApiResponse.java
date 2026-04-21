package com.sofka.plataforma_core_ohs.web;

public record ApiResponse<T>(T data) {

	public static <T> ApiResponse<T> of(T data) {
		return new ApiResponse<>(data);
	}
}