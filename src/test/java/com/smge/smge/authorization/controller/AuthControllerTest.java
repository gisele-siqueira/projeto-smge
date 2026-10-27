package com.smge.smge.authorization.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.smge.smge.ApiTestBase;
import com.smge.smge.authorization.model.UserModel;
import com.smge.smge.authorization.repository.UserRepository;

class AuthControllerTest extends ApiTestBase {

    @Autowired
    private UserRepository userRepository;

    private String loginJson(String login, String senha) {
        return """
                {"login": "%s", "senha": "%s"}
                """.formatted(login, senha);
    }

    @Test
    void loginDevolveTokenEDadosDoUsuario() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("ADMIN", ADMIN_SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiraEm").exists())
                .andExpect(jsonPath("$.usuario.login").value("admin"))
                .andExpect(jsonPath("$.usuario.permissoes").isNotEmpty())
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());
    }

    @Test
    void tokenDaAcessoAsRotasProtegidas() throws Exception {
        mockMvc.perform(get("/api/users/me").with(bearer(token(ADMIN_LOGIN, ADMIN_SENHA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("admin"));
    }

    @Test
    void senhaErradaRetorna401ComMensagemGenerica() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(ADMIN_LOGIN, "errada123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Login ou senha inválidos"));

        // usuário inexistente recebe a mesma mensagem (não revela quais logins existem)
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("ninguem", "errada123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Login ou senha inválidos"));
    }

    @Test
    void senhaExpiradaRetornaCodigoProprio() throws Exception {
        UserModel admin = userRepository.findByLogin(ADMIN_LOGIN).orElseThrow();
        admin.setSenhaExpiraEm(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("SENHA_EXPIRADA"));
    }

    @Test
    void semTokenOuComTokenInvalidoRetorna401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/me").with(bearer("token.falso.123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void trocarASenhaInvalidaOsTokensAntigos() throws Exception {
        String tokenAntigo = token(ADMIN_LOGIN, ADMIN_SENHA);

        // garante que o novo login aconteça em outro segundo (o token guarda o horário em segundos)
        UserModel admin = userRepository.findByLogin(ADMIN_LOGIN).orElseThrow();
        admin.setSenhaAlteradaEm(LocalDateTime.now().plusSeconds(1));

        mockMvc.perform(get("/api/users/me").with(bearer(tokenAntigo)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void trocarAPropriaSenhaPeloEndpoint() throws Exception {
        mockMvc.perform(put("/api/users/me/senha")
                        .with(como(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senhaAtual": "Admin@123", "novaSenha": "NovaSenha456"}
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(ADMIN_LOGIN, "NovaSenha456")))
                .andExpect(status().isOk());
    }

    @Test
    void corsLiberaOFrontEndConfigurado() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://site-malicioso.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
