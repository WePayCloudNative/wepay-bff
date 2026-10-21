package cl.duoc.api.controller;

import cl.duoc.api.dto.UsuarioResponse;
import cl.duoc.api.dto.ProductResponse;
import cl.duoc.api.dto.PedidoRequest;
import cl.duoc.api.dto.PedidoResponse;
import cl.duoc.api.service.ProductService;
import cl.duoc.api.service.UsuarioService;
import cl.duoc.api.service.PedidoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DataController {

    private final UsuarioService usuarioService;
    private final ProductService productService;
    private final PedidoService pedidoService;

    public DataController(UsuarioService usuarioService, ProductService productService, PedidoService pedidoService) {
        this.usuarioService = usuarioService;
        this.productService = productService;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/perfil")
    public UsuarioResponse perfil(@AuthenticationPrincipal Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        String email = jwt.getClaimAsString("preferred_username");
        String nombre = jwt.getClaimAsString("name");

        return usuarioService.obtenerPerfil(oid, email, nombre);
    }

    @GetMapping("/productos")
    public List<ProductResponse> productos() {
        return productService.obtenerProductos();
    }

    @GetMapping("/pedidos")
    public List<PedidoResponse> obtenerPedidos(@AuthenticationPrincipal Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        String email = jwt.getClaimAsString("preferred_username");
        String nombre = jwt.getClaimAsString("name");

        return pedidoService.obtenerPedidos(oid, email, nombre);
    }

    @PostMapping("/pedidos")
    public PedidoResponse crearPedido(@AuthenticationPrincipal Jwt jwt, @RequestBody PedidoRequest request) {
        String oid = jwt.getClaimAsString("oid");
        return pedidoService.crearPedido(oid, request);
    }

    @PutMapping("/pedidos/{id}/status")
    public PedidoResponse actualizarEstado(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return pedidoService.actualizarEstado(
                jwt.getClaimAsString("oid"),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("name"),
                id, body.get("status"));
    }
}