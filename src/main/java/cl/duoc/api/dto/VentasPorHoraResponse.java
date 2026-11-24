package cl.duoc.api.dto;

/** Pedidos entregados agrupados por hora (últimas 24 horas). */
public record VentasPorHoraResponse(
        String hora,
        Long cantidadPedidos,
        Double totalVentas) {
}
