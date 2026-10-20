package com.barbearia.api.assembler;

import org.springframework.stereotype.Component;

import com.barbearia.api.model.input.AgendamentoInput;
import com.barbearia.domain.model.Agendamento;
import com.barbearia.domain.model.Cliente;
import com.barbearia.domain.model.Servico;

@Component
public class AgendamentoInputDisassembler {

    public Agendamento toDomainObject(AgendamentoInput input) {
        Cliente cliente = new Cliente();
        cliente.setId(input.getClienteId());
        Servico servico = new Servico();
        servico.setId(input.getServicoId());

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setDataHora(input.getDataHora());
        agendamento.setObservacoes(input.getObservacoes());
        return agendamento;
    }
}
