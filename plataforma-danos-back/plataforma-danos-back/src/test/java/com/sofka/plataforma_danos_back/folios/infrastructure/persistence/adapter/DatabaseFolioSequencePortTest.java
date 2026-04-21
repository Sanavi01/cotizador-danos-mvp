package com.sofka.plataforma_danos_back.folios.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@ExtendWith(MockitoExtension.class)
class DatabaseFolioSequencePortTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @Test
    void nextNumeroFolio_createsSequenceAndReturnsNextValue() {
        DatabaseFolioSequencePort port = new DatabaseFolioSequencePort();
        ReflectionTestUtils.setField(port, "entityManager", entityManager);

        when(entityManager.createNativeQuery("CREATE SEQUENCE IF NOT EXISTS public.folio_number_sequence START WITH 1000001 INCREMENT BY 1"))
                .thenReturn(query);
        when(entityManager.createNativeQuery("select nextval('public.folio_number_sequence')")).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        when(query.getSingleResult()).thenReturn(1000001L);

        String numeroFolio = port.nextNumeroFolio();

        assertEquals("1000001", numeroFolio);
        verify(entityManager).createNativeQuery("CREATE SEQUENCE IF NOT EXISTS public.folio_number_sequence START WITH 1000001 INCREMENT BY 1");
        verify(entityManager).createNativeQuery("select nextval('public.folio_number_sequence')");
        verify(query).executeUpdate();
        verify(query).getSingleResult();
    }
}