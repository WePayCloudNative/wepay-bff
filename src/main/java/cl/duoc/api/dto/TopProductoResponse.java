package cl.duoc.api.dto;

/** Producto más vendido en los últimos 7 días (solo pedidos entregados). */
public record TopProductoResponse(
        Long productoId,
        String nombreProducto,
        Long cantidadVendida,
        Double ingresosTotales) {
}
