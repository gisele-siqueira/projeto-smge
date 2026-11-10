package com.smge.smge.sale.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.jayway.jsonpath.JsonPath;
import com.smge.smge.ApiTestBase;

class SaleControllerTest extends ApiTestBase {

    private String vendaJson(String codigo, String cliente, float total) {
        return """
                {
                    "codigo": "%s",
                    "cliente": "%s",
                    "valorTotal": %s,
                    "formaPagamento": "PIX",
                    "status": "CONCLUIDA",
                    "quantidadeItens": 2,
                    "dataVenda": "2026-11-05",
                    "observacoes": "Primeira venda"
                }
                """.formatted(codigo, cliente, String.valueOf(total));
    }

    private String criarVenda(String codigo, String cliente, float total) throws Exception {
        String resposta = mockMvc.perform(post("/api/sales")
                        .with(como(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vendaJson(codigo, cliente, total)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.saleId");
    }

    @Test
    void deveCriarEBuscarVendaPorId() throws Exception {
        String id = criarVenda("VND-001", "Cliente Teste", 150.50f);

        mockMvc.perform(get("/api/sales/" + id).with(como(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("VND-001"))
                .andExpect(jsonPath("$.cliente").value("Cliente Teste"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    void deveListarVendas() throws Exception {
        criarVenda("VND-002", "Cliente A", 100.0f);
        criarVenda("VND-003", "Cliente B", 200.0f);

        mockMvc.perform(get("/api/sales").with(como(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void naoDeveCriarVendaComCodigoDuplicado() throws Exception {
        criarVenda("VND-004", "Cliente C", 50.0f);

        mockMvc.perform(post("/api/sales")
                        .with(como(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vendaJson("VND-004", "Outro Cliente", 80.0f)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveAtualizarVenda() throws Exception {
        String id = criarVenda("VND-005", "Cliente Original", 90.0f);

        String atualizacao = """
                {
                    "codigo": "VND-005",
                    "cliente": "Cliente Atualizado",
                    "valorTotal": 120.0,
                    "formaPagamento": "CARTAO_CREDITO",
                    "status": "CONCLUIDA",
                    "quantidadeItens": 3,
                    "dataVenda": "2026-11-05",
                    "observacoes": "Atualizado com sucesso"
                }
                """;

        mockMvc.perform(put("/api/sales/" + id)
                        .with(como(ADMIN_LOGIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Cliente Atualizado"))
                .andExpect(jsonPath("$.quantidadeItens").value(3));
    }

    @Test
    void deveExcluirVenda() throws Exception {
        String id = criarVenda("VND-006", "Cliente Excluir", 30.0f);

        mockMvc.perform(delete("/api/sales/" + id).with(como(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/sales/" + id).with(como(ADMIN_LOGIN, ADMIN_SENHA)))
                .andExpect(status().isNotFound());
    }
}
