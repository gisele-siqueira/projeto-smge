package com.smge.smge.authorization.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.smge.smge.authorization.model.Role;
import com.smge.smge.authorization.model.UserModel;
import com.smge.smge.authorization.repository.UserRepository;

/**
 * Como o sistema não tem auto-cadastro, cria a conta ADMIN inicial
 * na primeira inicialização (somente se ainda não existir nenhum ADMIN).
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String nome;
    private final String login;
    private final String senha;

    public AdminSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${smge.admin.nome}") String nome,
            @Value("${smge.admin.login}") String login,
            @Value("${smge.admin.senha}") String senha
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {

        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        UserModel admin = new UserModel();
        admin.setNome(nome);
        admin.setLogin(login);
        admin.definirSenha(passwordEncoder.encode(senha));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);

        userRepository.save(admin);

        log.warn("Usuário ADMIN inicial criado com login '{}'. Troque a senha em PUT /users/me/senha.", login);
    }
}
