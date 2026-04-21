package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity;

import com.sofka.plataforma_danos_back.folios.domain.DatosGeneralesCotizacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "cotizacion_datos_generales")
public class DatosGeneralesCotizacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cotizacion_id", nullable = false, unique = true)
    private Long cotizacionId;

    @Column(name = "tipo_documento", nullable = false, length = 32)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 32)
    private String numeroDocumento;

    @Column(name = "nombre_o_razon_social", nullable = false, length = 160)
    private String nombreORazonSocial;

    @Column(name = "correo_electronico", length = 160)
    private String correoElectronico;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "codigo_agente", nullable = false, length = 32)
    private String codigoAgente;

    @Column(name = "clasificacion_riesgo", nullable = false, length = 32)
    private String clasificacionRiesgo;

    @Column(name = "tipo_negocio", nullable = false, length = 32)
    private String tipoNegocio;

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

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getNombreORazonSocial() {
        return nombreORazonSocial;
    }

    public void setNombreORazonSocial(String nombreORazonSocial) {
        this.nombreORazonSocial = nombreORazonSocial;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCodigoAgente() {
        return codigoAgente;
    }

    public void setCodigoAgente(String codigoAgente) {
        this.codigoAgente = codigoAgente;
    }

    public String getClasificacionRiesgo() {
        return clasificacionRiesgo;
    }

    public void setClasificacionRiesgo(String clasificacionRiesgo) {
        this.clasificacionRiesgo = clasificacionRiesgo;
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }

    public void setTipoNegocio(String tipoNegocio) {
        this.tipoNegocio = tipoNegocio;
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
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public DatosGeneralesCotizacion toDomain() {
        return new DatosGeneralesCotizacion(
                cotizacionId,
                new DatosGeneralesCotizacion.DatosAsegurado(
                        tipoDocumento,
                        numeroDocumento,
                        nombreORazonSocial,
                        correoElectronico,
                        telefono
                ),
                new DatosGeneralesCotizacion.DatosConduccion(
                        codigoAgente,
                        clasificacionRiesgo,
                        tipoNegocio
                ),
                createdAt,
                updatedAt
        );
    }

    public static DatosGeneralesCotizacionEntity fromDomain(DatosGeneralesCotizacion datosGeneralesCotizacion) {
        DatosGeneralesCotizacionEntity entity = new DatosGeneralesCotizacionEntity();
        entity.cotizacionId = datosGeneralesCotizacion.cotizacionId();
        entity.tipoDocumento = datosGeneralesCotizacion.datosAsegurado().tipoDocumento();
        entity.numeroDocumento = datosGeneralesCotizacion.datosAsegurado().numeroDocumento();
        entity.nombreORazonSocial = datosGeneralesCotizacion.datosAsegurado().nombreORazonSocial();
        entity.correoElectronico = datosGeneralesCotizacion.datosAsegurado().correoElectronico();
        entity.telefono = datosGeneralesCotizacion.datosAsegurado().telefono();
        entity.codigoAgente = datosGeneralesCotizacion.datosConduccion().codigoAgente();
        entity.clasificacionRiesgo = datosGeneralesCotizacion.datosConduccion().clasificacionRiesgo();
        entity.tipoNegocio = datosGeneralesCotizacion.datosConduccion().tipoNegocio();
        entity.createdAt = datosGeneralesCotizacion.createdAt();
        entity.updatedAt = datosGeneralesCotizacion.updatedAt();
        return entity;
    }
}