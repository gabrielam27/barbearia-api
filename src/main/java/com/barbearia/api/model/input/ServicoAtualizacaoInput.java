package com.barbearia.api.model.input;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicoAtualizacaoInput {
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120)
    private String nome;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.00", message = "Preço não pode ser negativo")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal preco;

    @NotNull(message = "Duração é obrigatória")
    @Positive(message = "Duração deve ser maior que zero, em minutos")
    private Integer duracao;

    @NotNull(message = "Ativo é obrigatório")
    private Boolean ativo;
}
