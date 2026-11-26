package cl.duoc.api.repository;

import cl.duoc.api.dto.ItemPedidoRequest;
import cl.duoc.api.dto.PedidoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import java.util.HashMap;
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

    public PedidoResponse crearPedido(String oid, List<ItemPedidoRequest> items) {
        return restClient.post()
                .uri("/api/pedidos/crear/{oid}", oid)
                .body(Map.of("items", items))
                .retrieve()
                .body(PedidoResponse.class);
    }
    /**
     * actorOid y actorRol dicen quién hace el cambio (sale del JWT, nunca del navegador).
     * ms-pedidos los pone en el evento de Kafka y ms-auditoria los guarda.
     */
    public PedidoResponse actualizarEstado(Long id, String status, String actorOid, String actorRol) {
        return restClient.put()
                .uri("/api/pedidos/{id}/status", id)
                .body(cuerpoEstado(status, actorOid, actorRol))
                .retrieve()
                .body(PedidoResponse.class);
    }

    // Map.of no acepta valores null; HashMap sí (un actor vacío lo resuelve ms-pedidos como SISTEMA)
    private static Map<String, String> cuerpoEstado(String status, String actorOid, String actorRol) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("status", status);
        cuerpo.put("actorOid", actorOid);
        cuerpo.put("actorRol", actorRol);
        return cuerpo;
    }
}
