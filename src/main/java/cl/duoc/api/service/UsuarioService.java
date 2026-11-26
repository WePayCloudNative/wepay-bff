package cl.duoc.api.service;

import cl.duoc.api.dto.DireccionDespacho;
import cl.duoc.api.dto.UsuarioResponse;
import cl.duoc.api.repository.UsuarioRepository;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    /** El rol lo decide ms-clientes, nunca el front. */
    public static boolean esAdmin(UsuarioResponse usuario) {
        return usuario != null && "ADMIN".equalsIgnoreCase(usuario.rol());
    }

    /** Devuelve el perfil si es admin; si no, responde 403. */
    public UsuarioResponse exigirAdmin(String oid, String email, String nombre) {
        UsuarioResponse usuario = obtenerPerfil(oid, email, nombre);
        if (!esAdmin(usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un administrador puede ver esta información");
        }
        return usuario;
    }

    /**
     * Nombre para mostrar (nombre o, si no tiene, correo). Vacío si no existe o si
     * ms-clientes no responde: mostrar un nombre es un extra, no debe romper la auditoría.
     */
    public Optional<String> nombreDe(String oid) {
        if (oid == null || oid.isBlank()) {
            return Optional.empty();
        }
        try {
            return usuarioRepository.obtener(oid)
                    .map(u -> u.nombre() != null && !u.nombre().isBlank() ? u.nombre() : u.email());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
