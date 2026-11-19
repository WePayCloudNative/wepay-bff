package cl.duoc.api.service;

import cl.duoc.api.dto.PedidoRequest;
import cl.duoc.api.dto.PedidoResponse;
import cl.duoc.api.dto.UsuarioResponse;
import cl.duoc.api.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioService usuarioService;

    // Un cliente solo puede cancelar antes de que el pedido entre a preparación
    private static final Set<String> CANCELABLES_POR_CLIENTE = Set.of("CREADO", "ACEPTADO");

    public PedidoService(PedidoRepository pedidoRepository, UsuarioService usuarioService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioService = usuarioService;
    }

    public List<PedidoResponse> obtenerPedidos(String oid, String email, String nombre) {
        // 1. Preguntamos al ms-usuario qué rol tiene esta persona (y lo crea si no
        // existe)
        UsuarioResponse usuario = usuarioService.obtenerPerfil(oid, email, nombre);

        // 2. Tomamos la decisión
        if ("ADMIN".equalsIgnoreCase(usuario.rol())) {
            // Si es admin, le devolvemos todos los pedidos de la base de datos
            return pedidoRepository.obtenerTodos();
        } else {
            // Si es un usuario normal, solo le devolvemos los suyos
            return pedidoRepository.obtenerPorUsuario(oid);
        }
    }

    public PedidoResponse crearPedido(String oid, PedidoRequest request) {
        return pedidoRepository.crearPedido(oid, request);
    }

    public PedidoResponse actualizarEstado(String oid, String email, String nombre, Long id, String status) {
        UsuarioResponse usuario = usuarioService.obtenerPerfil(oid, email, nombre);
        if (!"ADMIN".equalsIgnoreCase(usuario.rol())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un administrador puede cambiar el estado");
        }
        return pedidoRepository.actualizarEstado(id, status);
    }

    public PedidoResponse cancelarPorCliente(String oid, Long id) {
        // 1. Solo buscamos entre SUS pedidos: si el id es de otro usuario, responde 404
        //    (así no se revela que ese pedido existe)
        List<PedidoResponse> propios = pedidoRepository.obtenerPorUsuario(oid);
        PedidoResponse pedido = (propios == null ? List.<PedidoResponse>of() : propios).stream()
                .filter(p -> id.equals(p.id()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        // 2. Solo se puede cancelar en CREADO o ACEPTADO
        String estado = pedido.estado() == null ? "" : pedido.estado().toUpperCase();
        if (!CANCELABLES_POR_CLIENTE.contains(estado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pedido está " + estado + " y ya no se puede cancelar");
        }

        // 3. Mismo camino que el admin: ms-pedidos guarda y publica pedido.estado.cancelado
        return pedidoRepository.actualizarEstado(id, "CANCELADO");
    }
}
