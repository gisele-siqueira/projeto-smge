package com.smge.smge.authorization.config;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smge.smge.authorization.model.PerfilModel;
import com.smge.smge.authorization.model.Permissao;
import com.smge.smge.authorization.model.UserModel;
import com.smge.smge.authorization.repository.PerfilRepository;
import com.smge.smge.authorization.repository.UserRepository;

/**
 * Executado a cada inicialização:
 * 1. Garante o perfil "Administrador" com TODAS as permissões
 *    (inclusive as que forem criadas em versões futuras).
 * 2. Como o sistema não tem auto-cadastro, cria o usuário admin inicial
 *    se ainda não existir nenhum usuário com esse perfil.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final String nome;
    private final String login;
    private final String senha;

    public AdminSeeder(
            UserRepository userRepository,
            PerfilRepository perfilRepository,
            PasswordEncoder passwordEncoder,
            @Value("${smge.admin.nome}") String nome,
            @Value("${smge.admin.login}") String login,
            @Value("${smge.admin.senha}") String senha
    ) {
        this.userRepository = userRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(String... args) {

        PerfilModel perfilAdmin = garantirPerfilAdministrador();

        if (userRepository.existsByPerfis_Nome(PerfilModel.ADMINISTRADOR)) {
            return;
        }

        UserModel admin = new UserModel();
        admin.setNome(nome);
        admin.setLogin(login);
        admin.definirSenha(passwordEncoder.encode(senha));
        admin.getPerfis().add(perfilAdmin);
        admin.setActive(true);

        userRepository.save(admin);

        log.warn("Usuário administrador inicial criado com login '{}'. Troque a senha em PUT /api/users/me/senha.", login);
    }

    private PerfilModel garantirPerfilAdministrador() {
        PerfilModel perfil = perfilRepository.findByNome(PerfilModel.ADMINISTRADOR)
                .orElseGet(() -> {
                    PerfilModel novo = new PerfilModel();
                    novo.setNome(PerfilModel.ADMINISTRADOR);
                    novo.setDescricao("Acesso total ao sistema");
                    novo.setSistema(true);
                    return novo;
                });

        perfil.getPermissoes().addAll(EnumSet.allOf(Permissao.class));
        return perfilRepository.save(perfil);
    }
}
