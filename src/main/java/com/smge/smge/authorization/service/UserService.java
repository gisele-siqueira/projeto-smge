package com.smge.smge.authorization.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smge.smge.authorization.dto.ChangePasswordRequest;
import com.smge.smge.authorization.dto.CreateUserRequest;
import com.smge.smge.authorization.dto.UpdateUserAccessRequest;
import com.smge.smge.authorization.dto.UserResponse;
import com.smge.smge.authorization.model.PerfilModel;
import com.smge.smge.authorization.model.Permissao;
import com.smge.smge.authorization.model.UserModel;
import com.smge.smge.authorization.repository.UserRepository;
import com.smge.smge.authorization.security.UsuarioLogado;
import com.smge.smge.common.exception.ConflitoException;
import com.smge.smge.common.exception.RecursoNaoEncontradoException;
import com.smge.smge.common.exception.RegraNegocioException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PerfilService perfilService;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PerfilService perfilService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.perfilService = perfilService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse criarUsuario(CreateUserRequest request) {

        String login = normalizarLogin(request.getLogin());

        if (userRepository.existsByLogin(login)) {
            throw new ConflitoException("Login já cadastrado");
        }

        Set<PerfilModel> perfis = perfilService.buscarPerfis(request.getPerfisIds());
        Set<Permissao> extras = paraSet(request.getPermissoesExtras());
        UsuarioLogado.exigirPodeConceder(somarPermissoes(perfis, extras));

        UserModel user = new UserModel();

        user.setNome(request.getNome().trim());
        user.setLogin(login);
        user.definirSenha(passwordEncoder.encode(request.getSenha()));
        user.getPerfis().addAll(perfis);
        user.getPermissoesExtras().addAll(extras);
        user.setActive(true);

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listarUsuarios() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse buscarPorId(UUID id) {
        return UserResponse.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public UserResponse buscarPorLogin(String login) {
        return UserResponse.from(buscarEntidadePorLogin(login));
    }

    /**
     * Substitui os perfis e as permissões extras do usuário.
     */
    @Transactional
    public UserResponse atualizarAcessos(UUID id, UpdateUserAccessRequest request) {

        UserModel user = buscarParaGerenciar(id, "Você não pode alterar os próprios acessos");

        Set<PerfilModel> perfis = perfilService.buscarPerfis(request.getPerfisIds());
        Set<Permissao> extras = paraSet(request.getPermissoesExtras());
        UsuarioLogado.exigirPodeConceder(somarPermissoes(perfis, extras));

        user.getPerfis().clear();
        user.getPerfis().addAll(perfis);
        user.getPermissoesExtras().clear();
        user.getPermissoesExtras().addAll(extras);

        return UserResponse.from(user);
    }

    @Transactional
    public void desativarUsuario(UUID id) {
        buscarParaGerenciar(id, "Você não pode desativar o próprio usuário").desativarUser();
    }

    @Transactional
    public void reativarUsuario(UUID id) {
        buscarParaGerenciar(id, "Você não pode reativar o próprio usuário").ativarUser();
    }

    /**
     * Redefine a senha de outro usuário (ex.: esqueceu a senha ou ela expirou).
     */
    @Transactional
    public void redefinirSenha(UUID id, String novaSenha) {
        buscarParaGerenciar(id, "Para trocar a própria senha use /users/me/senha")
                .definirSenha(passwordEncoder.encode(novaSenha));
    }

    /**
     * O próprio usuário troca a senha, confirmando a senha atual.
     */
    @Transactional
    public void alterarPropriaSenha(String login, ChangePasswordRequest request) {
        UserModel user = buscarEntidadePorLogin(login);

        if (!passwordEncoder.matches(request.getSenhaAtual(), user.getSenha())) {
            throw new RegraNegocioException("Senha atual incorreta");
        }

        if (request.getSenhaAtual().equals(request.getNovaSenha())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da atual");
        }

        user.definirSenha(passwordEncoder.encode(request.getNovaSenha()));
    }

    /**
     * Busca um usuário que o solicitante vai gerenciar: não pode ser ele mesmo
     * nem alguém com mais acessos que ele.
     */
    private UserModel buscarParaGerenciar(UUID id, String mensagemSeForOProprio) {
        UserModel user = buscarEntidade(id);

        if (user.getLogin().equals(UsuarioLogado.login())) {
            throw new RegraNegocioException(mensagemSeForOProprio);
        }

        UsuarioLogado.exigirPodeGerenciar(user.permissoesEfetivas());
        return user;
    }

    private UserModel buscarEntidade(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private UserModel buscarEntidadePorLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private Set<Permissao> somarPermissoes(Set<PerfilModel> perfis, Set<Permissao> extras) {
        Set<Permissao> todas = EnumSet.noneOf(Permissao.class);
        perfis.forEach(perfil -> todas.addAll(perfil.getPermissoes()));
        todas.addAll(extras);
        return todas;
    }

    private Set<Permissao> paraSet(Set<Permissao> permissoes) {
        Set<Permissao> resultado = EnumSet.noneOf(Permissao.class);
        if (permissoes != null) {
            resultado.addAll(permissoes);
        }
        return resultado;
    }

    private String normalizarLogin(String login) {
        return login.trim().toLowerCase(Locale.ROOT);
    }
}
