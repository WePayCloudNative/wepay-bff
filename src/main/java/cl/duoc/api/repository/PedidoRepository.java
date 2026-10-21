package cl.duoc.api.repository;

import cl.duoc.api.dto.PedidoRequest;
import cl.duoc.api.dto.PedidoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import java.util.Map;

import java.util.List;

@Repository
public class PedidoRepository {

    private final RestClient restClient;

    public PedidoRepository(RestClient.Builder builder, @Value("${microservices.pedidos.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public List<PedidoResponse> obtenerTodos() {
        return restClient.get()
                .uri("/api/pedidos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<PedidoResponse>>() {});
    }

    public List<PedidoResponse> obtenerPorUsuario(String oid) {
        return restClient.get()
                .uri("/api/pedidos/usuario/" + oid)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PedidoResponse>>() {});
    }

    public PedidoResponse crearPedido(String oid, PedidoRequest request) {
        return restClient.post()
                .uri("/api/pedidos/crear/" + oid)
                .body(request)
                .retrieve()
                .body(PedidoResponse.class);
    }
    public PedidoResponse actualizarEstado(Long id, String status){
        return restClient.put()
                .uri("/api/pedidos/{id}/status", id)
                .body(Map.of("status", status))
                .retrieve()
                .body(PedidoResponse.class);
    }
}