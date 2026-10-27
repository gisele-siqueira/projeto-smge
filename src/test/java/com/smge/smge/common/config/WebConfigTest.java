package com.smge.smge.common.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.smge.smge.ApiTestBase;

/**
 * Usa o index.html de src/test/resources/static no lugar do build do React.
 */
class WebConfigTest extends ApiTestBase {

    @Test
    void raizEncaminhaParaOFrontEnd() throws Exception {
        // o Spring Boot faz forward de "/" para o index.html (o MockMvc não executa o forward)
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));
    }

    @Test
    void rotasDoReactEntreganOIndexParaOF5Funcionar() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FRONT-SMGE")));

        mockMvc.perform(get("/perfis/editar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FRONT-SMGE")));
    }

    @Test
    void arquivoInexistenteDa404() throws Exception {
        mockMvc.perform(get("/assets/nao-existe.js"))
                .andExpect(status().isNotFound());
    }

    @Test
    void apiExigeTokenERotaInexistenteDaApiNaoDevolveOFront() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/nao-existe").with(como(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rotasAntigasSemPrefixoNaoSaoMaisAApi() throws Exception {
        // /users agora é uma tela do front, não a API
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FRONT-SMGE")));
    }
}
