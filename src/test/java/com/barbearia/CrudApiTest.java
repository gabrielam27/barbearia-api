package com.barbearia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.barbearia.api.assembler.AgendamentoInputDisassembler;
import com.barbearia.api.model.input.AgendamentoInput;
import com.barbearia.api.model.input.ClienteInput;
import com.barbearia.api.model.input.ServicoAtualizacaoInput;
import com.barbearia.domain.model.Agendamento;
import com.barbearia.domain.model.Cliente;
import com.barbearia.domain.model.Servico;
import com.barbearia.domain.repository.AgendamentoRepository;
import com.barbearia.domain.repository.ClienteRepository;
import com.barbearia.domain.repository.ServicoRepository;
import com.barbearia.domain.service.CadastroAgendamentoService;
import com.barbearia.domain.service.FluxoAgendamentoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrudApiTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    ClienteRepository clientes;

    @Autowired
    ServicoRepository servicos;

    @Autowired
    AgendamentoRepository agendamentos;

    @Autowired
    CadastroAgendamentoService agenda;
    @Autowired
    FluxoAgendamentoService fluxoAgendamento;
    @Autowired
    AgendamentoInputDisassembler agendamentoInputDisassembler;

    private Cliente cliente;

    private Servico servico;

    private static final String C = "/api/v1/clientes/";

    private static final String S = "/api/v1/servicos/";

    @BeforeEach
    void preparar() {
        agendamentos.deleteAll();
        clientes.deleteAll();
        servicos.deleteAll();
        cliente = clientes.saveAndFlush(new Cliente("Ana", "11999999999", "ana@example.com"));
        servico = servicos.saveAndFlush(new Servico("Corte", new BigDecimal("50.00"), 30));
    }

    @AfterEach
    void limpar() {
        agendamentos.deleteAll();
    }

    private String clienteJson(String telefone) throws Exception {
        return mapper.writeValueAsString(new ClienteInput("Ana editada", telefone, null));
    }

    private String servicoJson(int duracao, boolean ativo) throws Exception {
        return mapper.writeValueAsString(
                new ServicoAtualizacaoInput(
                        "Corte editado", new BigDecimal("65.50"), duracao, ativo));
    }

    private Agendamento reservar() {
        AgendamentoInput input =
                new AgendamentoInput(
                        cliente.getId(),
                        servico.getId(),
                        LocalDateTime.parse("2030-01-15T10:00:00"),
                        null);
        return agenda.salvar(agendamentoInputDisassembler.toDomainObject(input));
    }

    @Test
    void atualizarClientePreservandoIdEData() throws Exception {
        LocalDateTime criacao = clientes.findById(cliente.getId()).orElseThrow().getDataCriacao();
        mvc.perform(
                        put(C + cliente.getId())
                                .contentType("application/json")
                                .content(clienteJson(cliente.getTelefone())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cliente.getId().toString()))
                .andExpect(jsonPath("$.nome").value("Ana editada"));
        Cliente salvo = clientes.findById(cliente.getId()).orElseThrow();
        assertThat(salvo.getDataCriacao()).isEqualTo(criacao);
        assertThat(salvo.getEmail()).isNull();
    }

    @Test
    void rejeitarTelefoneDeOutroClienteSemAlterarDados() throws Exception {
        clientes.saveAndFlush(new Cliente("Outra", "11888888888", null));
        mvc.perform(
                        put(C + cliente.getId())
                                .contentType("application/json")
                                .content(clienteJson("11888888888")))
                .andExpect(status().isBadRequest());
        assertThat(clientes.findById(cliente.getId()).orElseThrow().getNome()).isEqualTo("Ana");
    }

    @Test
    void excluirRegistrosSemAgendamentos() throws Exception {
        mvc.perform(delete(C + cliente.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mvc.perform(get(C + cliente.getId())).andExpect(status().isNotFound());
        mvc.perform(delete(S + servico.getId())).andExpect(status().isNoContent());
        mvc.perform(get(S + servico.getId())).andExpect(status().isNotFound());
    }

    @Test
    void protegerHistoricoMesmoAposCancelamento() throws Exception {
        Agendamento reserva = reservar();
        for (int i = 0; i < 2; i++) {
            mvc.perform(delete(C + cliente.getId())).andExpect(status().isConflict());
            mvc.perform(delete(S + servico.getId())).andExpect(status().isConflict());
            fluxoAgendamento.cancelar(reserva.getId());
        }
        assertThat(agendamentos.count()).isEqualTo(1);
    }

    @Test
    void editarDesativarEReativarServico() throws Exception {
        mvc.perform(
                        put(S + servico.getId())
                                .contentType("application/json")
                                .content(servicoJson(45, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.duracao").value(45))
                .andExpect(jsonPath("$.preco").value(65.5));
        mvc.perform(get(S + servico.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(get("/api/v1/servicos")).andExpect(content().json("[]"));
        mvc.perform(
                        put(S + servico.getId())
                                .contentType("application/json")
                                .content(servicoJson(45, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
        mvc.perform(get("/api/v1/servicos")).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void preservarDuracaoDaReservaExistente() throws Exception {
        reservar();
        mvc.perform(
                        put(S + servico.getId())
                                .contentType("application/json")
                                .content(servicoJson(60, true)))
                .andExpect(status().isOk());
        var seguinte =
                new AgendamentoInput(
                        cliente.getId(),
                        servico.getId(),
                        LocalDateTime.parse("2030-01-15T10:30:00"),
                        null);
        mvc.perform(
                        post("/api/v1/agendamentos")
                                .contentType("application/json")
                                .content(mapper.writeValueAsString(seguinte)))
                .andExpect(status().isCreated());
    }

    @Test
    void validarEdicoesSemMudarRegistro() throws Exception {
        mvc.perform(put(C + cliente.getId()).contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put(S + servico.getId()).contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        put(S + servico.getId())
                                .contentType("application/json")
                                .content(servicoJson(0, true)))
                .andExpect(status().isBadRequest());
        assertThat(servicos.findById(servico.getId()).orElseThrow().getDuracao()).isEqualTo(30);
    }

    @Test
    void retornar404ParaAtualizarOuExcluirAusentes() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(put(C + id).contentType("application/json").content(clienteJson("11777777777")))
                .andExpect(status().isNotFound());
        mvc.perform(put(S + id).contentType("application/json").content(servicoJson(30, true)))
                .andExpect(status().isNotFound());
        mvc.perform(delete(C + id)).andExpect(status().isNotFound());
        mvc.perform(delete(S + id)).andExpect(status().isNotFound());
        mvc.perform(get(S + id)).andExpect(status().isNotFound());
    }
}
