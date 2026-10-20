package com.barbearia.api.assembler;

import org.springframework.stereotype.Component;

import com.barbearia.api.model.input.ServicoAtualizacaoInput;
import com.barbearia.api.model.input.ServicoInput;
import com.barbearia.domain.model.Servico;

@Component
public class ServicoInputDisassembler {

    public Servico toDomainObject(ServicoInput input) {
        Servico servico = new Servico();
        servico.setNome(input.getNome());
        servico.setPreco(input.getPreco());
        servico.setDuracao(input.getDuracao());
        return servico;
    }

    public void copyToDomainObject(ServicoAtualizacaoInput input, Servico servico) {
        servico.setNome(input.getNome());
        servico.setPreco(input.getPreco());
        servico.setDuracao(input.getDuracao());
        servico.setAtivo(input.getAtivo());
    }
}
