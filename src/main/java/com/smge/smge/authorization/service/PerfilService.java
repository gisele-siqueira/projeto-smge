package com.smge.smge.authorization.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smge.smge.authorization.dto.PerfilRequest;
import com.smge.smge.authorization.dto.PerfilResponse;
import com.smge.smge.authorization.model.PerfilModel;
import com.smge.smge.authorization.repository.PerfilRepository;
import com.smge.smge.authorization.repository.UserRepository;
import com.smge.smge.authorization.security.UsuarioLogado;
import com.smge.smge.common.exception.ConflitoException;
import com.smge.smge.common.exception.RecursoNaoEncontradoException;
import com.smge.smge.common.exception.RegraNegocioException;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final UserRepository userRepository;

    public PerfilService(
            PerfilRepository perfilRepository,
            UserRepository userRepository
    ) {
        this.perfilRepository = perfilRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PerfilResponse> listarPerfis() {
        return perfilRepository.findAll().stream()
                .map(PerfilResponse::from)
                .sorted(Comparator.comparing(PerfilResponse::nome))
                .toList();
    }

    @Transactional(readOnly = true)
    public PerfilResponse buscarPorId(UUID id) {
        return PerfilResponse.from(buscarEntidade(id));
    }

    @Transactional
    public PerfilResponse criarPerfil(PerfilRequest request) {

        String nome = request.getNome().trim();

        if (perfilRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe um perfil com esse nome");
        }

        UsuarioLogado.exigirPodeConceder(request.getPermissoes());

        PerfilModel perfil = new PerfilModel();
        perfil.setNome(nome);
        perfil.setDescricao(request.getDescricao());
        perfil.getPermissoes().addAll(request.getPermissoes());

        return PerfilResponse.from(perfilRepository.save(perfil));
    }

    @Transactional
    public PerfilResponse atualizarPerfil(UUID id, PerfilRequest request) {

        PerfilModel perfil = buscarEntidade(id);
        exigirEditavel(perfil);

        String nome = request.getNome().trim();

        if (perfilRepository.existsByNomeIgnoreCaseAndPerfilIdNot(nome, id)) {
            throw new ConflitoException("Já existe um perfil com esse nome");
        }

        // precisa ter as permissões atuais (para poder retirá-las) e as novas (para concedê-las)
        UsuarioLogado.exigirPodeGerenciar(perfil.getPermissoes());
        UsuarioLogado.exigirPodeConceder(request.getPermissoes());

        perfil.setNome(nome);
        perfil.setDescricao(request.getDescricao());
        perfil.getPermissoes().clear();
        perfil.getPermissoes().addAll(request.getPermissoes());

        return PerfilResponse.from(perfil);
    }

    @Transactional
    public void excluirPerfil(UUID id) {

        PerfilModel perfil = buscarEntidade(id);
        exigirEditavel(perfil);
        UsuarioLogado.exigirPodeGerenciar(perfil.getPermissoes());

        if (userRepository.existsByPerfis_PerfilId(id)) {
            throw new ConflitoException("Perfil está atribuído a usuários. Remova-o dos usuários antes de excluir");
        }

        perfilRepository.delete(perfil);
    }

    /**
     * Busca os perfis pelos ids, falhando se algum não existir.
     */
    @Transactional(readOnly = true)
    public Set<PerfilModel> buscarPerfis(Collection<UUID> ids) {

        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        List<PerfilModel> encontrados = perfilRepository.findAllById(ids);

        if (encontrados.size() != new HashSet<>(ids).size()) {
            throw new RecursoNaoEncontradoException("Perfil não encontrado");
        }

        return new HashSet<>(encontrados);
    }

    private PerfilModel buscarEntidade(UUID id) {
        return perfilRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil não encontrado"));
    }

    private void exigirEditavel(PerfilModel perfil) {
        if (perfil.isSistema()) {
            throw new RegraNegocioException("Perfis do sistema não podem ser alterados ou excluídos");
        }
    }
}
