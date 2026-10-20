package com.barbearia.api.assembler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.barbearia.api.model.ServicoModel;
import com.barbearia.domain.model.Servico;

@Component
public class ServicoModelAssembler {

    public ServicoModel toModel(Servico entidade) {
        ServicoModel model = new ServicoModel();
        model.setId(entidade.getId());
        model.setNome(entidade.getNome());
        model.setPreco(entidade.getPreco());
        model.setDuracao(entidade.getDuracao());
        model.setAtivo(entidade.isAtivo());
        return model;
    }

    public List<ServicoModel> toCollectionModel(List<Servico> entidades) {
        List<ServicoModel> models = new ArrayList<>();
        for (Servico entidade : entidades) {
            models.add(toModel(entidade));
        }
        return models;
    }
}
