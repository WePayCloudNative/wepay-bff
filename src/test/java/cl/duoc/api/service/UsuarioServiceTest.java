package cl.duoc.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cl.duoc.api.dto.UsuarioResponse;
import cl.duoc.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

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
}
