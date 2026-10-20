package com.barbearia.domain.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbearia.domain.exception.EntidadeNaoEncontradaException;
import com.barbearia.domain.model.Agendamento;
import com.barbearia.domain.repository.AgendamentoRepository;

@Service
public class FluxoAgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Transactional
    public Agendamento confirmar(UUID agendamentoId) {
        Agendamento agendamento = buscarParaAlterar(agendamentoId);
        agendamento.confirmar();
        return agendamentoRepository.saveAndFlush(agendamento);
    }

    @Transactional
    public Agendamento cancelar(UUID agendamentoId) {
        Agendamento agendamento = buscarParaAlterar(agendamentoId);
        agendamento.cancelar();
        return agendamentoRepository.saveAndFlush(agendamento);
    }

    private Agendamento buscarParaAlterar(UUID agendamentoId) {
        Agendamento agendamento = agendamentoRepository.buscarParaAlterar(agendamentoId);
        if (agendamento == null) {
            throw new EntidadeNaoEncontradaException("Agendamento não encontrado");
        }
        return agendamento;
    }
}
