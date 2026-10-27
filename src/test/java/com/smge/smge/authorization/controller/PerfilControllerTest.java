package com.smge.smge.authorization.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.jayway.jsonpath.JsonPath;
import com.smge.smge.ApiTestBase;
import com.smge.smge.authorization.model.Permissao;

class PerfilControllerTest extends ApiTestBase {


    private String perfilJson(String nome, String permissoes) {
        return """
                {"nome": "%s", "descricao": "teste", "permissoes": [%s]}
                """.formatted(nome, permissoes);
    }

    private String criarPerfil(String nome, String permissoes) throws Exception {
        String resposta = mockMvc.perform(post("/api/perfis")
                        .with(como(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson(nome, permissoes)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.perfilId");
    }

    private String idDoAdministrador() throws Exception {
        String resposta = mockMvc.perform(get("/api/perfis").with(como(ADMIN_LOGIN, ADMIN_SENHA)))
                .andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(resposta, "$[?(@.nome == 'Administrador')].perfilId");
        return ids.get(0);
    }

    @Test
    void deveListarPermissoesAgrupadasPorModulo() throws Exception {
        mockMvc.perform(get("/api/permissoes").with(como(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.modulo == 'ESTOQUE')].permissoes[*].codigo")
                        .value(hasItem("PRODUTO_CRIAR")));
    }

    @Test
    void administradorTemTodasAsPermissoesEEhDoSistema() throws Exception {
        mockMvc.perform(get("/api/perfis/" + idDoAdministrador()).with(como(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sistema").value(true))
                .andExpect(jsonPath("$.permissoes.length()").value(
                        Permissao.values().length));
    }

    @Test
    void naoDevePermitirEditarOuExcluirPerfilDoSistema() throws Exception {
        String id = idDoAdministrador();

        mockMvc.perform(put("/api/perfis/" + id)
                        .with(como(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson("Administrador", "\"PRODUTO_VISUALIZAR\"")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/perfis/" + id).with(como(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveCriarEditarEExcluirPerfil() throws Exception {
        String id = criarPerfil("Conferente", "\"PRODUTO_VISUALIZAR\"");

        mockMvc.perform(put("/api/perfis/" + id)
                        .with(como(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson("Conferente", "\"PRODUTO_VISUALIZAR\", \"PRODUTO_EDITAR\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissoes.length()").value(2));

        mockMvc.perform(delete("/api/perfis/" + id).with(como(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isNoContent());
    }

    @Test
    void naoDeveExcluirPerfilEmUso() throws Exception {
        String id = criarPerfil("Vendedor", "\"PRODUTO_VISUALIZAR\"");

        mockMvc.perform(post("/api/users")
                        .with(como(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Vera", "login": "vera", "senha": "Senha123", "perfisIds": ["%s"]}
                                """.formatted(id)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/perfis/" + id).with(como(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRecusarNomeDuplicadoEPermissaoInexistente() throws Exception {
        criarPerfil("Estoque", "\"PRODUTO_VISUALIZAR\"");

        mockMvc.perform(post("/api/perfis")
                        .with(como(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson("estoque", "\"PRODUTO_VISUALIZAR\"")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/perfis")
                        .with(como(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson("Outro", "\"NAO_EXISTE\"")))
                .andExpect(status().isBadRequest());
    }
}
