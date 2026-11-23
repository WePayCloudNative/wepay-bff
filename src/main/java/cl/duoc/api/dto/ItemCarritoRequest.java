package cl.duoc.api.dto;

// Lo que manda el front: solo qué producto y cuántos.
// El nombre y el precio los pone el BFF desde el catálogo, para que no se puedan manipular.
public record ItemCarritoRequest(
    Long productoId,
    Integer cantidad
) {}
