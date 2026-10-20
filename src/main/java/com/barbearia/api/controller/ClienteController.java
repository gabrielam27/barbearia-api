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

import com.barbearia.api.assembler.ClienteInputDisassembler;
import com.barbearia.api.assembler.ClienteModelAssembler;
import com.barbearia.api.model.ClienteModel;
import com.barbearia.api.model.input.ClienteInput;
import com.barbearia.domain.model.Cliente;
import com.barbearia.domain.repository.ClienteRepository;
import com.barbearia.domain.service.CadastroClienteService;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CadastroClienteService cadastroCliente;

    @Autowired
    private ClienteModelAssembler clienteModelAssembler;

    @Autowired
    private ClienteInputDisassembler clienteInputDisassembler;

    @GetMapping
    public List<ClienteModel> listar() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clienteModelAssembler.toCollectionModel(clientes);
    }

    @GetMapping("/{clienteId}")
    public ClienteModel buscar(@PathVariable UUID clienteId) {
        Cliente cliente = cadastroCliente.buscarOuFalhar(clienteId);
        return clienteModelAssembler.toModel(cliente);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteModel adicionar(@RequestBody @Valid ClienteInput clienteInput) {
        Cliente cliente = clienteInputDisassembler.toDomainObject(clienteInput);
        cliente = cadastroCliente.salvar(cliente);
        return clienteModelAssembler.toModel(cliente);
    }

    @PutMapping("/{clienteId}")
    public ClienteModel atualizar(
            @PathVariable UUID clienteId, @RequestBody @Valid ClienteInput clienteInput) {
        Cliente clienteAtual = cadastroCliente.buscarOuFalhar(clienteId);
        clienteInputDisassembler.copyToDomainObject(clienteInput, clienteAtual);
        clienteAtual = cadastroCliente.salvar(clienteAtual);
        return clienteModelAssembler.toModel(clienteAtual);
    }

    @DeleteMapping("/{clienteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID clienteId) {
        cadastroCliente.excluir(clienteId);
    }
}
