package com.smge.smge.authorization.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smge.smge.authorization.dto.ChangePasswordRequest;
import com.smge.smge.authorization.dto.CreateUserRequest;
import com.smge.smge.authorization.dto.UserResponse;
import com.smge.smge.authorization.model.Role;
import com.smge.smge.authorization.model.UserModel;
import com.smge.smge.authorization.repository.UserRepository;
import com.smge.smge.common.exception.ConflitoException;
import com.smge.smge.common.exception.RegraNegocioException;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);

        when(userRepository.save(any(UserModel.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CreateUserRequest novoRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setNome(" João Silva ");
        request.setLogin(" Joao.Silva ");
        request.setSenha("Senha123");
        return request;
    }

    @Test
    void deveCriarUsuarioComSenhaCriptografadaEExpiracao() {

        UserResponse response = userService.criarUsuario(novoRequest());

        assertEquals("João Silva", response.nome());
        assertEquals("joao.silva", response.login());
        assertEquals(Role.USER, response.role());
        assertTrue(response.ativo());
        assertNotNull(response.senhaExpiraEm());

        verify(userRepository).save(argThat(user ->
                !user.getSenha().equals("Senha123")
                        && passwordEncoder.matches("Senha123", user.getSenha())));
    }

    @Test
    void deveRecusarLoginDuplicado() {

        when(userRepository.existsByLogin("joao.silva")).thenReturn(true);

        assertThrows(ConflitoException.class, () -> userService.criarUsuario(novoRequest()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirDesativarOProprioUsuario() {

        UserModel admin = new UserModel();
        admin.setLogin("admin");
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(admin));

        assertThrows(RegraNegocioException.class, () -> userService.desativarUsuario(id, "admin"));
        assertTrue(admin.isActive());
    }

    @Test
    void deveRecusarTrocaDeSenhaComSenhaAtualIncorreta() {

        UserModel user = new UserModel();
        user.setLogin("joao");
        user.definirSenha(passwordEncoder.encode("Senha123"));
        when(userRepository.findByLogin("joao")).thenReturn(Optional.of(user));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setSenhaAtual("errada123");
        request.setNovaSenha("NovaSenha456");

        assertThrows(RegraNegocioException.class, () -> userService.alterarPropriaSenha("joao", request));
    }

    @Test
    void deveTrocarSenhaQuandoSenhaAtualCorreta() {

        UserModel user = new UserModel();
        user.setLogin("joao");
        user.definirSenha(passwordEncoder.encode("Senha123"));
        when(userRepository.findByLogin("joao")).thenReturn(Optional.of(user));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setSenhaAtual("Senha123");
        request.setNovaSenha("NovaSenha456");

        userService.alterarPropriaSenha("joao", request);

        assertTrue(passwordEncoder.matches("NovaSenha456", user.getSenha()));
    }
}
