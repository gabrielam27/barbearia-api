package com.barbearia.api.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.barbearia.domain.model.StatusAgendamento;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgendamentoModel {

    private UUID id;

    private UUID clienteId;

    private UUID servicoId;

    private LocalDateTime dataHora;

    private StatusAgendamento status;

    private String observacoes;

    private LocalDateTime dataCriacao;
}
