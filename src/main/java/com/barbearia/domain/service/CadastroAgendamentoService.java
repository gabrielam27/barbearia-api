package com.barbearia.domain.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbearia.domain.exception.ConflitoException;
import com.barbearia.domain.exception.EntidadeNaoEncontradaException;
import com.barbearia.domain.exception.NegocioException;
import com.barbearia.domain.model.Agendamento;
import com.barbearia.domain.model.Cliente;
import com.barbearia.domain.model.Servico;
import com.barbearia.domain.model.StatusAgendamento;
import com.barbearia.domain.repository.AgendamentoRepository;
import com.barbearia.domain.repository.ClienteRepository;
import com.barbearia.domain.repository.ServicoRepository;

@Service
public class CadastroAgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Transactional
    public Agendamento salvar(Agendamento agendamento) {
        UUID clienteId = agendamento.getCliente().getId();
        Cliente cliente =
                clienteRepository
                        .findById(clienteId)
                        .orElseThrow(() -> new NegocioException("Cliente não encontrado"));

        UUID servicoId = agendamento.getServico().getId();
        Servico servico = servicoRepository.buscarParaAlterar(servicoId);
        if (servico == null) {
            throw new NegocioException("Serviço não encontrado");
        }
        if (!servico.isAtivo()) {
            throw new NegocioException("Serviço inativo");
        }

        LocalDateTime inicio = agendamento.getDataHora().withNano(0);
        LocalDateTime fim = inicio.plusMinutes(servico.getDuracao());
        long conflitos =
                agendamentoRepository.contarConflitos(inicio, fim, StatusAgendamento.CANCELADO);
        if (conflitos > 0) {
            throw new ConflitoException(
                    "Horário indisponível. Já existe um agendamento nesse intervalo.");
        }

        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setDataHora(inicio);
        agendamento.setDataHoraFim(fim);
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        // schema.sql protege também duas reservas que chegam simultaneamente.
        return agendamentoRepository.saveAndFlush(agendamento);
    }

    public Agendamento buscarOuFalhar(UUID agendamentoId) {
        return agendamentoRepository
                .findById(agendamentoId)
                .orElseThrow(
                        () -> new EntidadeNaoEncontradaException("Agendamento não encontrado"));
    }

    public List<Agendamento> listar(LocalDate data) {
        if (data == null) {
            return agendamentoRepository.findAllByOrderByDataHoraAscIdAsc();
        }
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay();
        return agendamentoRepository.buscarPorDia(inicio, fim);
    }
}
