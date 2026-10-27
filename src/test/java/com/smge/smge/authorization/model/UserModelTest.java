package com.smge.smge.authorization.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Set;

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

    @Test
    void permissoesEfetivasDevemSomarPerfisEExtras() {

        PerfilModel faturamento = new PerfilModel();
        faturamento.getPermissoes().add(Permissao.USUARIO_VISUALIZAR);

        UserModel usuario = new UserModel();
        usuario.getPerfis().add(faturamento);
        usuario.getPermissoesExtras().add(Permissao.PRODUTO_VISUALIZAR);

        assertEquals(
                Set.of(Permissao.USUARIO_VISUALIZAR, Permissao.PRODUTO_VISUALIZAR),
                usuario.permissoesEfetivas());
    }
}
