package com.barbearia.api.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteModel {

    private UUID id;

    private String nome;

    private String telefone;

    private String email;

    private LocalDateTime dataCriacao;
}
