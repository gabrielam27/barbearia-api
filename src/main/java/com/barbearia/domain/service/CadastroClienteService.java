package com.barbearia.domain.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbearia.domain.exception.EntidadeEmUsoException;
import com.barbearia.domain.exception.EntidadeNaoEncontradaException;
import com.barbearia.domain.exception.NegocioException;
import com.barbearia.domain.model.Cliente;
import com.barbearia.domain.repository.ClienteRepository;

@Service
public class CadastroClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Transactional
    public Cliente salvar(Cliente cliente) {
        cliente.setNome(cliente.getNome().strip());
        cliente.setTelefone(cliente.getTelefone().strip());

        boolean telefoneCadastrado;
        if (cliente.getId() == null) {
            telefoneCadastrado = clienteRepository.existsByTelefone(cliente.getTelefone());
        } else {
            telefoneCadastrado =
                    clienteRepository.existsByTelefoneAndIdNot(
                            cliente.getTelefone(), cliente.getId());
        }
        if (telefoneCadastrado) {
            throw new NegocioException("Telefone já cadastrado");
        }
        return clienteRepository.saveAndFlush(cliente);
    }

    public Cliente buscarOuFalhar(UUID clienteId) {
        return clienteRepository
                .findById(clienteId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente não encontrado"));
    }

    @Transactional
    public void excluir(UUID clienteId) {
        Cliente cliente = buscarOuFalhar(clienteId);
        try {
            clienteRepository.delete(cliente);
            clienteRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EntidadeEmUsoException("Cliente possui agendamentos e não pode ser excluído");
        }
    }
}
