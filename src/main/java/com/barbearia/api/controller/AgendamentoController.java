package com.barbearia.api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barbearia.api.assembler.AgendamentoInputDisassembler;
import com.barbearia.api.assembler.AgendamentoModelAssembler;
import com.barbearia.api.model.AgendamentoModel;
import com.barbearia.api.model.input.AgendamentoInput;
import com.barbearia.domain.model.Agendamento;
import com.barbearia.domain.service.CadastroAgendamentoService;
import com.barbearia.domain.service.FluxoAgendamentoService;

@RestController
@RequestMapping("/api/v1/agendamentos")
public class AgendamentoController {

    @Autowired
    private CadastroAgendamentoService cadastroAgendamento;

    @Autowired
    private FluxoAgendamentoService fluxoAgendamento;

    @Autowired
    private AgendamentoModelAssembler agendamentoModelAssembler;

    @Autowired
    private AgendamentoInputDisassembler agendamentoInputDisassembler;

    @GetMapping
    public List<AgendamentoModel> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate data) {
        List<Agendamento> agendamentos = cadastroAgendamento.listar(data);
        return agendamentoModelAssembler.toCollectionModel(agendamentos);
    }

    @GetMapping("/{agendamentoId}")
    public AgendamentoModel buscar(@PathVariable UUID agendamentoId) {
        Agendamento agendamento = cadastroAgendamento.buscarOuFalhar(agendamentoId);
        return agendamentoModelAssembler.toModel(agendamento);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoModel adicionar(@RequestBody @Valid AgendamentoInput agendamentoInput) {
        Agendamento agendamento = agendamentoInputDisassembler.toDomainObject(agendamentoInput);
        agendamento = cadastroAgendamento.salvar(agendamento);
        return agendamentoModelAssembler.toModel(agendamento);
    }

    @PutMapping("/{agendamentoId}/confirmar")
    public AgendamentoModel confirmar(@PathVariable UUID agendamentoId) {
        Agendamento agendamento = fluxoAgendamento.confirmar(agendamentoId);
        return agendamentoModelAssembler.toModel(agendamento);
    }

    @PutMapping("/{agendamentoId}/cancelar")
    public AgendamentoModel cancelar(@PathVariable UUID agendamentoId) {
        Agendamento agendamento = fluxoAgendamento.cancelar(agendamentoId);
        return agendamentoModelAssembler.toModel(agendamento);
    }
}
