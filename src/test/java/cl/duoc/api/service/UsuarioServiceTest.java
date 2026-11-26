package cl.duoc.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cl.duoc.api.dto.UsuarioResponse;
import cl.duoc.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class UsuarioServiceTest {

    @Test
    void obtenerPerfilDebeConsultarUsuarioPorOid() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        UsuarioResponse esperado = new UsuarioResponse("oid-123", "ana", "ana@duoc.cl", "user", null);
        when(usuarioRepository.buscarPorOid("oid-123", "ana@duoc.cl", "user")).thenReturn(esperado);

        UsuarioService usuarioService = new UsuarioService(usuarioRepository);
        UsuarioResponse resultado = usuarioService.obtenerPerfil("oid-123", "ana@duoc.cl", "user");

        assertEquals(esperado, resultado);
    }

    @Test
    void exigirAdminDejaPasarAlAdmin() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        UsuarioResponse admin = new UsuarioResponse("oid-1", "Jose", "jose@duoc.cl", "ADMIN", null);
        when(usuarioRepository.buscarPorOid("oid-1", null, null)).thenReturn(admin);

        assertEquals(admin, new UsuarioService(usuarioRepository).exigirAdmin("oid-1", null, null));
    }

    @Test
    void exigirAdminRechazaAUnCliente() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        when(usuarioRepository.buscarPorOid("oid-2", null, null))
                .thenReturn(new UsuarioResponse("oid-2", "Ana", "ana@duoc.cl", "USER", null));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> new UsuarioService(usuarioRepository).exigirAdmin("oid-2", null, null));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void nombreDeUsaElCorreoSiNoHayNombre() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        when(usuarioRepository.obtener("oid-3"))
                .thenReturn(java.util.Optional.of(new UsuarioResponse("oid-3", " ", "luis@duoc.cl", "USER", null)));

        assertEquals(java.util.Optional.of("luis@duoc.cl"), new UsuarioService(usuarioRepository).nombreDe("oid-3"));
    }

    @Test
    void nombreDeNoFallaSiMsClientesNoResponde() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        when(usuarioRepository.obtener("oid-4")).thenThrow(new RuntimeException("caído"));

        assertEquals(java.util.Optional.empty(), new UsuarioService(usuarioRepository).nombreDe("oid-4"));
    }
}
