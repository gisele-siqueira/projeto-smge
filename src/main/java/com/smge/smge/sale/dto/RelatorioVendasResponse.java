package com.smge.smge.sale.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RelatorioVendasResponse(
        LocalDate inicio,
        LocalDate fim,
        int totalVendas,
        int totalItens,
        float receitaTotal,
        List<ItemRelatorio> vendas,
        List<ProdutoRelatorio> produtosMaisVendidos
) {

    public record ItemRelatorio(
            UUID vendaId,
            LocalDateTime dataVenda,
            int quantidadeItens,
            float valorTotal
    ) {
    }

    public record ProdutoRelatorio(
            String produtoCodigo,
            String produtoNome,
            int quantidadeVendida,
            float receitaGerada
    ) {
    }
}
