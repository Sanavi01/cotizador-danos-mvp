package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.sofka.plataforma_danos_back.folios.domain.ConfiguracionLayout;
import com.sofka.plataforma_danos_back.folios.domain.LayoutUbicacionSlot;
import com.sofka.plataforma_danos_back.folios.domain.ModoCaptura;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "cotizacion_layout_ubicaciones")
public class ConfiguracionLayoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cotizacion_id", nullable = false, unique = true)
    private Long cotizacionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_captura", nullable = false, length = 16)
    private ModoCaptura modoCaptura;

    @Column(name = "cantidad_ubicaciones", nullable = false)
    private Integer cantidadUbicaciones;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "cotizacion_layout_ubicacion_slots",
            joinColumns = @JoinColumn(name = "cotizacion_layout_id")
    )
    @OrderBy("ordenCaptura ASC")
    private List<LayoutUbicacionSlotEmbeddable> ubicaciones = new ArrayList<>();

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

    public ModoCaptura getModoCaptura() {
        return modoCaptura;
    }

    public void setModoCaptura(ModoCaptura modoCaptura) {
        this.modoCaptura = modoCaptura;
    }

    public Integer getCantidadUbicaciones() {
        return cantidadUbicaciones;
    }

    public void setCantidadUbicaciones(Integer cantidadUbicaciones) {
        this.cantidadUbicaciones = cantidadUbicaciones;
    }

    public List<LayoutUbicacionSlotEmbeddable> getUbicaciones() {
        return ubicaciones;
    }

    public void setUbicaciones(List<LayoutUbicacionSlotEmbeddable> ubicaciones) {
        this.ubicaciones = ubicaciones == null ? new ArrayList<>() : new ArrayList<>(ubicaciones);
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
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public ConfiguracionLayout toDomain() {
        return new ConfiguracionLayout(
                id,
                cotizacionId,
                modoCaptura,
                cantidadUbicaciones,
                ubicaciones.stream()
                        .map(LayoutUbicacionSlotEmbeddable::toDomain)
                        .toList(),
                createdAt,
                updatedAt
        );
    }

    public static ConfiguracionLayoutEntity fromDomain(ConfiguracionLayout configuracionLayout) {
        ConfiguracionLayoutEntity entity = new ConfiguracionLayoutEntity();
        entity.id = configuracionLayout.id();
        entity.cotizacionId = configuracionLayout.cotizacionId();
        entity.modoCaptura = configuracionLayout.modoCaptura();
        entity.cantidadUbicaciones = configuracionLayout.cantidadUbicaciones();
        entity.ubicaciones = new ArrayList<>(configuracionLayout.ubicaciones().stream()
                .map(LayoutUbicacionSlotEmbeddable::fromDomain)
            .toList());
        entity.createdAt = configuracionLayout.createdAt();
        entity.updatedAt = configuracionLayout.updatedAt();
        return entity;
    }

    @Embeddable
    public static class LayoutUbicacionSlotEmbeddable {

        @Column(name = "indice", nullable = false)
        private Integer indice;

        @Column(name = "orden_captura", nullable = false)
        private Integer ordenCaptura;

        public Integer getIndice() {
            return indice;
        }

        public void setIndice(Integer indice) {
            this.indice = indice;
        }

        public Integer getOrdenCaptura() {
            return ordenCaptura;
        }

        public void setOrdenCaptura(Integer ordenCaptura) {
            this.ordenCaptura = ordenCaptura;
        }

        public LayoutUbicacionSlot toDomain() {
            return new LayoutUbicacionSlot(indice, ordenCaptura);
        }

        public static LayoutUbicacionSlotEmbeddable fromDomain(LayoutUbicacionSlot slot) {
            LayoutUbicacionSlotEmbeddable embeddable = new LayoutUbicacionSlotEmbeddable();
            embeddable.indice = slot.indice();
            embeddable.ordenCaptura = slot.ordenCaptura();
            return embeddable;
        }
    }
}