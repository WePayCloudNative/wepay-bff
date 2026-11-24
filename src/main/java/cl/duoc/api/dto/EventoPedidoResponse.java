package cl.duoc.api.dto;

/**
 * Lo que ve el cliente del historial de su pedido: qué pasó y cuándo.
 * No incluye el payload ni el OID: eso es información técnica para el admin.
 */
public record EventoPedidoResponse(
        String tipoEvento,
        String fecha) {

    public static EventoPedidoResponse desde(EventoAuditoriaResponse evento) {
        return new EventoPedidoResponse(evento.tipoEvento(), evento.fechaEvento());
    }
}
