package com.smge.smge.authorization.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smge.smge.authorization.model.PerfilModel;

public interface PerfilRepository extends JpaRepository<PerfilModel, UUID> {

    Optional<PerfilModel> findByNome(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndPerfilIdNot(String nome, UUID perfilId);

}
