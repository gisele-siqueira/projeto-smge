package com.smge.smge;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

/**
 * Base para testes de API: sobe a aplicação com banco H2 e faz login
 * de verdade em POST /auth/login para obter o token.
 * Cada teste roda em uma transação desfeita ao final.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class ApiTestBase {

    // administrador criado pelo AdminSeeder
    protected static final String ADMIN_LOGIN = "admin";
    protected static final String ADMIN_SENHA = "Admin@123";

    @Autowired
    protected MockMvc mockMvc;

    /**
     * Faz login e devolve o token de acesso.
     */
    protected String token(String login, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login": "%s", "senha": "%s"}
                                """.formatted(login, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(resposta, "$.accessToken");
    }

    /**
     * Envia a requisição autenticada com o token informado.
     */
    protected static RequestPostProcessor bearer(String token) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }

    /**
     * Faz login e envia a requisição autenticada como esse usuário.
     */
    protected RequestPostProcessor como(String login, String senha) throws Exception {
        return bearer(token(login, senha));
    }
}
