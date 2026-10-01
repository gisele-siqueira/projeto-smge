package com.smge.smge.authorization.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.smge.smge.authorization.model.Permissao;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PerfilControllerTest {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_SENHA = "Admin@123";

    @Autowired
    private MockMvc mockMvc;

    private String perfilJson(String nome, String permissoes) {
        return """
                {"nome": "%s", "descricao": "teste", "permissoes": [%s]}
                """.formatted(nome, permissoes);
    }

    private String criarPerfil(String nome, String permissoes) throws Exception {
        String resposta = mockMvc.perform(post("/perfis")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson(nome, permissoes)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.perfilId");
    }

    private String idDoAdministrador() throws Exception {
        String resposta = mockMvc.perform(get("/perfis").with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA)))
                .andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(resposta, "$[?(@.nome == 'Administrador')].perfilId");
        return ids.get(0);
    }

    @Test
    void deveListarPermissoesAgrupadasPorModulo() throws Exception {
        mockMvc.perform(get("/permissoes").with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.modulo == 'ESTOQUE')].permissoes[*].codigo")
                        .value(hasItem("PRODUTO_CRIAR")));
    }

    @Test
    void administradorTemTodasAsPermissoesEEhDoSistema() throws Exception {
        mockMvc.perform(get("/perfis/" + idDoAdministrador()).with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sistema").value(true))
                .andExpect(jsonPath("$.permissoes.length()").value(
                        Permissao.values().length));
    }

    @Test
    void naoDevePermitirEditarOuExcluirPerfilDoSistema() throws Exception {
        String id = idDoAdministrador();

        mockMvc.perform(put("/perfis/" + id)
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson("Administrador", "\"PRODUTO_VISUALIZAR\"")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/perfis/" + id).with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveCriarEditarEExcluirPerfil() throws Exception {
        String id = criarPerfil("Conferente", "\"PRODUTO_VISUALIZAR\"");

        mockMvc.perform(put("/perfis/" + id)
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson("Conferente", "\"PRODUTO_VISUALIZAR\", \"PRODUTO_EDITAR\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissoes.length()").value(2));

        mockMvc.perform(delete("/perfis/" + id).with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isNoContent());
    }

    @Test
    void naoDeveExcluirPerfilEmUso() throws Exception {
        String id = criarPerfil("Vendedor", "\"PRODUTO_VISUALIZAR\"");

        mockMvc.perform(post("/users")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Vera", "login": "vera", "senha": "Senha123", "perfisIds": ["%s"]}
                                """.formatted(id)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/perfis/" + id).with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRecusarNomeDuplicadoEPermissaoInexistente() throws Exception {
        criarPerfil("Estoque", "\"PRODUTO_VISUALIZAR\"");

        mockMvc.perform(post("/perfis")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson("estoque", "\"PRODUTO_VISUALIZAR\"")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/perfis")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson("Outro", "\"NAO_EXISTE\"")))
                .andExpect(status().isBadRequest());
    }
}
