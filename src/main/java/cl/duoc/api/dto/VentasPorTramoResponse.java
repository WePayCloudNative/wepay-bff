package cl.duoc.api.dto;

/** Ventas confirmadas de una hora o un día. inicio en UTC sin zona. */
public record VentasPorTramoResponse(
        String inicio,
        long pedidos,
        double ventas) {
}
