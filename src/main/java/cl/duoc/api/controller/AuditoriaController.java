package cl.duoc.api.controller;

import cl.duoc.api.dto.EventoAuditoriaResponse;
import cl.duoc.api.dto.EventoPedidoResponse;
import cl.duoc.api.dto.ResumenReportesResponse;
import cl.duoc.api.service.AuditoriaService;
import cl.duoc.api.service.ReporteService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auditoría y reportes. El admin ve todo; el cliente solo el historial de sus pedidos.
 * El chequeo de rol está en los servicios (403 si no es admin).
 */
@RestController
@RequestMapping("/api")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;
    private final ReporteService reporteService;

    public AuditoriaController(AuditoriaService auditoriaService, ReporteService reporteService) {
        this.auditoriaService = auditoriaService;
        this.reporteService = reporteService;
    }

    @GetMapping("/auditoria")
    public List<EventoAuditoriaResponse> auditoria(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long pedidoId,
            @RequestParam(required = false) String tipoEvento,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        return auditoriaService.listar(
                jwt.getClaimAsString("oid"),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("name"),
                pedidoId, tipoEvento, desde, hasta);
    }

    @GetMapping("/pedidos/{id}/historial")
    public List<EventoPedidoResponse> historial(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return auditoriaService.historialDePedido(
                jwt.getClaimAsString("oid"),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("name"),
                id);
    }

    @GetMapping("/reportes")
    public ResumenReportesResponse reportes(@AuthenticationPrincipal Jwt jwt) {
        return reporteService.resumen(
                jwt.getClaimAsString("oid"),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("name"));
    }
}
