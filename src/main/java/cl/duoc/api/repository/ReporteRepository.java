package cl.duoc.api.repository;

import cl.duoc.api.dto.ResumenReportesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

/** Cliente HTTP de ms-reportes (puerto 8087, solo dentro de la red Docker). */
@Repository
public class ReporteRepository {

    // Los días y horas del panel se agrupan según la hora de Chile
    private static final String ZONA_NEGOCIO = "America/Santiago";

    private final RestClient restClient;

    public ReporteRepository(RestClient.Builder builder,
            @Value("${microservices.reportes.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    /** desde y hasta en UTC, formato ISO (2026-11-24T03:00:00). */
    public ResumenReportesResponse resumen(String desde, String hasta) {
        return restClient.get()
                .uri(uri -> uri.path("/api/report/resumen")
                        .queryParam("desde", desde)
                        .queryParam("hasta", hasta)
                        .queryParam("zona", ZONA_NEGOCIO)
                        .build())
                .retrieve()
                .body(ResumenReportesResponse.class);
    }
}
