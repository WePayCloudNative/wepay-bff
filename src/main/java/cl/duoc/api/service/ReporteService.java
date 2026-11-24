package cl.duoc.api.service;

import cl.duoc.api.dto.ResumenReportesResponse;
import cl.duoc.api.repository.ReporteRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final UsuarioService usuarioService;

    public ReporteService(ReporteRepository reporteRepository, UsuarioService usuarioService) {
        this.reporteRepository = reporteRepository;
        this.usuarioService = usuarioService;
    }

    /** KPIs (24 h), productos más vendidos (7 días) y ventas por hora (24 h). Solo admin. */
    public ResumenReportesResponse resumen(String oid, String email, String nombre) {
        usuarioService.exigirAdmin(oid, email, nombre);

        var top = reporteRepository.topProductos();
        var porHora = reporteRepository.ventasPorHora();
        return new ResumenReportesResponse(
                reporteRepository.kpis(),
                top == null ? List.of() : top,
                porHora == null ? List.of() : porHora);
    }
}
