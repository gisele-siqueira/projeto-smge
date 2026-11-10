package com.smge.smge.sale.dto;

import com.smge.smge.sale.model.ItemVendaModel;

import java.util.UUID;

public record ItemVendaResponse(
        UUID produtoId,
        String produtoCodigo,
        String produtoNome,
        int quantidade,
        float precoUnitario,
        float subtotal
) {

    public static ItemVendaResponse from(ItemVendaModel item) {
        return new ItemVendaResponse(
                item.getProdutoId(),
                item.getProdutoCodigo(),
                item.getProdutoNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal()
        );
    }
}
