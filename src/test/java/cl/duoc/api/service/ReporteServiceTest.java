package cl.duoc.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.duoc.api.dto.KpiResponse;
import cl.duoc.api.dto.ResumenReportesResponse;
import cl.duoc.api.dto.TopProductoResponse;
import cl.duoc.api.dto.UsuarioResponse;
import cl.duoc.api.repository.ReporteRepository;
import cl.duoc.api.repository.UsuarioRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ReporteServiceTest {

    private ReporteRepository reporteRepository;
    private UsuarioRepository usuarioRepository;
    private ReporteService servicio;

    @BeforeEach
    void preparar() {
        reporteRepository = mock(ReporteRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        servicio = new ReporteService(reporteRepository, new UsuarioService(usuarioRepository));
    }

    @Test
    void unClienteNoPuedeVerLosReportes() {
        when(usuarioRepository.buscarPorOid("oid-ana", null, null))
                .thenReturn(new UsuarioResponse("oid-ana", "Ana", "ana@duoc.cl", "USER", null));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.resumen("oid-ana", null, null));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(reporteRepository);
    }

    @Test
    void elAdminRecibeLosTresReportesJuntos() {
        when(usuarioRepository.buscarPorOid("oid-admin", null, null))
                .thenReturn(new UsuarioResponse("oid-admin", "Admin", "admin@duoc.cl", "ADMIN", null));
        KpiResponse kpis = new KpiResponse(3, 1, 2, 99990.0, 42.0, Map.of("ENTREGADO", 1L, "CREADO", 2L));
        List<TopProductoResponse> top = List.of(new TopProductoResponse(1L, "Teclado", 2L, 199980.0));
        when(reporteRepository.kpis()).thenReturn(kpis);
        when(reporteRepository.topProductos()).thenReturn(top);
        when(reporteRepository.ventasPorHora()).thenReturn(null);

        ResumenReportesResponse resumen = servicio.resumen("oid-admin", null, null);

        assertEquals(kpis, resumen.kpis());
        assertEquals(top, resumen.topProductos());
        // Si ms-reportes no devuelve nada, el front recibe una lista vacía y no null
        assertEquals(List.of(), resumen.ventasPorHora());
    }
}
