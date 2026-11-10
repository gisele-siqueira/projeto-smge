package com.smge.smge.sale.service;

import com.smge.smge.sale.dto.RelatorioVendasResponse;
import com.smge.smge.sale.model.ItemVendaModel;
import com.smge.smge.sale.model.VendaModel;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VendaRelatorioService {

    private final VendaService vendaService;

    public VendaRelatorioService(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    public RelatorioVendasResponse gerar(LocalDate inicio, LocalDate fim) {

        List<VendaModel> vendas = vendaService.listarEntre(inicio.atStartOfDay(), fim.atTime(LocalTime.MAX)).stream()
                .filter(venda -> !VendaModel.CANCELADA.equals(venda.getStatus()))
                .toList();

        List<RelatorioVendasResponse.ItemRelatorio> itensRelatorio = vendas.stream()
                .map(venda -> new RelatorioVendasResponse.ItemRelatorio(
                        venda.getVendaId(),
                        venda.getDataVenda(),
                        venda.getItens().stream().mapToInt(ItemVendaModel::getQuantidade).sum(),
                        venda.getValorTotal()))
                .toList();

        // chave: código do produto -> [quantidade vendida, receita gerada]
        Map<String, float[]> porProduto = new LinkedHashMap<>();
        Map<String, String> nomePorCodigo = new LinkedHashMap<>();

        for (VendaModel venda : vendas) {
            for (ItemVendaModel item : venda.getItens()) {
                float[] acumulado = porProduto.computeIfAbsent(item.getProdutoCodigo(), codigo -> new float[2]);
                acumulado[0] += item.getQuantidade();
                acumulado[1] += item.getSubtotal();
                nomePorCodigo.put(item.getProdutoCodigo(), item.getProdutoNome());
            }
        }

        List<RelatorioVendasResponse.ProdutoRelatorio> produtosMaisVendidos = porProduto.entrySet().stream()
                .map(entrada -> new RelatorioVendasResponse.ProdutoRelatorio(
                        entrada.getKey(),
                        nomePorCodigo.get(entrada.getKey()),
                        (int) entrada.getValue()[0],
                        entrada.getValue()[1]))
                .sorted(Comparator.comparingInt(RelatorioVendasResponse.ProdutoRelatorio::quantidadeVendida).reversed())
                .toList();

        int totalItens = vendas.stream()
                .flatMap(venda -> venda.getItens().stream())
                .mapToInt(ItemVendaModel::getQuantidade)
                .sum();

        float receitaTotal = (float) vendas.stream()
                .mapToDouble(VendaModel::getValorTotal)
                .sum();

        return new RelatorioVendasResponse(
                inicio, fim, vendas.size(), totalItens, receitaTotal, itensRelatorio, produtosMaisVendidos);
    }
}
