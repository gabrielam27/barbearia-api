package com.barbearia.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barbearia.domain.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    boolean existsByTelefoneAndIdNot(String telefone, UUID id);

    boolean existsByTelefone(String telefone);
}
