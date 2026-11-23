package cl.duoc.api.dto;

/** Dirección de despacho guardada en ms-clientes. Teléfono en formato +569XXXXXXXX. */
public record DireccionDespacho(
        String direccion,
        String comuna,
        String region,
        String telefono) {
}
