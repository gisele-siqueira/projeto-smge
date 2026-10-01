package com.smge.smge.authorization.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smge.smge.authorization.model.Role;
import com.smge.smge.authorization.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, UUID> {

    boolean existsByLogin(String login);

    Optional<UserModel> findByLogin(String login);

    boolean existsByRole(Role role);

}
