package com.smge.smge.authorization.security;

import java.util.List;
import java.util.Locale;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smge.smge.authorization.model.UserModel;
import com.smge.smge.authorization.repository.UserRepository;

/**
 * Usado no login (POST /auth/login) para conferir login e senha
 * contra os usuários salvos no banco.
 */
@Service
public class SmgeUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public SmgeUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Cada permissão efetiva do usuário vira uma "authority" (ex.: PRODUTO_CRIAR).
     */
    public static List<GrantedAuthority> authoritiesDe(UserModel user) {
        return user.permissoesEfetivas().stream()
                .map(permissao -> (GrantedAuthority) new SimpleGrantedAuthority(permissao.name()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserModel user = userRepository.findByLogin(username.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return User.withUsername(user.getLogin())
                .password(user.getSenha())
                .authorities(authoritiesDe(user))
                // usuário desativado não consegue logar
                .disabled(!user.isActive())
                // senha expirada bloqueia o login até alguém com USUARIO_GERENCIAR redefinir
                .credentialsExpired(user.senhaExpirada())
                .build();
    }
}
