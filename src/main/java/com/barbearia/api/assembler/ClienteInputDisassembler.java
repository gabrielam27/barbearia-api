package com.barbearia.api.assembler;

import org.springframework.stereotype.Component;

import com.barbearia.api.model.input.ClienteInput;
import com.barbearia.domain.model.Cliente;

@Component
public class ClienteInputDisassembler {

    public Cliente toDomainObject(ClienteInput input) {
        Cliente cliente = new Cliente();
        copyToDomainObject(input, cliente);
        return cliente;
    }

    public void copyToDomainObject(ClienteInput input, Cliente cliente) {
        cliente.setNome(input.getNome());
        cliente.setTelefone(input.getTelefone());
        cliente.setEmail(input.getEmail());
    }
}
