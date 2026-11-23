package cl.duoc.api.dto;

public record ProductResponse(
		Long id,
		String name,
		String description,
		double price,
		String categoria,
		String imagenUrl,
		Double precioAnterior) {
}
