package cl.duoc.api.service;

import cl.duoc.api.dto.ResumenReportesResponse;
import cl.duoc.api.repository.ReporteRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReporteService {

    private static final Duration MAXIMO = Duration.ofDays(366);

    private final ReporteRepository reporteRepository;
    private final UsuarioService usuarioService;

    public ReporteService(ReporteRepository reporteRepository, UsuarioService usuarioService) {
        this.reporteRepository = reporteRepository;
        this.usuarioService = usuarioService;
    }

    /**
     * Reportes de un período (desde y hasta en UTC, ISO). Solo admin.
     * Se valida aquí para responder un 400 claro antes de llamar a ms-reportes.
     */
    public ResumenReportesResponse resumen(String oid, String email, String nombre, String desde, String hasta) {
        usuarioService.exigirAdmin(oid, email, nombre);

        LocalDateTime inicio = leer(desde, "desde");
        LocalDateTime fin = leer(hasta, "hasta");
        if (!inicio.isBefore(fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha desde debe ser anterior a hasta");
        }
        if (Duration.between(inicio, fin).compareTo(MAXIMO) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El período no puede superar un año");
        }
        // Se reenvía el texto original ya validado: LocalDateTime.toString() omite los
        // segundos cuando son :00 y cambiaría el formato sin necesidad
        return reporteRepository.resumen(desde.trim(), hasta.trim());
    }

    private static LocalDateTime leer(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta la fecha " + campo);
        }
        try {
            return LocalDateTime.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fecha " + campo + " no válida: usa el formato 2026-11-24T03:00:00");
        }
    }
}
