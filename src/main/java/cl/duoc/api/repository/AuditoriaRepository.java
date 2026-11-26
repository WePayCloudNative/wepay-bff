package cl.duoc.api.repository;

import cl.duoc.api.dto.EventoAuditoriaResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

/** Cliente HTTP de ms-auditoria (puerto 8086, solo dentro de la red Docker). */
@Repository
public class AuditoriaRepository {

    private static final ParameterizedTypeReference<List<EventoAuditoriaResponse>> LISTA =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public AuditoriaRepository(RestClient.Builder builder,
            @Value("${microservices.auditoria.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public List<EventoAuditoriaResponse> obtenerTodos() {
        return restClient.get().uri("/api/audit").retrieve().body(LISTA);
    }

    public List<EventoAuditoriaResponse> obtenerPorPedido(Long pedidoId) {
        return restClient.get().uri("/api/audit/entidad/{id}", pedidoId).retrieve().body(LISTA);
    }

    /** desde y hasta van juntos, en formato ISO (2026-11-24T00:00:00). */
    public List<EventoAuditoriaResponse> filtrar(String tipoEvento, String desde, String hasta) {
        return restClient.get()
                .uri(uri -> {
                    uri.path("/api/audit/filter");
                    if (tipoEvento != null) {
                        uri.queryParam("tipoEvento", tipoEvento);
                    }
                    if (desde != null) {
                        uri.queryParam("desde", desde).queryParam("hasta", hasta);
                    }
                    return uri.build();
                })
                .retrieve()
                .body(LISTA);
    }
}
