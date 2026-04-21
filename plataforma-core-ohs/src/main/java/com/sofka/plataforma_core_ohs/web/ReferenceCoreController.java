package com.sofka.plataforma_core_ohs.web;

import com.sofka.plataforma_core_ohs.application.ReferenceCoreService;
import com.sofka.plataforma_core_ohs.application.TariffUpsertCommand;
import com.sofka.plataforma_core_ohs.domain.CatalogItem;
import com.sofka.plataforma_core_ohs.domain.FolioSequence;
import com.sofka.plataforma_core_ohs.domain.TariffRecord;
import com.sofka.plataforma_core_ohs.domain.ZipCodeInfo;
import com.sofka.plataforma_core_ohs.web.dto.TariffUpsertRequest;
import com.sofka.plataforma_core_ohs.web.dto.ZipCodeValidationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/v1")
@Tag(name = "Referencia Core", description = "Catalogos, validacion postal y tarifas tecnicas del core mock")
public class ReferenceCoreController {

	private final ReferenceCoreService referenceCoreService;

	public ReferenceCoreController(ReferenceCoreService referenceCoreService) {
		this.referenceCoreService = referenceCoreService;
	}

	@GetMapping("/subscribers")
	@Operation(summary = "Listar suscriptores")
	public ResponseEntity<ApiResponse<List<CatalogItem>>> listSubscribers() {
		return ResponseEntity.ok(ApiResponse.of(referenceCoreService.listSubscribers()));
	}

	@GetMapping("/agents")
	@Operation(summary = "Listar agentes")
	public ResponseEntity<ApiResponse<List<CatalogItem>>> listAgents() {
		return ResponseEntity.ok(ApiResponse.of(referenceCoreService.listAgents()));
	}

	@GetMapping("/business-lines")
	@Operation(summary = "Listar giros comerciales")
	public ResponseEntity<ApiResponse<List<CatalogItem>>> listBusinessLines() {
		return ResponseEntity.ok(ApiResponse.of(referenceCoreService.listBusinessLines()));
	}

	@GetMapping("/catalogs/risk-classification")
	@Operation(summary = "Listar clasificacion de riesgo")
	public ResponseEntity<ApiResponse<List<CatalogItem>>> listRiskClassifications() {
		return ResponseEntity.ok(ApiResponse.of(referenceCoreService.listRiskClassifications()));
	}

	@GetMapping("/catalogs/guarantees")
	@Operation(summary = "Listar garantias")
	public ResponseEntity<ApiResponse<List<CatalogItem>>> listGuarantees() {
		return ResponseEntity.ok(ApiResponse.of(referenceCoreService.listGuarantees()));
	}

	@GetMapping("/zip-codes/{zipCode}")
	@Operation(summary = "Consultar codigo postal")
	public ResponseEntity<ApiResponse<ZipCodeInfo>> getZipCode(
			@PathVariable @NotBlank(message = "El codigo postal es obligatorio")
			@Pattern(regexp = "\\d{6}", message = "El codigo postal debe tener 6 digitos") String zipCode) {
		return ResponseEntity.ok(ApiResponse.of(referenceCoreService.getZipCode(zipCode)));
	}

	@PostMapping("/zip-codes/validate")
	@Operation(summary = "Validar codigo postal")
	public ResponseEntity<ApiResponse<ZipCodeInfo>> validateZipCode(@Valid @RequestBody ZipCodeValidationRequest request) {
		return ResponseEntity.ok(ApiResponse.of(referenceCoreService.validateZipCode(request.zipCode())));
	}

	@GetMapping("/folios")
	@Operation(summary = "Obtener secuencia de folios")
	public ResponseEntity<ApiResponse<FolioSequence>> getFolioSequence() {
		return ResponseEntity.ok(ApiResponse.of(referenceCoreService.getFolioSequence()));
	}

	@GetMapping("/tariffs/{tariffKey}")
	@Operation(summary = "Resolver tarifa tecnica")
	public ResponseEntity<ApiResponse<TariffRecord>> getTariff(
			@PathVariable @NotBlank(message = "La clave de tarifa es obligatoria") String tariffKey) {
		return ResponseEntity.ok(ApiResponse.of(referenceCoreService.getTariff(tariffKey)));
	}

	@PutMapping("/tariffs/{tariffKey}")
	@Operation(summary = "Publicar o actualizar una tarifa tecnica")
	public ResponseEntity<ApiResponse<TariffKeyResponse>> upsertTariff(
			@PathVariable @NotBlank(message = "La clave de tarifa es obligatoria") String tariffKey,
			@Valid @RequestBody TariffUpsertRequest request) {
		TariffRecord tariffRecord = referenceCoreService.upsertTariff(tariffKey, toCommand(request));
		return ResponseEntity.ok(ApiResponse.of(new TariffKeyResponse(tariffRecord.tariffKey())));
	}

	private TariffUpsertCommand toCommand(TariffUpsertRequest request) {
		return new TariffUpsertCommand(
				request.giroCode(),
				request.zonaCode(),
				request.garantiaCode(),
				request.rate(),
				request.factor(),
				request.moneda(),
				request.vigenciaDesde(),
				request.vigenciaHasta());
	}

	public record TariffKeyResponse(String tariffKey) {
	}
}