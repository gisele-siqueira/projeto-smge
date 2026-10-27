
package com.smge.smge.product.service;

import com.smge.smge.product.audit.ProductAuditService;
import com.smge.smge.product.model.ProductModel;
import com.smge.smge.product.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductAuditService auditService;

    public ProductService(
            ProductRepository productRepository,
            ProductAuditService auditService) {

        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ProductModel criar(ProductModel product, UUID usuarioId) {

        ProductModel produtoSalvo = productRepository.save(product);

        auditService.registrar(
                produtoSalvo.getProductId(),
                usuarioId,
                "CRIACAO",
                "produto",
                null,
                produtoSalvo.getNome()
        );

        return produtoSalvo;
    }

    public List<ProductModel> listar() {
        return productRepository.findAll();
    }

    public ProductModel buscarPorId(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Produto não encontrado"));
    }

    @Transactional
    public ProductModel atualizar(
            UUID id,
            ProductModel novoProduto,
            UUID usuarioId) {

        ProductModel produto = buscarPorId(id);

        registrarAlteracao(id, usuarioId, "codigo",
                produto.getCodigo(), novoProduto.getCodigo());

        registrarAlteracao(id, usuarioId, "nome",
                produto.getNome(), novoProduto.getNome());

        registrarAlteracao(id, usuarioId, "descricao",
                produto.getDescrição(), novoProduto.getDescrição());

        registrarAlteracao(id, usuarioId, "categoria",
                produto.getCategoria(), novoProduto.getCategoria());

        registrarAlteracao(id, usuarioId, "quantidade",
                produto.getQuantidade(), novoProduto.getQuantidade());

        registrarAlteracao(id, usuarioId, "precoUnitario",
                produto.getPrecoUnitario(), novoProduto.getPrecoUnitario());

        registrarAlteracao(id, usuarioId, "quantidadeMinima",
                produto.getQuantidadeMinima(),
                novoProduto.getQuantidadeMinima());

        registrarAlteracao(id, usuarioId, "unidadeMedida",
                produto.getUnidadeMedida(),
                novoProduto.getUnidadeMedida());

        registrarAlteracao(id, usuarioId, "dataCadastro",
                produto.getDataCadastro(),
                novoProduto.getDataCadastro());

        produto.setCodigo(novoProduto.getCodigo());
        produto.setNome(novoProduto.getNome());
        produto.setDescrição(novoProduto.getDescrição());
        produto.setCategoria(novoProduto.getCategoria());
        produto.setQuantidade(novoProduto.getQuantidade());
        produto.setPrecoUnitario(novoProduto.getPrecoUnitario());
        produto.setQuantidadeMinima(novoProduto.getQuantidadeMinima());
        produto.setUnidadeMedida(novoProduto.getUnidadeMedida());
        produto.setDataCadastro(novoProduto.getDataCadastro());

        return productRepository.save(produto);
    }

    private void registrarAlteracao(
            UUID produtoId,
            UUID usuarioId,
            String campo,
            Object valorAnterior,
            Object valorNovo) {

        auditService.registrar(
                produtoId,
                usuarioId,
                "ATUALIZACAO",
                campo,
                valorAnterior,
                valorNovo
        );
    }

    @Transactional
    public void deletar(UUID id, UUID usuarioId) {

        ProductModel produto = buscarPorId(id);

        auditService.registrar(
                id,
                usuarioId,
                "EXCLUSAO",
                "produto",
                produto.getNome(),
                null
        );

        productRepository.delete(produto);
    }
}
