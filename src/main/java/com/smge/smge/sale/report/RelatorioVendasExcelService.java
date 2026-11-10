package com.smge.smge.sale.report;

import com.smge.smge.sale.dto.RelatorioVendasResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

@Service
public class RelatorioVendasExcelService {

    public byte[] gerar(RelatorioVendasResponse relatorio) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {

            CellStyle estiloCabecalho = criarEstiloCabecalho(workbook);

            escreverResumo(workbook, estiloCabecalho, relatorio);
            escreverVendas(workbook, estiloCabecalho, relatorio);
            escreverProdutos(workbook, estiloCabecalho, relatorio);

            workbook.write(saida);
            return saida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private CellStyle criarEstiloCabecalho(Workbook workbook) {
        Font fonte = workbook.createFont();
        fonte.setBold(true);
        CellStyle estilo = workbook.createCellStyle();
        estilo.setFont(fonte);
        return estilo;
    }

    private void escreverResumo(Workbook workbook, CellStyle estiloCabecalho, RelatorioVendasResponse relatorio) {
        Sheet aba = workbook.createSheet("Resumo");
        int linha = 0;
        linha = escreverLinha(aba, linha, estiloCabecalho, "Relatório de vendas");
        linha = escreverLinha(aba, linha, null, "Período", relatorio.inicio() + " a " + relatorio.fim());
        linha = escreverLinha(aba, linha, null, "Total de vendas", String.valueOf(relatorio.totalVendas()));
        linha = escreverLinha(aba, linha, null, "Itens vendidos", String.valueOf(relatorio.totalItens()));
        escreverLinha(aba, linha, null, "Receita total", String.format("%.2f", relatorio.receitaTotal()));
        aba.autoSizeColumn(0);
        aba.autoSizeColumn(1);
    }

    private void escreverVendas(Workbook workbook, CellStyle estiloCabecalho, RelatorioVendasResponse relatorio) {
        Sheet aba = workbook.createSheet("Vendas");
        int linha = escreverCabecalhoTabela(aba, estiloCabecalho, "Data", "Itens", "Valor total");

        for (var venda : relatorio.vendas()) {
            Row row = aba.createRow(linha++);
            row.createCell(0).setCellValue(venda.dataVenda().toString());
            row.createCell(1).setCellValue(venda.quantidadeItens());
            row.createCell(2).setCellValue(venda.valorTotal());
        }
        for (int coluna = 0; coluna < 3; coluna++) {
            aba.autoSizeColumn(coluna);
        }
    }

    private void escreverProdutos(Workbook workbook, CellStyle estiloCabecalho, RelatorioVendasResponse relatorio) {
        Sheet aba = workbook.createSheet("Produtos mais vendidos");
        int linha = escreverCabecalhoTabela(
                aba, estiloCabecalho, "Código", "Produto", "Quantidade vendida", "Receita gerada");

        for (var produto : relatorio.produtosMaisVendidos()) {
            Row row = aba.createRow(linha++);
            row.createCell(0).setCellValue(produto.produtoCodigo());
            row.createCell(1).setCellValue(produto.produtoNome());
            row.createCell(2).setCellValue(produto.quantidadeVendida());
            row.createCell(3).setCellValue(produto.receitaGerada());
        }
        for (int coluna = 0; coluna < 4; coluna++) {
            aba.autoSizeColumn(coluna);
        }
    }

    private int escreverCabecalhoTabela(Sheet aba, CellStyle estilo, String... colunas) {
        Row row = aba.createRow(0);
        for (int c = 0; c < colunas.length; c++) {
            Cell cell = row.createCell(c);
            cell.setCellValue(colunas[c]);
            cell.setCellStyle(estilo);
        }
        return 1;
    }

    private int escreverLinha(Sheet aba, int linha, CellStyle estilo, String... valores) {
        Row row = aba.createRow(linha);
        for (int c = 0; c < valores.length; c++) {
            Cell cell = row.createCell(c);
            cell.setCellValue(valores[c]);
            if (estilo != null) {
                cell.setCellStyle(estilo);
            }
        }
        return linha + 1;
    }
}
