package com.smge.smge.authorization.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smge.smge.authorization.dto.ChangePasswordRequest;
import com.smge.smge.authorization.dto.CreateUserRequest;
import com.smge.smge.authorization.dto.UserResponse;
import com.smge.smge.authorization.model.PerfilModel;
import com.smge.smge.authorization.model.Permissao;
import com.smge.smge.authorization.model.UserModel;
import com.smge.smge.authorization.repository.UserRepository;
import com.smge.smge.common.exception.AcessoNegadoException;
import com.smge.smge.common.exception.ConflitoException;
import com.smge.smge.common.exception.RegraNegocioException;

class UserServiceTest {

    private UserRepository userRepository;
    private PerfilService perfilService;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        perfilService = mock(PerfilService.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, perfilService, passwordEncoder);

        when(userRepository.save(any(UserModel.class))).thenAnswer(inv -> inv.getArgument(0));
        when(perfilService.buscarPerfis(anyCollection())).thenReturn(new HashSet<>());
    }

    @AfterEach
    void limparLogin() {
        SecurityContextHolder.clearContext();
    }

    private void logarComo(String login, Permissao... permissoes) {
        String[] nomes = Arrays.stream(permissoes).map(Enum::name).toArray(String[]::new);
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(login, null, nomes));
    }

    private CreateUserRequest novoRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setNome(" João Silva ");
        request.setLogin(" Joao.Silva ");
        request.setSenha("Senha123");
        return request;
    }

    private UserModel usuario(String login, Permissao... extras) {
        UserModel user = new UserModel();
        user.setLogin(login);
        user.definirSenha(passwordEncoder.encode("Senha123"));
        user.getPermissoesExtras().addAll(Set.of(extras));
        return user;
    }

    @Test
    void deveCriarUsuarioComSenhaCriptografadaEExpiracao() {
        logarComo("admin", Permissao.USUARIO_GERENCIAR);

        UserResponse response = userService.criarUsuario(novoRequest());

        assertEquals("João Silva", response.nome());
        assertEquals("joao.silva", response.login());
        assertTrue(response.ativo());
        assertTrue(response.permissoes().isEmpty());
        assertNotNull(response.senhaExpiraEm());

        verify(userRepository).save(argThat(user ->
                !user.getSenha().equals("Senha123")
                        && passwordEncoder.matches("Senha123", user.getSenha())));
    }

    @Test
    void deveRecusarLoginDuplicado() {
        logarComo("admin", Permissao.USUARIO_GERENCIAR);
        when(userRepository.existsByLogin("joao.silva")).thenReturn(true);

        assertThrows(ConflitoException.class, () -> userService.criarUsuario(novoRequest()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirConcederPermissaoQueNaoPossui() {
        logarComo("gerente", Permissao.USUARIO_GERENCIAR);

        CreateUserRequest request = novoRequest();
        request.setPermissoesExtras(Set.of(Permissao.PRODUTO_EXCLUIR));

        assertThrows(AcessoNegadoException.class, () -> userService.criarUsuario(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirConcederPerfilComPermissaoQueNaoPossui() {
        logarComo("gerente", Permissao.USUARIO_GERENCIAR);

        PerfilModel estoquista = new PerfilModel();
        estoquista.getPermissoes().add(Permissao.PRODUTO_CRIAR);
        when(perfilService.buscarPerfis(anyCollection())).thenReturn(new HashSet<>(Set.of(estoquista)));

        CreateUserRequest request = novoRequest();
        request.setPerfisIds(Set.of(UUID.randomUUID()));

        assertThrows(AcessoNegadoException.class, () -> userService.criarUsuario(request));
    }

    @Test
    void naoDevePermitirDesativarOProprioUsuario() {
        logarComo("admin", Permissao.USUARIO_GERENCIAR);

        UserModel admin = usuario("admin");
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(admin));

        assertThrows(RegraNegocioException.class, () -> userService.desativarUsuario(id));
        assertTrue(admin.isActive());
    }

    @Test
    void naoDevePermitirGerenciarUsuarioComMaisAcessos() {
        logarComo("gerente", Permissao.USUARIO_GERENCIAR);

        UserModel chefe = usuario("chefe", Permissao.USUARIO_GERENCIAR, Permissao.PERFIL_GERENCIAR);
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(chefe));

        assertThrows(AcessoNegadoException.class, () -> userService.desativarUsuario(id));
        assertThrows(AcessoNegadoException.class, () -> userService.redefinirSenha(id, "NovaSenha1"));
        assertTrue(chefe.isActive());
    }

    @Test
    void deveRecusarTrocaDeSenhaComSenhaAtualIncorreta() {
        UserModel user = usuario("joao");
        when(userRepository.findByLogin("joao")).thenReturn(Optional.of(user));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setSenhaAtual("errada123");
        request.setNovaSenha("NovaSenha456");

        assertThrows(RegraNegocioException.class, () -> userService.alterarPropriaSenha("joao", request));
    }

    @Test
    void deveTrocarSenhaQuandoSenhaAtualCorreta() {
        UserModel user = usuario("joao");
        when(userRepository.findByLogin("joao")).thenReturn(Optional.of(user));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setSenhaAtual("Senha123");
        request.setNovaSenha("NovaSenha456");

        userService.alterarPropriaSenha("joao", request);

        assertTrue(passwordEncoder.matches("NovaSenha456", user.getSenha()));
    }
}
