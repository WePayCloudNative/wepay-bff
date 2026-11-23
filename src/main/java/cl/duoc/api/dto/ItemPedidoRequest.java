package cl.duoc.api.dto;

// Lo que el BFF envía a ms-pedidos, con nombre y precio tomados del catálogo
public record ItemPedidoRequest(
    Long productoId,
    String nombre,
    Double precio,
    Integer cantidad
) {}
