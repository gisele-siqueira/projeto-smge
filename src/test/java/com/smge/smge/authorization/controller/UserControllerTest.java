package com.smge.smge.authorization.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

/**
 * Testa o fluxo completo (segurança + permissões + validação + banco H2).
 * Usa o administrador criado pelo AdminSeeder (admin / Admin@123).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_SENHA = "Admin@123";
    private static final String SENHA = "Senha123";

    @Autowired
    private MockMvc mockMvc;

    private String criarPerfil(String nome, String... permissoes) throws Exception {
        String body = """
                {"nome": "%s", "permissoes": [%s]}
                """.formatted(nome, aspas(permissoes));

        String resposta = mockMvc.perform(post("/perfis")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(resposta, "$.perfilId");
    }

    private String criarUsuario(String login, List<String> perfisIds, String... extras) throws Exception {
        String body = """
                {"nome": "%s", "login": "%s", "senha": "%s", "perfisIds": [%s], "permissoesExtras": [%s]}
                """.formatted(login, login, SENHA, aspas(perfisIds.toArray(String[]::new)), aspas(extras));

        String resposta = mockMvc.perform(post("/users")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(resposta, "$.userId");
    }

    private static String aspas(String... valores) {
        return String.join(", ", Arrays.stream(valores).map(v -> "\"" + v + "\"").toList());
    }

    private String jsonUsuario(String nome, String login, String senha) {
        return """
                {"nome": "%s", "login": "%s", "senha": "%s"}
                """.formatted(nome, login, senha);
    }

    @Test
    void adminDeveCriarUsuarioSemExporSenha() throws Exception {
        mockMvc.perform(post("/users")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUsuario("Maria", "maria", SENHA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("maria"))
                .andExpect(jsonPath("$.permissoes").isEmpty())
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void semAutenticacaoDeveRetornar401() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUsuario("Maria", "maria", SENHA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioSemPermissaoNaoPodeCriarUsuariosMasVeOsPropriosDados() throws Exception {
        criarUsuario("comum", List.of());

        mockMvc.perform(post("/users")
                        .with(httpBasic("comum", SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUsuario("Outro", "outro", SENHA)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/users/me").with(httpBasic("comum", SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("comum"));
    }

    @Test
    void perfilDaAcessoApenasAoQueContem() throws Exception {
        String estoquista = criarPerfil("Estoquista", "PRODUTO_VISUALIZAR", "PRODUTO_CRIAR");
        criarUsuario("carlos", List.of(estoquista));

        mockMvc.perform(get("/products").with(httpBasic("carlos", SENHA)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/products/" + UUID.randomUUID()).with(httpBasic("carlos", SENHA)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/users").with(httpBasic("carlos", SENHA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void permissaoExtraLiberaSoAquelaFuncionalidade() throws Exception {
        // ex.: funcionária do faturamento que só pode consultar produtos
        criarUsuario("ana", List.of(), "PRODUTO_VISUALIZAR");

        mockMvc.perform(get("/products").with(httpBasic("ana", SENHA)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/products")
                        .with(httpBasic("ana", SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void alterarAcessosTemEfeitoNoProximoLogin() throws Exception {
        String id = criarUsuario("bruno", List.of());

        mockMvc.perform(get("/products").with(httpBasic("bruno", SENHA)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/users/" + id + "/acessos")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"perfisIds": [], "permissoesExtras": ["PRODUTO_VISUALIZAR"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissoes[0]").value("PRODUTO_VISUALIZAR"));

        mockMvc.perform(get("/products").with(httpBasic("bruno", SENHA)))
                .andExpect(status().isOk());
    }

    @Test
    void gerenteNaoPodeCriarUsuarioComMaisAcessosQueEle() throws Exception {
        criarUsuario("gerente", List.of(), "USUARIO_GERENCIAR");

        mockMvc.perform(post("/users")
                        .with(httpBasic("gerente", SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "X", "login": "xavier", "senha": "Senha123", "permissoesExtras": ["PRODUTO_EXCLUIR"]}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void desativadoNaoConsegueLogar() throws Exception {
        String id = criarUsuario("davi", List.of());

        mockMvc.perform(patch("/users/" + id + "/desativar").with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/me").with(httpBasic("davi", SENHA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar409ParaLoginDuplicado() throws Exception {
        mockMvc.perform(post("/users")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUsuario("Admin 2", "ADMIN", SENHA)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400ParaDadosInvalidos() throws Exception {
        mockMvc.perform(post("/users")
                        .with(httpBasic(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUsuario("", "a b", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.login").exists())
                .andExpect(jsonPath("$.erros.senha").exists());
    }
}
