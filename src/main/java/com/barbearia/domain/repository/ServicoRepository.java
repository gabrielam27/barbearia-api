package com.barbearia.domain.repository;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.barbearia.domain.model.Servico;

public interface ServicoRepository extends JpaRepository<Servico, UUID> {
    List<Servico> findByAtivoTrueOrderByNomeAscIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Servico s where s.id = :id")
    Servico buscarParaAlterar(@Param("id") UUID id);
}
