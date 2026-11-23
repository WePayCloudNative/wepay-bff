package cl.duoc.api.dto;

public record DetallePedidoResponse(
    Long productoId,
    String nombreProducto,
    Double precioUnitario,
    Integer cantidad
) {}
