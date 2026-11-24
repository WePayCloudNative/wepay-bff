package cl.duoc.api.repository;

import cl.duoc.api.dto.KpiResponse;
import cl.duoc.api.dto.TopProductoResponse;
import cl.duoc.api.dto.VentasPorHoraResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

/** Cliente HTTP de ms-reportes (puerto 8087, solo dentro de la red Docker). */
@Repository
public class ReporteRepository {

    private final RestClient restClient;

    public ReporteRepository(RestClient.Builder builder,
            @Value("${microservices.reportes.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public KpiResponse kpis() {
        return restClient.get().uri("/api/report/kpis?range=last24h").retrieve().body(KpiResponse.class);
    }

    public List<TopProductoResponse> topProductos() {
        return restClient.get().uri("/api/report/top-products?range=last7d").retrieve()
                .body(new ParameterizedTypeReference<List<TopProductoResponse>>() {
                });
    }

    public List<VentasPorHoraResponse> ventasPorHora() {
        return restClient.get().uri("/api/report/sales-by-hour?range=last24h").retrieve()
                .body(new ParameterizedTypeReference<List<VentasPorHoraResponse>>() {
                });
    }
}
