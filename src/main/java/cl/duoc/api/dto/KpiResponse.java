package cl.duoc.api.dto;

import java.util.Map;

/** KPIs de las últimas 24 horas que calcula ms-reportes. */
public record KpiResponse(
        long totalPedidos,
        long pedidosEntregados,
        long pedidosActivos,
        double ventasTotales,
        double leadTimePromedioMinutos,
        Map<String, Long> pedidosPorEstado) {
}
