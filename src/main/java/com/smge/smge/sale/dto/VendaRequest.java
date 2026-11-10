package com.smge.smge.sale.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VendaRequest(
        @NotEmpty(message = "Informe ao menos um item")
        @Valid
        List<ItemVendaRequest> itens
) {
}
