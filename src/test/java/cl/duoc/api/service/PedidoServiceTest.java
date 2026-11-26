package cl.duoc.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.api.dto.ItemCarritoRequest;
import cl.duoc.api.dto.ItemPedidoRequest;
import cl.duoc.api.dto.PedidoRequest;
import cl.duoc.api.dto.PedidoResponse;
import cl.duoc.api.dto.ProductResponse;
import cl.duoc.api.repository.PedidoRepository;
import cl.duoc.api.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Reglas de negocio del carrito y de la cancelación por el cliente. */
class PedidoServiceTest {

    private PedidoRepository pedidoRepository;
    private ProductRepository productRepository;
    private PedidoService servicio;

    @BeforeEach
    void preparar() {
        pedidoRepository = mock(PedidoRepository.class);
        productRepository = mock(ProductRepository.class);
        servicio = new PedidoService(pedidoRepository, mock(UsuarioService.class), productRepository);
    }

    private static ProductResponse teclado() {
        return new ProductResponse(1L, "Teclado", "Mecánico", 89990, "Periféricos",
                "/img/productos/teclado.jpg", 99990.0);
    }

    private void esperarError(HttpStatus estado, Runnable accion) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, accion::run);
        assertEquals(estado, error.getStatusCode());
    }

    @Test
    void elPrecioSaleDelCatalogoYNoDelNavegador() {
        when(productRepository.obtenerPorId(1L)).thenReturn(Optional.of(teclado()));

        servicio.crearPedido("oid-ana", new PedidoRequest(List.of(new ItemCarritoRequest(1L, 2))));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ItemPedidoRequest>> items = ArgumentCaptor.forClass(List.class);
        verify(pedidoRepository).crearPedido(eq("oid-ana"), items.capture());
        ItemPedidoRequest item = items.getValue().get(0);
        assertEquals("Teclado", item.nombre());
        assertEquals(89990.0, item.precio());
        assertEquals(2, item.cantidad());
    }

    @Test
    void rechazaUnCarritoVacio() {
        esperarError(HttpStatus.BAD_REQUEST,
                () -> servicio.crearPedido("oid-ana", new PedidoRequest(List.of())));
        verify(pedidoRepository, never()).crearPedido(anyString(), any());
    }

    @Test
    void rechazaMasDeDiezUnidadesDeUnProducto() {
        esperarError(HttpStatus.BAD_REQUEST, () -> servicio.crearPedido("oid-ana",
                new PedidoRequest(List.of(new ItemCarritoRequest(1L, 11)))));
    }

    @Test
    void rechazaCantidadCero() {
        esperarError(HttpStatus.BAD_REQUEST, () -> servicio.crearPedido("oid-ana",
                new PedidoRequest(List.of(new ItemCarritoRequest(1L, 0)))));
    }

    @Test
    void rechazaUnProductoQueNoExiste() {
        when(productRepository.obtenerPorId(anyLong())).thenReturn(Optional.empty());

        esperarError(HttpStatus.BAD_REQUEST, () -> servicio.crearPedido("oid-ana",
                new PedidoRequest(List.of(new ItemCarritoRequest(999L, 1)))));
        verify(pedidoRepository, never()).crearPedido(anyString(), any());
    }

    @Test
    void elClienteNoPuedeCancelarUnPedidoAjeno() {
        when(pedidoRepository.obtenerPorUsuario("oid-ana")).thenReturn(List.of());

        esperarError(HttpStatus.NOT_FOUND, () -> servicio.cancelarPorCliente("oid-ana", 15L));
        verify(pedidoRepository, never()).actualizarEstado(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void noSePuedeCancelarUnPedidoDespachado() {
        when(pedidoRepository.obtenerPorUsuario("oid-ana")).thenReturn(List.of(
                new PedidoResponse(15L, "oid-ana", 89990.0, null, "DESPACHADO", List.of())));

        esperarError(HttpStatus.CONFLICT, () -> servicio.cancelarPorCliente("oid-ana", 15L));
    }

    @Test
    void sePuedeCancelarUnPedidoRecienCreado() {
        when(pedidoRepository.obtenerPorUsuario("oid-ana")).thenReturn(List.of(
                new PedidoResponse(15L, "oid-ana", 89990.0, null, "CREADO", List.of())));

        servicio.cancelarPorCliente("oid-ana", 15L);

        // Queda registrado que lo canceló el propio cliente (para auditoría)
        verify(pedidoRepository).actualizarEstado(15L, "CANCELADO", "oid-ana", "CLIENTE");
    }
}
