package cl.duoc.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.duoc.api.dto.ResumenReportesResponse;
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

    private static final String DESDE = "2026-11-24T03:00:00";
    private static final String HASTA = "2026-11-25T03:00:00";

    private ReporteRepository reporteRepository;
    private ReporteService servicio;

    @BeforeEach
    void preparar() {
        reporteRepository = mock(ReporteRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        servicio = new ReporteService(reporteRepository, new UsuarioService(usuarioRepository));

        when(usuarioRepository.buscarPorOid("oid-admin", null, null))
                .thenReturn(new UsuarioResponse("oid-admin", "Admin", "admin@duoc.cl", "ADMIN", null));
        when(usuarioRepository.buscarPorOid("oid-ana", null, null))
                .thenReturn(new UsuarioResponse("oid-ana", "Ana", "ana@duoc.cl", "USER", null));
    }

    private void esperarError(HttpStatus estado, Runnable accion) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, accion::run);
        assertEquals(estado, error.getStatusCode());
    }

    @Test
    void unClienteNoPuedeVerLosReportes() {
        esperarError(HttpStatus.FORBIDDEN, () -> servicio.resumen("oid-ana", null, null, DESDE, HASTA));
        verifyNoInteractions(reporteRepository);
    }

    @Test
    void elAdminRecibeElResumenDelPeriodo() {
        ResumenReportesResponse esperado = new ResumenReportesResponse(DESDE, HASTA, "hora", null, null,
                Map.of(), List.of(), List.of());
        when(reporteRepository.resumen(DESDE, HASTA)).thenReturn(esperado);

        assertSame(esperado, servicio.resumen("oid-admin", null, null, DESDE, HASTA));
    }

    @Test
    void exigeAmbasFechas() {
        esperarError(HttpStatus.BAD_REQUEST, () -> servicio.resumen("oid-admin", null, null, DESDE, " "));
        verify(reporteRepository, never()).resumen(anyString(), anyString());
    }

    @Test
    void rechazaFechasMalEscritas() {
        esperarError(HttpStatus.BAD_REQUEST, () -> servicio.resumen("oid-admin", null, null, "ayer", HASTA));
    }

    @Test
    void rechazaUnRangoAlReves() {
        esperarError(HttpStatus.BAD_REQUEST, () -> servicio.resumen("oid-admin", null, null, HASTA, DESDE));
    }

    @Test
    void rechazaPeriodosDeMasDeUnAno() {
        esperarError(HttpStatus.BAD_REQUEST,
                () -> servicio.resumen("oid-admin", null, null, "2024-01-01T00:00:00", HASTA));
    }
}
