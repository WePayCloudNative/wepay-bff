package cl.duoc.api.dto;

public record UsuarioResponse(
        String oid,
        String nombre,
        String email,
        String rol,
        // null si el usuario aún no guarda una dirección
        DireccionDespacho direccion
    ) {
}
