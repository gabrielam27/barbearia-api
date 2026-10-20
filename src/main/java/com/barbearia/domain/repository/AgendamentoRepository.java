package com.barbearia.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.barbearia.domain.model.Agendamento;
import com.barbearia.domain.model.StatusAgendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {
    boolean existsByClienteId(UUID clienteId);

    boolean existsByServicoId(UUID servicoId);

    List<Agendamento> findAllByOrderByDataHoraAscIdAsc();

    @Query(
            "select a from Agendamento a where a.dataHora >= :inicio and a.dataHora < :fim order by a.dataHora, a.id")
    List<Agendamento> buscarPorDia(
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query(
            "select count(a) from Agendamento a where a.status <> :cancelado and a.dataHora < :fim and a.dataHoraFim > :inicio")
    long contarConflitos(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("cancelado") StatusAgendamento cancelado);

    // Confirmar e cancelar o mesmo registro ao mesmo tempo devem esperar um pelo outro.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Agendamento a where a.id = :id")
    Agendamento buscarParaAlterar(@Param("id") UUID id);
}
