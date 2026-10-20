package com.barbearia.api.assembler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.barbearia.api.model.ClienteModel;
import com.barbearia.domain.model.Cliente;

@Component
public class ClienteModelAssembler {

    public ClienteModel toModel(Cliente entidade) {
        ClienteModel model = new ClienteModel();
        model.setId(entidade.getId());
        model.setNome(entidade.getNome());
        model.setTelefone(entidade.getTelefone());
        model.setEmail(entidade.getEmail());
        model.setDataCriacao(entidade.getDataCriacao());
        return model;
    }

    public List<ClienteModel> toCollectionModel(List<Cliente> entidades) {
        List<ClienteModel> models = new ArrayList<>();
        for (Cliente entidade : entidades) {
            models.add(toModel(entidade));
        }
        return models;
    }
}
