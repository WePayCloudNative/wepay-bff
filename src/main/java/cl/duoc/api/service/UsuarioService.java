package cl.duoc.api.service;

import cl.duoc.api.dto.DireccionDespacho;
import cl.duoc.api.dto.UsuarioResponse;
import cl.duoc.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponse obtenerPerfil(String oid, String email, String nombre) {
        return usuarioRepository.buscarPorOid(oid, email, nombre);
    }

    /** Se asegura de que el usuario exista y guarda su dirección (ms-clientes la valida). */
    public UsuarioResponse guardarDireccion(String oid, String email, String nombre, DireccionDespacho direccion) {
        usuarioRepository.buscarPorOid(oid, email, nombre);
        return usuarioRepository.guardarDireccion(oid, direccion);
    }
}
