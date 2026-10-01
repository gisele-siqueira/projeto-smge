package com.smge.smge.authorization.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smge.smge.authorization.dto.ChangePasswordRequest;
import com.smge.smge.authorization.dto.CreateUserRequest;
import com.smge.smge.authorization.dto.UserResponse;
import com.smge.smge.authorization.model.Role;
import com.smge.smge.authorization.model.UserModel;
import com.smge.smge.authorization.repository.UserRepository;
import com.smge.smge.common.exception.ConflitoException;
import com.smge.smge.common.exception.RecursoNaoEncontradoException;
import com.smge.smge.common.exception.RegraNegocioException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse criarUsuario(CreateUserRequest request) {

        String login = normalizarLogin(request.getLogin());

        if (userRepository.existsByLogin(login)) {
            throw new ConflitoException("Login já cadastrado");
        }

        UserModel user = new UserModel();

        user.setNome(request.getNome().trim());
        user.setLogin(login);
        user.definirSenha(passwordEncoder.encode(request.getSenha()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);
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

    @Transactional
    public void desativarUsuario(UUID id, String loginSolicitante) {
        UserModel user = buscarEntidade(id);

        if (user.getLogin().equals(loginSolicitante)) {
            throw new RegraNegocioException("Você não pode desativar o próprio usuário");
        }

        user.desativarUser();
    }

    @Transactional
    public void reativarUsuario(UUID id) {
        buscarEntidade(id).ativarUser();
    }

    /**
     * ADMIN redefine a senha de um usuário (ex.: esqueceu a senha ou ela expirou).
     */
    @Transactional
    public void redefinirSenha(UUID id, String novaSenha) {
        buscarEntidade(id).definirSenha(passwordEncoder.encode(novaSenha));
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

    private UserModel buscarEntidade(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private UserModel buscarEntidadePorLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private String normalizarLogin(String login) {
        return login.trim().toLowerCase(Locale.ROOT);
    }
}
