package com.smge.smge.sale.service;

import com.smge.smge.authorization.service.UserService;
import com.smge.smge.common.exception.RecursoNaoEncontradoException;
import com.smge.smge.common.exception.RegraNegocioException;
import com.smge.smge.product.model.ProductModel;
import com.smge.smge.product.service.ProductService;
import com.smge.smge.sale.dto.ItemVendaRequest;
import com.smge.smge.sale.dto.VendaRequest;
import com.smge.smge.sale.dto.VendaResponse;
import com.smge.smge.sale.model.ItemVendaModel;
import com.smge.smge.sale.model.VendaModel;
import com.smge.smge.sale.repository.VendaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ProductService productService;
    private final UserService userService;

    public VendaService(
            VendaRepository vendaRepository,
            ProductService productService,
            UserService userService) {

        this.vendaRepository = vendaRepository;
        this.productService = productService;
        this.userService = userService;
    }

    @Transactional
    public VendaResponse criar(VendaRequest request) {

        VendaModel venda = new VendaModel();
        venda.setStatus(VendaModel.CONCLUIDA);
        venda.setUsuarioId(userService.idDoUsuarioLogado());

        float total = 0f;

        for (ItemVendaRequest itemRequest : request.itens()) {

            ProductModel produto = productService.baixarEstoque(itemRequest.produtoId(), itemRequest.quantidade());

            ItemVendaModel item = new ItemVendaModel();
            item.setVenda(venda);
            item.setProdutoId(produto.getProductId());
            item.setProdutoCodigo(produto.getCodigo());
            item.setProdutoNome(produto.getNome());
            item.setQuantidade(itemRequest.quantidade());
            item.setPrecoUnitario(produto.getPrecoUnitario());
            item.setSubtotal(produto.getPrecoUnitario() * itemRequest.quantidade());

            venda.getItens().add(item);
            total += item.getSubtotal();
        }

        venda.setValorTotal(total);

        return VendaResponse.from(vendaRepository.save(venda));
    }

    @Transactional(readOnly = true)
    public List<VendaResponse> listar() {
        return vendaRepository.findAllByOrderByDataVendaDesc().stream()
                .map(VendaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VendaResponse buscarPorId(UUID id) {
        return VendaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public VendaResponse cancelar(UUID id) {

        VendaModel venda = buscarEntidade(id);

        if (VendaModel.CANCELADA.equals(venda.getStatus())) {
            throw new RegraNegocioException("Venda já está cancelada");
        }

        for (ItemVendaModel item : venda.getItens()) {
            try {
                productService.reporEstoque(item.getProdutoId(), item.getQuantidade());
            } catch (RecursoNaoEncontradoException ignorada) {
                // produto foi excluído do catálogo depois da venda: não há onde repor
            }
        }

        venda.setStatus(VendaModel.CANCELADA);
        return VendaResponse.from(vendaRepository.save(venda));
    }

    // usado só pelo relatório (mesmo pacote): expõe as entidades, não o DTO resumido
    @Transactional(readOnly = true)
    List<VendaModel> listarEntre(LocalDateTime inicio, LocalDateTime fim) {
        return vendaRepository.findByDataVendaBetweenOrderByDataVendaDesc(inicio, fim);
    }

    private VendaModel buscarEntidade(UUID id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda não encontrada"));
    }
}
