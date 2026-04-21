package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.sofka.plataforma_danos_back.folios.domain.AlertaBloqueante;
import com.sofka.plataforma_danos_back.folios.domain.EstadoValidacion;
import com.sofka.plataforma_danos_back.folios.domain.Giro;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionCotizacion;
import com.sofka.plataforma_danos_back.folios.domain.UbicacionDetalle;
import com.sofka.plataforma_danos_back.folios.domain.ZonaCatastrofica;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "cotizacion_ubicaciones",
        uniqueConstraints = @UniqueConstraint(name = "uk_cotizacion_ubicaciones_cotizacion_indice", columnNames = {"cotizacion_id", "indice"})
)
public class UbicacionCotizacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cotizacion_id", nullable = false)
    private Long cotizacionId;

    @Column(name = "indice", nullable = false)
    private Integer indice;

    @Column(name = "nombre_ubicacion", length = 160)
    private String nombreUbicacion;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "codigo_postal", length = 16)
    private String codigoPostal;

    @Column(name = "estado", length = 100)
    private String estado;

    @Column(name = "municipio", length = 100)
    private String municipio;

    @Column(name = "colonia", length = 100)
    private String colonia;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "tipo_constructivo", length = 64)
    private String tipoConstructivo;

    @Column(name = "nivel")
    private Integer nivel;

    @Column(name = "anio_construccion")
    private Integer anioConstruccion;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "codigo", column = @Column(name = "giro_codigo", length = 32)),
            @AttributeOverride(name = "nombre", column = @Column(name = "giro_nombre", length = 160)),
            @AttributeOverride(name = "claveIncendio", column = @Column(name = "giro_clave_incendio", length = 32))
    })
    private GiroEmbeddable giro;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zonaTev", column = @Column(name = "zona_catastrofica_zona_tev", length = 32)),
            @AttributeOverride(name = "zonaFhm", column = @Column(name = "zona_catastrofica_zona_fhm", length = 32))
    })
    private ZonaCatastroficaEmbeddable zonaCatastrofica;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false, length = 20)
    private EstadoValidacion estadoValidacion;

    @Convert(converter = JsonAlertaBloqueanteListConverter.class)
    @Column(name = "alertas_bloqueantes", nullable = false, columnDefinition = "text")
    private List<AlertaBloqueante> alertasBloqueantes = new ArrayList<>();

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

    public Integer getIndice() {
        return indice;
    }

    public void setIndice(Integer indice) {
        this.indice = indice;
    }

    public String getNombreUbicacion() {
        return nombreUbicacion;
    }

    public void setNombreUbicacion(String nombreUbicacion) {
        this.nombreUbicacion = nombreUbicacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getColonia() {
        return colonia;
    }

    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getTipoConstructivo() {
        return tipoConstructivo;
    }

    public void setTipoConstructivo(String tipoConstructivo) {
        this.tipoConstructivo = tipoConstructivo;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public Integer getAnioConstruccion() {
        return anioConstruccion;
    }

    public void setAnioConstruccion(Integer anioConstruccion) {
        this.anioConstruccion = anioConstruccion;
    }

    public GiroEmbeddable getGiro() {
        return giro;
    }

    public void setGiro(GiroEmbeddable giro) {
        this.giro = giro;
    }

    public ZonaCatastroficaEmbeddable getZonaCatastrofica() {
        return zonaCatastrofica;
    }

    public void setZonaCatastrofica(ZonaCatastroficaEmbeddable zonaCatastrofica) {
        this.zonaCatastrofica = zonaCatastrofica;
    }

    public EstadoValidacion getEstadoValidacion() {
        return estadoValidacion;
    }

    public void setEstadoValidacion(EstadoValidacion estadoValidacion) {
        this.estadoValidacion = estadoValidacion;
    }

    public List<AlertaBloqueante> getAlertasBloqueantes() {
        return alertasBloqueantes;
    }

    public void setAlertasBloqueantes(List<AlertaBloqueante> alertasBloqueantes) {
        this.alertasBloqueantes = alertasBloqueantes == null ? new ArrayList<>() : new ArrayList<>(alertasBloqueantes);
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
        if (alertasBloqueantes == null) {
            alertasBloqueantes = new ArrayList<>();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UbicacionCotizacion toDomain() {
        return new UbicacionCotizacion(
                id,
                cotizacionId,
                new UbicacionDetalle(
                        indice,
                        nombreUbicacion,
                        direccion,
                        codigoPostal,
                        estado,
                        municipio,
                        colonia,
                        ciudad,
                        tipoConstructivo,
                        nivel,
                        anioConstruccion,
                        giro == null ? null : giro.toDomain(),
                        zonaCatastrofica == null ? null : zonaCatastrofica.toDomain()
                ),
                estadoValidacion,
                alertasBloqueantes,
                createdAt,
                updatedAt
        );
    }

    public static UbicacionCotizacionEntity fromDomain(UbicacionCotizacion ubicacion) {
        UbicacionCotizacionEntity entity = new UbicacionCotizacionEntity();
        entity.id = ubicacion.id();
        entity.cotizacionId = ubicacion.cotizacionId();
        entity.indice = ubicacion.detalle().indice();
        entity.nombreUbicacion = ubicacion.detalle().nombreUbicacion();
        entity.direccion = ubicacion.detalle().direccion();
        entity.codigoPostal = ubicacion.detalle().codigoPostal();
        entity.estado = ubicacion.detalle().estado();
        entity.municipio = ubicacion.detalle().municipio();
        entity.colonia = ubicacion.detalle().colonia();
        entity.ciudad = ubicacion.detalle().ciudad();
        entity.tipoConstructivo = ubicacion.detalle().tipoConstructivo();
        entity.nivel = ubicacion.detalle().nivel();
        entity.anioConstruccion = ubicacion.detalle().anioConstruccion();
        entity.giro = ubicacion.detalle().giro() == null ? null : GiroEmbeddable.fromDomain(ubicacion.detalle().giro());
        entity.zonaCatastrofica = ubicacion.detalle().zonaCatastrofica() == null ? null : ZonaCatastroficaEmbeddable.fromDomain(ubicacion.detalle().zonaCatastrofica());
        entity.estadoValidacion = ubicacion.estadoValidacion();
        entity.alertasBloqueantes = new ArrayList<>(ubicacion.alertasBloqueantes());
        entity.createdAt = ubicacion.createdAt();
        entity.updatedAt = ubicacion.updatedAt();
        return entity;
    }

    @Embeddable
    public static class GiroEmbeddable {

        @Column(name = "giro_codigo", length = 32)
        private String codigo;

        @Column(name = "giro_nombre", length = 160)
        private String nombre;

        @Column(name = "giro_clave_incendio", length = 32)
        private String claveIncendio;

        public GiroEmbeddable() {
        }

        public String getCodigo() {
            return codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getClaveIncendio() {
            return claveIncendio;
        }

        public Giro toDomain() {
            return new Giro(codigo, nombre, claveIncendio);
        }

        public static GiroEmbeddable fromDomain(Giro giro) {
            GiroEmbeddable embeddable = new GiroEmbeddable();
            embeddable.codigo = giro.codigo();
            embeddable.nombre = giro.nombre();
            embeddable.claveIncendio = giro.claveIncendio();
            return embeddable;
        }
    }

    @Embeddable
    public static class ZonaCatastroficaEmbeddable {

        @Column(name = "zona_catastrofica_zona_tev", length = 32)
        private String zonaTev;

        @Column(name = "zona_catastrofica_zona_fhm", length = 32)
        private String zonaFhm;

        public ZonaCatastroficaEmbeddable() {
        }

        public String getZonaTev() {
            return zonaTev;
        }

        public String getZonaFhm() {
            return zonaFhm;
        }

        public ZonaCatastrofica toDomain() {
            return new ZonaCatastrofica(zonaTev, zonaFhm);
        }

        public static ZonaCatastroficaEmbeddable fromDomain(ZonaCatastrofica zonaCatastrofica) {
            ZonaCatastroficaEmbeddable embeddable = new ZonaCatastroficaEmbeddable();
            embeddable.zonaTev = zonaCatastrofica.zonaTev();
            embeddable.zonaFhm = zonaCatastrofica.zonaFhm();
            return embeddable;
        }
    }
}