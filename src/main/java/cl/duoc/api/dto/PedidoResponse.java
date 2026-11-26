package cl.duoc.api.dto;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    Long id,
    String usuarioOid,
    Double total,
    LocalDateTime fechaRegistro,
    String estado,
    List<DetallePedidoResponse> detalles,
    // Confirmación de ms-auditoria (vía audit.timeline): null hasta el primer evento auditado
    LocalDateTime auditadoEn,
    String estadoAuditado
) {}