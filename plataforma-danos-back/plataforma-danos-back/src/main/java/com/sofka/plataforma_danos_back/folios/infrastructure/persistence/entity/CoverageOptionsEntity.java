package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.sofka.plataforma_danos_back.folios.domain.CoverageOptions;
import com.sofka.plataforma_danos_back.folios.domain.SelectedGuarantee;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "cotizacion_opciones_cobertura")
public class CoverageOptionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cotizacion_id", nullable = false, unique = true)
    private Long cotizacionId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Convert(converter = JsonSelectedGuaranteeListConverter.class)
    @Column(name = "garantias_seleccionadas", nullable = false, columnDefinition = "text")
    private List<SelectedGuarantee> garantiasSeleccionadas = new ArrayList<>();

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public Long getCotizacionId() {
        return cotizacionId;
    }

    public void setCotizacionId(Long cotizacionId) {
        this.cotizacionId = cotizacionId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<SelectedGuarantee> getGarantiasSeleccionadas() {
        return garantiasSeleccionadas;
    }

    public void setGarantiasSeleccionadas(List<SelectedGuarantee> garantiasSeleccionadas) {
        this.garantiasSeleccionadas = garantiasSeleccionadas == null ? new ArrayList<>() : new ArrayList<>(garantiasSeleccionadas);
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones == null || observaciones.isBlank() ? null : observaciones.trim();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (version == null) {
            version = 0L;
        }
        if (garantiasSeleccionadas == null) {
            garantiasSeleccionadas = new ArrayList<>();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public CoverageOptions toDomain() {
        return new CoverageOptions(cotizacionId, garantiasSeleccionadas, observaciones);
    }

    public static CoverageOptionsEntity fromDomain(CoverageOptions coverageOptions) {
        CoverageOptionsEntity entity = new CoverageOptionsEntity();
        entity.cotizacionId = coverageOptions.cotizacionId();
        entity.garantiasSeleccionadas = new ArrayList<>(coverageOptions.garantiasSeleccionadas());
        entity.observaciones = coverageOptions.observaciones();
        return entity;
    }
}