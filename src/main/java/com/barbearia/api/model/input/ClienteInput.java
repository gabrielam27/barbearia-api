package com.barbearia.api.model.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteInput {
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120)
    private String nome;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 30)
    private String telefone;

    @Email(message = "Email inválido")
    @Size(max = 254)
    private String email;
}
