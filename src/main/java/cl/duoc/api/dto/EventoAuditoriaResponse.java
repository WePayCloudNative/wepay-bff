package cl.duoc.api.dto;

/**
 * Un evento guardado por ms-auditoria (viene de Kafka, tópico orders.events).
 * La fecha llega como texto ISO (2026-11-24T16:20:05) y se reenvía igual al front.
 */
public record EventoAuditoriaResponse(
        Long id,
        String usuarioOid,
        String tipoEvento,
        String entidadId,
        String fechaEvento,
        String origen,
        String payload) {
}
