package cl.duoc.api.service;

import cl.duoc.api.dto.ItemCarritoRequest;
import cl.duoc.api.dto.ItemPedidoRequest;
import cl.duoc.api.dto.PedidoRequest;
import cl.duoc.api.dto.PedidoResponse;
import cl.duoc.api.dto.UsuarioResponse;
import cl.duoc.api.dto.ProductResponse;
import cl.duoc.api.repository.PedidoRepository;
import cl.duoc.api.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioService usuarioService;
    private final ProductRepository productRepository;

    private static final int MAX_POR_PRODUCTO = 10;

    // Un cliente solo puede cancelar antes de que el pedido entre a preparación
    private static final Set<String> CANCELABLES_POR_CLIENTE = Set.of("CREADO", "ACEPTADO");

    public PedidoService(PedidoRepository pedidoRepository, UsuarioService usuarioService,
            ProductRepository productRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioService = usuarioService;
        this.productRepository = productRepository;
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
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El carrito está vacío");
        }

        // Nombre y precio salen del catálogo, nunca del navegador
        List<ItemPedidoRequest> items = new ArrayList<>();
        for (ItemCarritoRequest item : request.items()) {
            if (item.productoId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el producto en un item del carrito");
            }
            if (item.cantidad() == null || item.cantidad() < 1 || item.cantidad() > MAX_POR_PRODUCTO) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La cantidad debe estar entre 1 y " + MAX_POR_PRODUCTO);
            }
            ProductResponse producto = productRepository.obtenerPorId(item.productoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El producto " + item.productoId() + " no existe"));
            items.add(new ItemPedidoRequest(producto.id(), producto.name(), producto.price(), item.cantidad()));
        }
        return pedidoRepository.crearPedido(oid, items);
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
