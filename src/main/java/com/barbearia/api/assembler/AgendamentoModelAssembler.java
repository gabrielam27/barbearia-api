package com.barbearia.api.assembler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.barbearia.api.model.AgendamentoModel;
import com.barbearia.domain.model.Agendamento;

@Component
public class AgendamentoModelAssembler {

    public AgendamentoModel toModel(Agendamento entidade) {
        AgendamentoModel model = new AgendamentoModel();
        model.setId(entidade.getId());
        model.setClienteId(entidade.getCliente().getId());
        model.setServicoId(entidade.getServico().getId());
        model.setDataHora(entidade.getDataHora());
        model.setStatus(entidade.getStatus());
        model.setObservacoes(entidade.getObservacoes());
        model.setDataCriacao(entidade.getDataCriacao());
        return model;
    }

    public List<AgendamentoModel> toCollectionModel(List<Agendamento> entidades) {
        List<AgendamentoModel> models = new ArrayList<>();
        for (Agendamento entidade : entidades) {
            models.add(toModel(entidade));
        }
        return models;
    }
}
