package cl.duoc.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.duoc.api.dto.EventoAuditoriaResponse;
import cl.duoc.api.dto.EventoPedidoResponse;
import cl.duoc.api.dto.UsuarioResponse;
import cl.duoc.api.repository.AuditoriaRepository;
import cl.duoc.api.repository.UsuarioRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Cada test sigue el patrón preparar → ejecutar → verificar.
 * Los repositorios son mocks: no se llama a ningún microservicio real.
 */
class AuditoriaServiceTest {

    private static final String OID_ADMIN = "oid-admin";
    private static final String OID_ANA = "oid-ana";
    private static final String OID_LUIS = "oid-luis";

    private AuditoriaRepository auditoriaRepository;
    private UsuarioRepository usuarioRepository;
    private AuditoriaService servicio;

    @BeforeEach
    void preparar() {
        auditoriaRepository = mock(AuditoriaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        // UsuarioService es real: así se prueba también la regla del rol
        servicio = new AuditoriaService(auditoriaRepository, new UsuarioService(usuarioRepository));

        when(usuarioRepository.buscarPorOid(OID_ADMIN, null, null))
                .thenReturn(new UsuarioResponse(OID_ADMIN, "Admin", "admin@duoc.cl", "ADMIN", null));
        when(usuarioRepository.buscarPorOid(OID_ANA, null, null))
                .thenReturn(new UsuarioResponse(OID_ANA, "Ana", "ana@duoc.cl", "USER", null));
    }

    private static EventoAuditoriaResponse evento(long id, String oid, String tipo, String fecha) {
        return new EventoAuditoriaResponse(id, oid, tipo, "15", fecha, "wepay-ms-pedidos", "{}");
    }

    @Test
    void unClienteNoPuedeVerLaBitacora() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listar(OID_ANA, null, null, null, null, null, null));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(auditoriaRepository);
    }

    @Test
    void elAdminVeLosEventosMasRecientesPrimero() {
        when(auditoriaRepository.obtenerTodos()).thenReturn(List.of(
                evento(1, OID_ANA, "OrderCreated", "2026-11-24T10:00:00"),
                evento(2, OID_ANA, "OrderAccepted", "2026-11-24T10:05:00")));

        List<EventoAuditoriaResponse> resultado =
                servicio.listar(OID_ADMIN, null, null, null, null, null, null);

        assertEquals(List.of(2L, 1L), resultado.stream().map(EventoAuditoriaResponse::id).toList());
    }

    @Test
    void conTipoYFechasUsaElFiltroDeAuditoria() {
        when(auditoriaRepository.filtrar("OrderCancelled", "2026-11-24T00:00:00", "2026-11-24T23:59:59"))
                .thenReturn(List.of(evento(3, OID_ANA, "OrderCancelled", "2026-11-24T12:00:00")));

        List<EventoAuditoriaResponse> resultado = servicio.listar(OID_ADMIN, null, null, null,
                "OrderCancelled", "2026-11-24T00:00:00", "2026-11-24T23:59:59");

        assertEquals(1, resultado.size());
        verify(auditoriaRepository, never()).obtenerTodos();
    }

    @Test
    void rechazaUnRangoConSoloUnaFecha() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listar(OID_ADMIN, null, null, null, null, "2026-11-24T00:00:00", null));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(auditoriaRepository, never()).filtrar(any(), any(), any());
    }

    @Test
    void rechazaUnRangoAlReves() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listar(OID_ADMIN, null, null, null, null,
                        "2026-11-25T00:00:00", "2026-11-24T00:00:00"));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void rechazaUnTipoConCaracteresRaros() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.listar(OID_ADMIN, null, null, null, "Order'; DROP", null, null));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void elClienteVeElHistorialDeSuPedidoEnOrdenCronologico() {
        when(auditoriaRepository.obtenerPorPedido(15L)).thenReturn(List.of(
                evento(2, OID_ANA, "OrderAccepted", "2026-11-24T10:05:00"),
                evento(1, OID_ANA, "OrderCreated", "2026-11-24T10:00:00")));

        List<EventoPedidoResponse> historial = servicio.historialDePedido(OID_ANA, null, null, 15L);

        assertEquals(List.of("OrderCreated", "OrderAccepted"),
                historial.stream().map(EventoPedidoResponse::tipoEvento).toList());
    }

    @Test
    void elClienteNoPuedeVerElHistorialDeUnPedidoAjeno() {
        when(auditoriaRepository.obtenerPorPedido(15L))
                .thenReturn(List.of(evento(1, OID_LUIS, "OrderCreated", "2026-11-24T10:00:00")));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.historialDePedido(OID_ANA, null, null, 15L));

        // 404 y no 403: así no se revela que el pedido existe
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void elAdminVeElHistorialDeCualquierPedido() {
        when(auditoriaRepository.obtenerPorPedido(15L))
                .thenReturn(List.of(evento(1, OID_LUIS, "OrderCreated", "2026-11-24T10:00:00")));

        assertEquals(1, servicio.historialDePedido(OID_ADMIN, null, null, 15L).size());
    }

    @Test
    void unPedidoSinEventosDevuelveHistorialVacio() {
        when(auditoriaRepository.obtenerPorPedido(99L)).thenReturn(null);

        assertEquals(List.of(), servicio.historialDePedido(OID_ANA, null, null, 99L));
    }
}
