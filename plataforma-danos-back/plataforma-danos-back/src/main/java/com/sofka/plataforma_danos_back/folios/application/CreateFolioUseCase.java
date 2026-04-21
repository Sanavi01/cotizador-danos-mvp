package com.sofka.plataforma_danos_back.folios.application;

import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioRequest;
import com.sofka.plataforma_danos_back.folios.application.dto.CreateFolioResponse;
import com.sofka.plataforma_danos_back.folios.application.dto.FolioCreationResult;
import com.sofka.plataforma_danos_back.folios.application.idempotency.IdempotencyPayloadCodec;
import com.sofka.plataforma_danos_back.folios.application.exception.IdempotencyConflictException;
import com.sofka.plataforma_danos_back.folios.application.exception.MissingIdempotencyKeyException;
import com.sofka.plataforma_danos_back.folios.domain.Cotizacion;
import com.sofka.plataforma_danos_back.folios.domain.IdempotencyRecord;
import com.sofka.plataforma_danos_back.folios.domain.port.CotizacionRepository;
import com.sofka.plataforma_danos_back.folios.domain.port.FolioSequencePort;
import com.sofka.plataforma_danos_back.folios.domain.port.IdempotencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class CreateFolioUseCase {

    private final CotizacionRepository cotizacionRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final FolioSequencePort folioSequencePort;
    private final IdempotencyPayloadCodec idempotencyPayloadCodec;
    private final Clock clock;

    @Autowired
    public CreateFolioUseCase(
            CotizacionRepository cotizacionRepository,
            IdempotencyRepository idempotencyRepository,
            FolioSequencePort folioSequencePort,
            IdempotencyPayloadCodec idempotencyPayloadCodec
    ) {
        this(cotizacionRepository, idempotencyRepository, folioSequencePort, idempotencyPayloadCodec, Clock.systemUTC());
    }

    public CreateFolioUseCase(
            CotizacionRepository cotizacionRepository,
            IdempotencyRepository idempotencyRepository,
            FolioSequencePort folioSequencePort,
            IdempotencyPayloadCodec idempotencyPayloadCodec,
            Clock clock
    ) {
        this.cotizacionRepository = cotizacionRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.folioSequencePort = folioSequencePort;
        this.idempotencyPayloadCodec = idempotencyPayloadCodec;
        this.clock = clock;
    }

    @Transactional
    public FolioCreationResult handle(CreateFolioRequest request, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new MissingIdempotencyKeyException();
        }

        CreateFolioRequest normalizedRequest = request == null ? CreateFolioRequest.defaultRequest() : request;
        String requestHash = idempotencyPayloadCodec.hashRequest(normalizedRequest);

        return idempotencyRepository.findByKey(idempotencyKey)
                .map(existing -> replayIfCompatible(existing, requestHash))
                .orElseGet(() -> createNewFolio(idempotencyKey, requestHash));
    }

    private FolioCreationResult replayIfCompatible(IdempotencyRecord existing, String requestHash) {
        if (!existing.requestHash().equals(requestHash)) {
            throw new IdempotencyConflictException();
        }

        CreateFolioResponse response = idempotencyPayloadCodec.deserializeCreateFolioResponse(existing.responsePayload());
        return new FolioCreationResult(response, false);
    }

    private FolioCreationResult createNewFolio(String idempotencyKey, String requestHash) {
        Instant now = Instant.now(clock);
        String numeroFolio = folioSequencePort.nextNumeroFolio();
        Cotizacion cotizacion = Cotizacion.nueva(numeroFolio, now);
        Cotizacion savedCotizacion = cotizacionRepository.save(cotizacion);
        CreateFolioResponse response = CreateFolioResponse.from(savedCotizacion);

        String payload = idempotencyPayloadCodec.serializeCreateFolioResponse(response);
        IdempotencyRecord idempotencyRecord = IdempotencyRecord.createNew(
                idempotencyKey,
                requestHash,
                payload,
                savedCotizacion.id(),
                savedCotizacion.numeroFolio(),
                now
        );
        idempotencyRepository.save(idempotencyRecord);
        return new FolioCreationResult(response, true);
    }
}
