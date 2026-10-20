package com.barbearia.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barbearia.api.assembler.ServicoInputDisassembler;
import com.barbearia.api.assembler.ServicoModelAssembler;
import com.barbearia.api.model.ServicoModel;
import com.barbearia.api.model.input.ServicoAtualizacaoInput;
import com.barbearia.api.model.input.ServicoInput;
import com.barbearia.domain.model.Servico;
import com.barbearia.domain.repository.ServicoRepository;
import com.barbearia.domain.service.CadastroServicoService;

@RestController
@RequestMapping("/api/v1/servicos")
public class ServicoController {

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private CadastroServicoService cadastroServico;

    @Autowired
    private ServicoModelAssembler servicoModelAssembler;

    @Autowired
    private ServicoInputDisassembler servicoInputDisassembler;

    @GetMapping
    public List<ServicoModel> listar() {
        List<Servico> servicos = servicoRepository.findByAtivoTrueOrderByNomeAscIdAsc();
        return servicoModelAssembler.toCollectionModel(servicos);
    }

    @GetMapping("/{servicoId}")
    public ServicoModel buscar(@PathVariable UUID servicoId) {
        Servico servico = cadastroServico.buscarOuFalhar(servicoId);
        return servicoModelAssembler.toModel(servico);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoModel adicionar(@RequestBody @Valid ServicoInput servicoInput) {
        Servico servico = servicoInputDisassembler.toDomainObject(servicoInput);
        servico = cadastroServico.salvar(servico);
        return servicoModelAssembler.toModel(servico);
    }

    @PutMapping("/{servicoId}")
    public ServicoModel atualizar(
            @PathVariable UUID servicoId,
            @RequestBody @Valid ServicoAtualizacaoInput servicoInput) {
        Servico servicoAtual = cadastroServico.buscarOuFalhar(servicoId);
        servicoInputDisassembler.copyToDomainObject(servicoInput, servicoAtual);
        servicoAtual = cadastroServico.salvar(servicoAtual);
        return servicoModelAssembler.toModel(servicoAtual);
    }

    @DeleteMapping("/{servicoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID servicoId) {
        cadastroServico.excluir(servicoId);
    }
}
