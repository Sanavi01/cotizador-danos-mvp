package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.ComponenteComercialCalculado;
import com.sofka.plataforma_danos_back.folios.domain.GarantiaCalculada;
import com.sofka.plataforma_danos_back.folios.domain.PrimaPorUbicacion;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "cotizacion_primas_por_ubicacion",
        uniqueConstraints = @UniqueConstraint(name = "uk_cotizacion_primas_por_ubicacion_indice", columnNames = {"cotizacion_id", "indice_ubicacion"})
)
public class PrimaPorUbicacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cotizacion_id", nullable = false)
    private Long cotizacionId;

    @Column(name = "indice_ubicacion", nullable = false)
    private Integer indiceUbicacion;

    @Column(name = "ubicacion_calculable", nullable = false)
    private boolean ubicacionCalculable;

    @Column(name = "prima_neta_ubicacion", nullable = false)
    private BigDecimal primaNetaUbicacion;

    @Column(name = "prima_comercial_ubicacion", nullable = false)
    private BigDecimal primaComercialUbicacion;

    @Convert(converter = JsonGarantiaCalculadaListConverter.class)
    @Column(name = "garantias_calculadas", nullable = false, columnDefinition = "text")
    private List<GarantiaCalculada> garantiasCalculadas = new ArrayList<>();

    @Convert(converter = JsonComponenteComercialListConverter.class)
    @Column(name = "componentes_comerciales", nullable = false, columnDefinition = "text")
    private List<ComponenteComercialCalculado> componentesComerciales = new ArrayList<>();

    @Convert(converter = JsonAlertaBloqueanteListConverter.class)
    @Column(name = "alertas", nullable = false, columnDefinition = "text")
    private List<AlertaBloqueante> alertas = new ArrayList<>();

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

    public Integer getIndiceUbicacion() {
        return indiceUbicacion;
    }

    public void setIndiceUbicacion(Integer indiceUbicacion) {
        this.indiceUbicacion = indiceUbicacion;
    }

    public boolean isUbicacionCalculable() {
        return ubicacionCalculable;
    }

    public void setUbicacionCalculable(boolean ubicacionCalculable) {
        this.ubicacionCalculable = ubicacionCalculable;
    }

    public BigDecimal getPrimaNetaUbicacion() {
        return primaNetaUbicacion;
    }

    public void setPrimaNetaUbicacion(BigDecimal primaNetaUbicacion) {
        this.primaNetaUbicacion = primaNetaUbicacion;
    }

    public BigDecimal getPrimaComercialUbicacion() {
        return primaComercialUbicacion;
    }

    public void setPrimaComercialUbicacion(BigDecimal primaComercialUbicacion) {
        this.primaComercialUbicacion = primaComercialUbicacion;
    }

    public List<GarantiaCalculada> getGarantiasCalculadas() {
        return garantiasCalculadas;
    }

    public void setGarantiasCalculadas(List<GarantiaCalculada> garantiasCalculadas) {
        this.garantiasCalculadas = garantiasCalculadas == null ? new ArrayList<>() : new ArrayList<>(garantiasCalculadas);
    }

    public List<ComponenteComercialCalculado> getComponentesComerciales() {
        return componentesComerciales;
    }

    public void setComponentesComerciales(List<ComponenteComercialCalculado> componentesComerciales) {
        this.componentesComerciales = componentesComerciales == null ? new ArrayList<>() : new ArrayList<>(componentesComerciales);
    }

    public List<AlertaBloqueante> getAlertas() {
        return alertas;
    }

    public void setAlertas(List<AlertaBloqueante> alertas) {
        this.alertas = alertas == null ? new ArrayList<>() : new ArrayList<>(alertas);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (garantiasCalculadas == null) {
            garantiasCalculadas = new ArrayList<>();
        }
        if (componentesComerciales == null) {
            componentesComerciales = new ArrayList<>();
        }
        if (alertas == null) {
            alertas = new ArrayList<>();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public PrimaPorUbicacion toDomain() {
        return new PrimaPorUbicacion(
                indiceUbicacion,
                ubicacionCalculable,
                primaNetaUbicacion,
                primaComercialUbicacion,
                garantiasCalculadas,
                componentesComerciales,
                alertas
        );
    }

    public static PrimaPorUbicacionEntity fromDomain(Long cotizacionId, PrimaPorUbicacion primaPorUbicacion) {
        PrimaPorUbicacionEntity entity = new PrimaPorUbicacionEntity();
        entity.cotizacionId = cotizacionId;
        entity.indiceUbicacion = primaPorUbicacion.indiceUbicacion();
        entity.ubicacionCalculable = primaPorUbicacion.ubicacionCalculable();
        entity.primaNetaUbicacion = primaPorUbicacion.primaNetaUbicacion();
        entity.primaComercialUbicacion = primaPorUbicacion.primaComercialUbicacion();
        entity.garantiasCalculadas = new ArrayList<>(primaPorUbicacion.garantiasCalculadas());
        entity.componentesComerciales = new ArrayList<>(primaPorUbicacion.componentesComerciales());
        entity.alertas = new ArrayList<>(primaPorUbicacion.alertas());
        return entity;
    }
}