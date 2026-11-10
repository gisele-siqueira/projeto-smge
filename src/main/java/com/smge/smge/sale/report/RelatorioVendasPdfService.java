package com.smge.smge.sale.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.smge.smge.sale.dto.RelatorioVendasResponse;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class RelatorioVendasPdfService {

    private static final Font FONTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font FONTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONTE_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font FONTE_CABECALHO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] gerar(RelatorioVendasResponse relatorio) {
        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(documento, saida);
            documento.open();

            documento.add(new Paragraph("Relatório de vendas", FONTE_TITULO));
            documento.add(new Paragraph("Período: " + relatorio.inicio() + " a " + relatorio.fim(), FONTE_TEXTO));
            documento.add(new Paragraph(" "));

            documento.add(tabelaResumo(relatorio));
            documento.add(new Paragraph(" "));

            documento.add(new Paragraph("Vendas no período", FONTE_SUBTITULO));
            documento.add(tabelaVendas(relatorio));
            documento.add(new Paragraph(" "));

            documento.add(new Paragraph("Produtos mais vendidos", FONTE_SUBTITULO));
            documento.add(tabelaProdutos(relatorio));

            documento.close();
            return saida.toByteArray();
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Erro ao gerar o PDF do relatório", e);
        }
    }

    private PdfPTable tabelaResumo(RelatorioVendasResponse relatorio) {
        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(100);
        adicionarLinhaSemBorda(tabela, "Total de vendas", String.valueOf(relatorio.totalVendas()));
        adicionarLinhaSemBorda(tabela, "Itens vendidos", String.valueOf(relatorio.totalItens()));
        adicionarLinhaSemBorda(tabela, "Receita total", String.format("R$ %.2f", relatorio.receitaTotal()));
        return tabela;
    }

    private PdfPTable tabelaVendas(RelatorioVendasResponse relatorio) {
        PdfPTable tabela = new PdfPTable(3);
        tabela.setWidthPercentage(100);
        adicionarCabecalho(tabela, "Data", "Itens", "Valor total");
        for (var venda : relatorio.vendas()) {
            tabela.addCell(celula(venda.dataVenda().format(FORMATO_DATA_HORA)));
            tabela.addCell(celula(String.valueOf(venda.quantidadeItens())));
            tabela.addCell(celula(String.format("R$ %.2f", venda.valorTotal())));
        }
        return tabela;
    }

    private PdfPTable tabelaProdutos(RelatorioVendasResponse relatorio) {
        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);
        adicionarCabecalho(tabela, "Código", "Produto", "Quantidade", "Receita");
        for (var produto : relatorio.produtosMaisVendidos()) {
            tabela.addCell(celula(produto.produtoCodigo()));
            tabela.addCell(celula(produto.produtoNome()));
            tabela.addCell(celula(String.valueOf(produto.quantidadeVendida())));
            tabela.addCell(celula(String.format("R$ %.2f", produto.receitaGerada())));
        }
        return tabela;
    }

    private void adicionarCabecalho(PdfPTable tabela, String... colunas) {
        for (String coluna : colunas) {
            PdfPCell celula = new PdfPCell(new Phrase(coluna, FONTE_CABECALHO));
            celula.setHorizontalAlignment(Element.ALIGN_LEFT);
            tabela.addCell(celula);
        }
    }

    private void adicionarLinhaSemBorda(PdfPTable tabela, String rotulo, String valor) {
        PdfPCell rotuloCell = new PdfPCell(new Phrase(rotulo, FONTE_CABECALHO));
        rotuloCell.setBorder(0);
        PdfPCell valorCell = new PdfPCell(new Phrase(valor, FONTE_TEXTO));
        valorCell.setBorder(0);
        tabela.addCell(rotuloCell);
        tabela.addCell(valorCell);
    }

    private PdfPCell celula(String texto) {
        return new PdfPCell(new Phrase(texto, FONTE_TEXTO));
    }
}
