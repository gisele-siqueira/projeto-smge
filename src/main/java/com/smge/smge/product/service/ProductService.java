package com.smge.smge.product.service;

import com.smge.smge.common.exception.ConflitoException;
import com.smge.smge.common.exception.RecursoNaoEncontradoException;
import com.smge.smge.product.model.ProductModel;
import com.smge.smge.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductModel criar(ProductModel product) {
        if (productRepository.existsByCodigoIgnoreCase(product.getCodigo())) {
            throw new ConflitoException("Já existe um produto com esse código");
        }
        if (productRepository.existsByNomeIgnoreCase(product.getNome())) {
            throw new ConflitoException("Já existe um produto com esse nome");
        }
        return productRepository.save(product);
    }

    public List<ProductModel> listar() {
        return productRepository.findAll();
    }

    public ProductModel buscarPorId(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
    }

    public ProductModel atualizar(UUID id, ProductModel novoProduto) {
        ProductModel produto = buscarPorId(id);

        if (productRepository.existsByCodigoIgnoreCaseAndProductIdNot(novoProduto.getCodigo(), id)) {
            throw new ConflitoException("Já existe um produto com esse código");
        }
        if (productRepository.existsByNomeIgnoreCaseAndProductIdNot(novoProduto.getNome(), id)) {
            throw new ConflitoException("Já existe um produto com esse nome");
        }

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

    public void deletar(UUID id) {
        ProductModel produto = buscarPorId(id);
        productRepository.delete(produto);
    }
}