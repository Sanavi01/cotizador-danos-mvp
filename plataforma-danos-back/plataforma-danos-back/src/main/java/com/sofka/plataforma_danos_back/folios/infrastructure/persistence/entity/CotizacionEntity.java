package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCalculo;
import com.sofka.plataforma_danos_back.folios.domain.EstadoCotizacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "cotizaciones_danos")
public class CotizacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_folio", nullable = false, unique = true, length = 32)
    private String numeroFolio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cotizacion", nullable = false, length = 50)
    private EstadoCotizacion estadoCotizacion;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "fecha_ultima_actualizacion", nullable = false)
    private Instant fechaUltimaActualizacion;

    @Column(name = "prima_neta")
    private BigDecimal primaNeta;

    @Column(name = "prima_comercial")
    private BigDecimal primaComercial;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_calculo", length = 20)
    private EstadoCalculo estadoCalculo;

    @Column(name = "calculated_at")
    private Instant calculatedAt;

    @Column(name = "calculation_parameter_version", length = 32)
    private String calculationParameterVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public String getNumeroFolio() {
        return numeroFolio;
    }

    public void setNumeroFolio(String numeroFolio) {
        this.numeroFolio = numeroFolio;
    }

    public EstadoCotizacion getEstadoCotizacion() {
        return estadoCotizacion;
    }

    public void setEstadoCotizacion(EstadoCotizacion estadoCotizacion) {
        this.estadoCotizacion = estadoCotizacion;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public void setFechaUltimaActualizacion(Instant fechaUltimaActualizacion) {
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public BigDecimal getPrimaNeta() {
        return primaNeta;
    }

    public void setPrimaNeta(BigDecimal primaNeta) {
        this.primaNeta = primaNeta;
    }

    public BigDecimal getPrimaComercial() {
        return primaComercial;
    }

    public void setPrimaComercial(BigDecimal primaComercial) {
        this.primaComercial = primaComercial;
    }

    public EstadoCalculo getEstadoCalculo() {
        return estadoCalculo;
    }

    public void setEstadoCalculo(EstadoCalculo estadoCalculo) {
        this.estadoCalculo = estadoCalculo;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public String getCalculationParameterVersion() {
        return calculationParameterVersion;
    }

    public void setCalculationParameterVersion(String calculationParameterVersion) {
        this.calculationParameterVersion = calculationParameterVersion;
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
        if (fechaUltimaActualizacion == null) {
            fechaUltimaActualizacion = now;
        }
        if (estadoCotizacion == null) {
            estadoCotizacion = EstadoCotizacion.BORRADOR;
        }
        if (version == null) {
            version = 0L;
        }
    }

    @PreUpdate
    void onUpdate() {
        Instant now = Instant.now();
        updatedAt = now;
        fechaUltimaActualizacion = now;
    }

    public Cotizacion toDomain() {
        return new Cotizacion(
                id,
                numeroFolio,
                estadoCotizacion,
                version,
                fechaUltimaActualizacion,
            primaNeta,
            primaComercial,
            estadoCalculo,
            calculatedAt,
            calculationParameterVersion
        );
    }

    public static CotizacionEntity fromDomain(Cotizacion cotizacion) {
        CotizacionEntity entity = new CotizacionEntity();
        entity.id = cotizacion.id();
        entity.numeroFolio = cotizacion.numeroFolio();
        entity.estadoCotizacion = cotizacion.estadoCotizacion();
        entity.version = cotizacion.version();
        entity.fechaUltimaActualizacion = cotizacion.fechaUltimaActualizacion();
        entity.primaNeta = cotizacion.primaNeta();
        entity.primaComercial = cotizacion.primaComercial();
        entity.estadoCalculo = cotizacion.estadoCalculo();
        entity.calculatedAt = cotizacion.calculatedAt();
        entity.calculationParameterVersion = cotizacion.calculationParameterVersion();
        return entity;
    }
}
