package com.barbearia.domain.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbearia.domain.exception.EntidadeEmUsoException;
import com.barbearia.domain.exception.EntidadeNaoEncontradaException;
import com.barbearia.domain.model.Servico;
import com.barbearia.domain.repository.ServicoRepository;

@Service
public class CadastroServicoService {

    @Autowired
    private ServicoRepository servicoRepository;

    @Transactional
    public Servico salvar(Servico servico) {
        servico.setNome(servico.getNome().strip());
        if (servico.getId() == null) {
            return servicoRepository.saveAndFlush(servico);
        }

        // A edição espera se uma reserva está usando a duração deste serviço.
        Servico servicoAtual = servicoRepository.buscarParaAlterar(servico.getId());
        if (servicoAtual == null) {
            throw new EntidadeNaoEncontradaException("Serviço não encontrado");
        }
        servicoAtual.setNome(servico.getNome());
        servicoAtual.setPreco(servico.getPreco());
        servicoAtual.setDuracao(servico.getDuracao());
        servicoAtual.setAtivo(servico.isAtivo());
        return servicoRepository.saveAndFlush(servicoAtual);
    }

    public Servico buscarOuFalhar(UUID servicoId) {
        return servicoRepository
                .findById(servicoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Serviço não encontrado"));
    }

    @Transactional
    public void excluir(UUID servicoId) {
        Servico servico = buscarOuFalhar(servicoId);
        try {
            servicoRepository.delete(servico);
            servicoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EntidadeEmUsoException(
                    "Serviço possui agendamentos; desative-o em vez de excluir");
        }
    }
}
