package com.smge.smge.authorization.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testa o fluxo completo (segurança + validação + banco H2).
 * Usa o ADMIN criado pelo AdminSeeder (admin / Admin@123).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_SENHA = "Admin@123";

    @Autowired
    private MockMvc mockMvc;

    private String json(String nome, String login, String senha) {
        return """
                {"nome": "%s", "login": "%s", "senha": "%s"}
                """.formatted(nome, login, senha);
    }

    @Test
    void adminDeveCriarUsuarioSemExporSenha() throws Exception {
        mockMvc.perform(post("/users")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Maria", "maria", "Senha123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("maria"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void semAutenticacaoDeveRetornar401() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Maria", "maria", "Senha123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioComumNaoPodeCriarUsuarios() throws Exception {
        mockMvc.perform(post("/users")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Comum", "comum", "Senha123")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users")
                        .with(httpBasic("comum", "Senha123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Outro", "outro", "Senha123")))
                .andExpect(status().isForbidden());

        // mas pode consultar os próprios dados
        mockMvc.perform(get("/users/me").with(httpBasic("comum", "Senha123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("comum"));
    }

    @Test
    void deveRetornar409ParaLoginDuplicado() throws Exception {
        mockMvc.perform(post("/users")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Admin 2", "ADMIN", "Senha123")))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400ParaDadosInvalidos() throws Exception {
        mockMvc.perform(post("/users")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "a b", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.login").exists())
                .andExpect(jsonPath("$.erros.senha").exists());
    }
}
