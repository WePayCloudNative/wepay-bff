package cl.duoc.api.dto;

import java.util.List;
import java.util.Map;

/**
 * Panel de reportes de un período, tal como lo calcula ms-reportes.
 * anterior = período de igual largo justo antes (para comparar).
 * agrupacion = "hora" o "dia" (cómo vienen los tramos de ventasPorTramo).
 */
public record ResumenReportesResponse(
        String desde,
        String hasta,
        String agrupacion,
        IndicadoresResponse actual,
        IndicadoresResponse anterior,
        Map<String, Long> pedidosPorEstado,
        List<TopProductoResponse> topProductos,
        List<VentasPorTramoResponse> ventasPorTramo) {
}
