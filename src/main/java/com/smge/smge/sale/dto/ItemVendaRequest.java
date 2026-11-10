package com.smge.smge.sale.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ItemVendaRequest(
        @NotNull(message = "Informe o produto")
        UUID produtoId,

        @Min(value = 1, message = "Quantidade deve ser maior que zero")
        int quantidade
) {
}
