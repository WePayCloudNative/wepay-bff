package cl.duoc.api.dto;

/**
 * Un evento guardado por ms-auditoria (viene de Kafka, tópico orders.events).
 * La fecha llega como texto ISO (2026-11-24T16:20:05) y se reenvía igual al front.
 *
 * usuarioOid = dueño del pedido; actorOid/actorRol = quién hizo el cambio
 * (ADMIN, CLIENTE o SISTEMA; null en eventos anteriores a este registro).
 * clienteNombre y actorNombre los agrega el BFF consultando ms-clientes.
 */
public record EventoAuditoriaResponse(
        Long id,
        String usuarioOid,
        String tipoEvento,
        String entidadId,
        String fechaEvento,
        String origen,
        String payload,
        String actorOid,
        String actorRol,
        String estadoAnterior,
        String estadoNuevo,
        String clienteNombre,
        String actorNombre) {

    public EventoAuditoriaResponse conNombres(String cliente, String actor) {
        return new EventoAuditoriaResponse(id, usuarioOid, tipoEvento, entidadId, fechaEvento, origen, payload,
                actorOid, actorRol, estadoAnterior, estadoNuevo, cliente, actor);
    }
}
