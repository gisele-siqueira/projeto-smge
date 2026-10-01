package com.smge.smge.authorization.security;

import java.util.Locale;

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
 * Faz o Spring Security autenticar usando os usuários salvos no banco.
 * Cada permissão efetiva do usuário vira uma "authority" (ex.: PRODUTO_CRIAR).
 */
@Service
public class SmgeUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public SmgeUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserModel user = userRepository.findByLogin(username.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return User.withUsername(user.getLogin())
                .password(user.getSenha())
                .authorities(user.permissoesEfetivas().stream()
                        .map(permissao -> new SimpleGrantedAuthority(permissao.name()))
                        .toList())
                // usuário desativado não consegue logar
                .disabled(!user.isActive())
                // senha expirada bloqueia o login até alguém com USUARIO_GERENCIAR redefinir
                .credentialsExpired(user.senhaExpirada())
                .build();
    }
}
