package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import com.sofka.plataforma_danos_back.folios.domain.port.FolioSequencePort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseFolioSequencePort implements FolioSequencePort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public String nextNumeroFolio() {
        entityManager.createNativeQuery(
                "CREATE SEQUENCE IF NOT EXISTS public.folio_number_sequence START WITH 1000001 INCREMENT BY 1"
        ).executeUpdate();
        Number value = (Number) entityManager.createNativeQuery("select nextval('public.folio_number_sequence')").getSingleResult();
        return String.valueOf(value.longValue());
    }
}
