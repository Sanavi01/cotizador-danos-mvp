package com.sofka.plataforma_core_ohs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(title = "Plataforma Core OHS", version = "v1"))
@SpringBootApplication
public class PlataformaCoreOhsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlataformaCoreOhsApplication.class, args);
	}

}
