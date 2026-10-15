package com.smge.smge.authorization.controller;

import com.smge.smge.authorization.dto.FindUserRequest;
import com.smge.smge.authorization.dto.UserReturnDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smge.smge.authorization.dto.CreateUserRequest;
import com.smge.smge.authorization.model.UserModel;
import com.smge.smge.authorization.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserReturnDTO> listarUsuarios(){
        return userService.listarUsuarios();
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<UserReturnDTO>> listarUsuariosAtivos() {
        return ResponseEntity.ok(userService.listarUsuariosAtivos());
    }

    @GetMapping("/inativos")
    public ResponseEntity<List<UserReturnDTO>> listarUsuariosInativos() {
        return ResponseEntity.ok(userService.listarUsuariosInativos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserReturnDTO> buscarUsuarioPorId(@PathVariable UUID id) {
        UserReturnDTO user = userService.buscarPorId(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserModel criarUsuario(
            @RequestBody CreateUserRequest request
    ) {
        return userService.criarUsuario(request);
    }

    @PostMapping("/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public UserModel DesativarUsuario(
            @RequestBody FindUserRequest request
    ) {
        return userService.desativarUsuario(request);
    }
}