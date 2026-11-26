package cl.duoc.api.dto;

/**
 * Lo que ve el cliente del historial de su pedido: qué pasó, cuándo y si lo hizo él o la tienda.
 * No incluye el payload ni los OID: eso es información técnica para el admin.
 *
 * hechoPor: "TIENDA", "CLIENTE" o null si el evento es anterior a este registro.
 */
public record EventoPedidoResponse(
        String tipoEvento,
        String fecha,
        String hechoPor) {

    public static EventoPedidoResponse desde(EventoAuditoriaResponse evento) {
        String hechoPor = null;
        if ("ADMIN".equals(evento.actorRol())) {
            hechoPor = "TIENDA";
        } else if ("CLIENTE".equals(evento.actorRol())) {
            hechoPor = "CLIENTE";
        }
        return new EventoPedidoResponse(evento.tipoEvento(), evento.fechaEvento(), hechoPor);
    }
}
