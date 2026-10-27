package com.smge.smge.product.audit;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProductAuditService {
    private final ProductAuditRepository repository;

    public ProductAuditService(ProductAuditRepository repository) {
        this.repository = repository;
    }

    public void registrar(UUID productId, UUID usuarioId, String operacao,
                          String campo, Object anterior, Object novo) {
        if (Objects.equals(anterior, novo)) return;
        ProductAuditModel registro = new ProductAuditModel();
        registro.setProductId(productId);
        registro.setUsuarioId(Objects.requireNonNull(usuarioId, "Usuário obrigatório"));
        registro.setDataAlteracao(LocalDateTime.now());
        registro.setOperacao(operacao);
        registro.setCampoAlterado(campo);
        registro.setValorAnterior(anterior == null ? null : anterior.toString());
        registro.setValorNovo(novo == null ? null : novo.toString());
        repository.save(registro);
    }

    public List<ProductAuditModel> listarPorProduto(UUID id) {
        return repository.findByProductIdOrderByDataAlteracaoDesc(id);
    }
}
