package com.smge.smge.sale.service;

import com.smge.smge.common.exception.ConflitoException;
import com.smge.smge.common.exception.RecursoNaoEncontradoException;
import com.smge.smge.sale.model.SaleModel;
import com.smge.smge.sale.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class SaleService {

    private final SaleRepository saleRepository;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional
    public SaleModel criar(SaleModel sale) {
        if (saleRepository.existsByCodigoIgnoreCase(sale.getCodigo())) {
            throw new ConflitoException("Já existe uma venda com esse código");
        }

        if (sale.getDataVenda() == null) {
            sale.setDataVenda(LocalDate.now());
        }

        if (sale.getStatus() == null || sale.getStatus().isBlank()) {
            sale.setStatus("CONCLUIDA");
        }

        return saleRepository.save(sale);
    }

    public List<SaleModel> listar() {
        return saleRepository.findAll();
    }

    public SaleModel buscarPorId(UUID id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda não encontrada"));
    }

    @Transactional
    public SaleModel atualizar(UUID id, SaleModel novaVenda) {
        SaleModel venda = buscarPorId(id);

        if (saleRepository.existsByCodigoIgnoreCaseAndSaleIdNot(novaVenda.getCodigo(), id)) {
            throw new ConflitoException("Já existe uma venda com esse código");
        }

        venda.setCodigo(novaVenda.getCodigo());
        venda.setCliente(novaVenda.getCliente());
        venda.setValorTotal(novaVenda.getValorTotal());
        venda.setFormaPagamento(novaVenda.getFormaPagamento());
        venda.setStatus(novaVenda.getStatus());
        venda.setQuantidadeItens(novaVenda.getQuantidadeItens());
        venda.setDataVenda(novaVenda.getDataVenda());
        venda.setObservacoes(novaVenda.getObservacoes());

        return saleRepository.save(venda);
    }

    @Transactional
    public void deletar(UUID id) {
        SaleModel venda = buscarPorId(id);
        saleRepository.delete(venda);
    }
}
