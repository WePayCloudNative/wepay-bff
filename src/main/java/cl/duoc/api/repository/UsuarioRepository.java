package cl.duoc.api.repository;

import cl.duoc.api.dto.DireccionDespacho;
import cl.duoc.api.dto.UsuarioResponse;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Repository
public class UsuarioRepository {
    private final RestClient restClient;

    public UsuarioRepository(
            RestClient.Builder restClientBuilder,
            @Value("${microservices.usuarios.base-url}") String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public UsuarioResponse buscarPorOid(String oid, String email, String nombre) {
    try {
        return restClient
                .get()
                .uri("/api/usuarios/{oid}", oid)
                .retrieve()
                .body(UsuarioResponse.class);
        } catch (HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            var usuario = new UsuarioResponse(oid, nombre, email, "user", null);
            return restClient
                    .post()
                    .uri("/api/usuarios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(usuario)
                    .retrieve()
                    .body(UsuarioResponse.class);
        }
        throw e;
        }
    }
    

    /** Solo lectura: no crea el usuario si no existe (a diferencia de buscarPorOid). */
    public Optional<UsuarioResponse> obtener(String oid) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/api/usuarios/{oid}", oid)
                    .retrieve()
                    .body(UsuarioResponse.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public UsuarioResponse guardarDireccion(String oid, DireccionDespacho direccion) {
        return restClient
                .put()
                .uri("/api/usuarios/{oid}/direccion", oid)
                .contentType(MediaType.APPLICATION_JSON)
                .body(direccion)
                .retrieve()
                .body(UsuarioResponse.class);
    }
}
