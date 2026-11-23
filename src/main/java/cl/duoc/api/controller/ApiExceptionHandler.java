package cl.duoc.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Traduce los errores a respuestas JSON {"mensaje": "..."} con el código correcto.
 * Sin esto, un 404 o 409 que devuelve ms-pedidos llegaba al front como 500.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // Busca "mensaje", "message" o "detail" dentro del JSON de error del microservicio
    private static final Pattern CAMPO_MENSAJE =
            Pattern.compile("\"(mensaje|message|detail)\"\\s*:\\s*\"([^\"]*)\"");

    // Errores que lanza el propio BFF (403 no admin, 404 pedido ajeno, 409 estado no cancelable)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> manejarPropio(ResponseStatusException ex) {
        String mensaje = ex.getReason() != null ? ex.getReason() : "No se pudo completar la operación";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("mensaje", mensaje));
    }

    // El microservicio respondió, pero con error
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, String>> manejarMicroservicio(RestClientResponseException ex) {
        HttpStatusCode status = ex.getStatusCode();
        // 4xx de negocio (400, 404, 409...) se reenvían tal cual.
        // 401/403 no: el token del usuario es válido, el problema es entre el BFF y el microservicio.
        if (status.is4xxClientError() && status.value() != 401 && status.value() != 403) {
            return ResponseEntity.status(status).body(Map.of("mensaje", extraerMensaje(ex)));
        }
        log.error("Error del microservicio: {} {}", status.value(), ex.getResponseBodyAsString());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("mensaje", "Un servicio interno respondió con error. Intenta de nuevo."));
    }

    // El microservicio no respondió (caído o inaccesible)
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, String>> manejarSinConexion(ResourceAccessException ex) {
        log.error("Microservicio inaccesible: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("mensaje", "Un servicio interno no está disponible en este momento."));
    }

    private String extraerMensaje(RestClientResponseException ex) {
        Matcher m = CAMPO_MENSAJE.matcher(ex.getResponseBodyAsString());
        if (m.find() && !m.group(2).isBlank()) {
            return m.group(2);
        }
        return "La solicitud no se pudo procesar (" + ex.getStatusCode().value() + ")";
    }
}
