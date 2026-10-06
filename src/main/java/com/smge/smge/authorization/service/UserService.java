package com.smge.smge.authorization.service;

import com.smge.smge.authorization.dto.FindUserRequest;
import com.smge.smge.authorization.dto.UserReturnDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smge.smge.authorization.dto.CreateUserRequest;
import com.smge.smge.authorization.model.UserModel;
import com.smge.smge.authorization.repository.UserRepository;

import java.util.List;

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

    public UserModel criarUsuario(CreateUserRequest request) {

        if (userRepository.existsByLogin(request.getLogin())) {
            throw new RuntimeException("Login já cadastrado");
        }

        UserModel user = new UserModel();

        user.setNome(request.getNome());
        user.setLogin(request.getLogin());

        user.setSenha(passwordEncoder.encode(request.getSenha()));

        user.setActive(true);

        return userRepository.save(user);
    }

    public UserModel desativarUsuario(FindUserRequest request) {
        UserModel user = userRepository.findById(request.id())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.desativarUser();

        return userRepository.save(user);
    }

    public List<UserReturnDTO> listarUsuarios() {
        List<UserModel> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserReturnDTO(
                        user.getUserId(),
                        user.getNome()
                ))
                .toList();
    }

}