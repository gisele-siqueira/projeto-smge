package com.smge.smge.authorization.controller;

import com.smge.smge.authorization.dto.FindUserRequest;
import com.smge.smge.authorization.dto.UserReturnDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.smge.smge.authorization.dto.CreateUserRequest;
import com.smge.smge.authorization.model.UserModel;
import com.smge.smge.authorization.service.UserService;

import java.util.List;

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