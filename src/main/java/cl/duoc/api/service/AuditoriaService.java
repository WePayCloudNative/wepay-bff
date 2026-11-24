package cl.duoc.api.service;

import cl.duoc.api.dto.EventoAuditoriaResponse;
import cl.duoc.api.dto.EventoPedidoResponse;
import cl.duoc.api.dto.UsuarioResponse;
import cl.duoc.api.repository.AuditoriaRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuditoriaService {

    // Los tipos son nombres como OrderCreated u OrderCancelled: solo letras
    private static final Pattern TIPO_VALIDO = Pattern.compile("^[A-Za-z]{1,40}$");

    private static final Comparator<EventoAuditoriaResponse> POR_FECHA = Comparator.comparing(
            EventoAuditoriaResponse::fechaEvento, Comparator.nullsFirst(Comparator.naturalOrder()));

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioService usuarioService;

    public AuditoriaService(AuditoriaRepository auditoriaRepository, UsuarioService usuarioService) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioService = usuarioService;
    }

    /**
     * Bitácora completa para el admin, con los más recientes primero.
     * Todos los filtros son opcionales; desde y hasta van juntos (ISO: 2026-11-24T00:00:00).
     */
    public List<EventoAuditoriaResponse> listar(String oid, String email, String nombre,
            Long pedidoId, String tipoEvento, String desde, String hasta) {
        usuarioService.exigirAdmin(oid, email, nombre);

        String tipo = vacioANull(tipoEvento);
        String inicio = vacioANull(desde);
        String fin = vacioANull(hasta);
        validarFiltros(tipo, inicio, fin);

        List<EventoAuditoriaResponse> eventos;
        if (pedidoId != null) {
            eventos = sinNulos(auditoriaRepository.obtenerPorPedido(pedidoId));
            if (tipo != null) {
                eventos = eventos.stream().filter(e -> tipo.equals(e.tipoEvento())).toList();
            }
        } else if (tipo != null || inicio != null) {
            eventos = sinNulos(auditoriaRepository.filtrar(tipo, inicio, fin));
        } else {
            eventos = sinNulos(auditoriaRepository.obtenerTodos());
        }
        return eventos.stream().sorted(POR_FECHA.reversed()).toList();
    }

    /**
     * Historial de un pedido en orden cronológico.
     * El cliente solo ve el de sus propios pedidos: si es de otro, 404 (no se revela que existe).
     */
    public List<EventoPedidoResponse> historialDePedido(String oid, String email, String nombre, Long pedidoId) {
        UsuarioResponse usuario = usuarioService.obtenerPerfil(oid, email, nombre);
        List<EventoAuditoriaResponse> eventos = sinNulos(auditoriaRepository.obtenerPorPedido(pedidoId));

        List<EventoAuditoriaResponse> visibles = UsuarioService.esAdmin(usuario)
                ? eventos
                : eventos.stream().filter(e -> oid != null && oid.equals(e.usuarioOid())).toList();

        if (!eventos.isEmpty() && visibles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado");
        }
        return visibles.stream().sorted(POR_FECHA).map(EventoPedidoResponse::desde).toList();
    }

    private static void validarFiltros(String tipo, String desde, String hasta) {
        if (tipo != null && !TIPO_VALIDO.matcher(tipo).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de evento no válido");
        }
        if ((desde == null) != (hasta == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica ambas fechas: desde y hasta");
        }
        if (desde != null) {
            LocalDateTime inicio = leerFecha(desde);
            LocalDateTime fin = leerFecha(hasta);
            if (inicio.isAfter(fin)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La fecha desde no puede ser posterior a hasta");
            }
        }
    }

    private static LocalDateTime leerFecha(String texto) {
        try {
            return LocalDateTime.parse(texto);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fecha no válida: usa el formato 2026-11-24T00:00:00");
        }
    }

    private static String vacioANull(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private static <T> List<T> sinNulos(List<T> lista) {
        return lista == null ? List.of() : lista;
    }
}
