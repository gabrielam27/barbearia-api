package com.barbearia.api.model.input;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoInput {
    @NotNull(message = "Cliente é obrigatório")
    private UUID clienteId;

    @NotNull(message = "Serviço é obrigatório")
    private UUID servicoId;

    @NotNull(message = "Data e hora são obrigatórias")
    private LocalDateTime dataHora;

    @Size(max = 1000)
    private String observacoes;
}
