package com.smge.smge.sale.controller;

import com.smge.smge.sale.dto.RelatorioVendasResponse;
import com.smge.smge.sale.report.RelatorioVendasExcelService;
import com.smge.smge.sale.report.RelatorioVendasPdfService;
import com.smge.smge.sale.service.VendaRelatorioService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/sales/report")
public class RelatorioVendasController {

    private static final String EXCEL_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final VendaRelatorioService relatorioService;
    private final RelatorioVendasExcelService excelService;
    private final RelatorioVendasPdfService pdfService;

    public RelatorioVendasController(
            VendaRelatorioService relatorioService,
            RelatorioVendasExcelService excelService,
            RelatorioVendasPdfService pdfService) {

        this.relatorioService = relatorioService;
        this.excelService = excelService;
        this.pdfService = pdfService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDA_VISUALIZAR')")
    public RelatorioVendasResponse gerar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        return relatorioService.gerar(inicio, fim);
    }

    @GetMapping(value = "/excel", produces = EXCEL_MEDIA_TYPE)
    @PreAuthorize("hasAuthority('VENDA_VISUALIZAR')")
    public ResponseEntity<byte[]> excel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        byte[] arquivo = excelService.gerar(relatorioService.gerar(inicio, fim));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-vendas.xlsx\"")
                .body(arquivo);
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAuthority('VENDA_VISUALIZAR')")
    public ResponseEntity<byte[]> pdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        byte[] arquivo = pdfService.gerar(relatorioService.gerar(inicio, fim));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-vendas.pdf\"")
                .body(arquivo);
    }
}
