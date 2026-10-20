package com.barbearia.api.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicoModel {

    private UUID id;

    private String nome;

    private BigDecimal preco;

    private Integer duracao;

    private boolean ativo;
}
