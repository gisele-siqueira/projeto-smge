package com.smge.smge.sale.dto;

import com.smge.smge.sale.model.VendaModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record VendaResponse(
        UUID vendaId,
        LocalDateTime dataVenda,
        String status,
        float valorTotal,
        List<ItemVendaResponse> itens
) {

    public static VendaResponse from(VendaModel venda) {
        return new VendaResponse(
                venda.getVendaId(),
                venda.getDataVenda(),
                venda.getStatus(),
                venda.getValorTotal(),
                venda.getItens().stream().map(ItemVendaResponse::from).toList()
        );
    }
}
