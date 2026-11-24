package cl.duoc.api.dto;

import java.util.List;

/** Los tres reportes juntos: el panel del admin los carga con una sola llamada. */
public record ResumenReportesResponse(
        KpiResponse kpis,
        List<TopProductoResponse> topProductos,
        List<VentasPorHoraResponse> ventasPorHora) {
}
