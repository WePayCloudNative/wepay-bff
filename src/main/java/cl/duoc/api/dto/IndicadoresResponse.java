package cl.duoc.api.dto;

/** Indicadores de un período (ms-reportes). Confirmados = pago aceptado en adelante. */
public record IndicadoresResponse(
        long totalPedidos,
        long pedidosConfirmados,
        long pedidosEntregados,
        long pedidosActivos,
        long pedidosCancelados,
        double ventasConfirmadas,
        double ventasEntregadas,
        double montoCancelado,
        double ticketPromedio,
        double tasaCancelacion,
        double leadTimePromedioMinutos) {
}
