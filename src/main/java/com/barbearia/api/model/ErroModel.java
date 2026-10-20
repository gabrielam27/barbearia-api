package com.barbearia.api.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErroModel {

    private String erro;

    private LocalDateTime timestamp;
}
