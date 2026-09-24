package com.smge.smge.authorization.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserModelTest {

    @Test
    void deveDefinirSenhaComExpiracaoDe90Dias() {

        UserModel usuario = new UserModel();

        usuario.definirSenha("Senha123");

        assertEquals("Senha123", usuario.getSenha());
        assertNotNull(usuario.getSenhaExpiraEm());
        assertFalse(usuario.senhaExpirada());
    }

    @Test
    void deveIdentificarSenhaExpirada() {

        UserModel usuario = new UserModel();

        usuario.setSenha("Senha123");
        usuario.setSenhaExpiraEm(LocalDateTime.now().minusDays(1));

        assertTrue(usuario.senhaExpirada());
    }
}
