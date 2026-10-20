package com.barbearia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.barbearia.api.assembler.AgendamentoInputDisassembler;
import com.barbearia.api.model.input.AgendamentoInput;
import com.barbearia.domain.exception.ConflitoException;
import com.barbearia.domain.model.Agendamento;
import com.barbearia.domain.model.Cliente;
import com.barbearia.domain.model.Servico;
import com.barbearia.domain.repository.AgendamentoRepository;
import com.barbearia.domain.repository.ClienteRepository;
import com.barbearia.domain.repository.ServicoRepository;
import com.barbearia.domain.service.CadastroAgendamentoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendamentoApiTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    ClienteRepository clientes;

    @Autowired
    ServicoRepository servicos;

    @Autowired
    AgendamentoRepository repository;

    @Autowired
    CadastroAgendamentoService service;
    @Autowired
    AgendamentoInputDisassembler agendamentoInputDisassembler;

    private UUID clienteId;

    private UUID servicoId;

    private static final String URL = "/api/v1/agendamentos";

    @BeforeEach
    void preparar() {
        repository.deleteAll();
        clientes.deleteAll();
        servicos.deleteAll();
        clienteId = clientes.saveAndFlush(new Cliente("Ana", "11999999999", null)).getId();
        servicoId =
                servicos.saveAndFlush(new Servico("Corte", new BigDecimal("50.00"), 30)).getId();
    }

    @AfterEach
    void limpar() {
        repository.deleteAll();
    }

    private AgendamentoInput request(String data) {
        return new AgendamentoInput(clienteId, servicoId, LocalDateTime.parse(data), "Sem perfume");
    }

    private String corpo(String data) throws Exception {
        return mapper.writeValueAsString(request(data));
    }

    private String cadastrar(String data) throws Exception {
        String resposta =
                mvc.perform(post(URL).contentType("application/json").content(corpo(data)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status").value("AGENDADO"))
                        .andExpect(jsonPath("$.clienteId").value(clienteId.toString()))
                        .andExpect(jsonPath("$.servicoId").value(servicoId.toString()))
                        .andExpect(jsonPath("$.observacoes").value("Sem perfume"))
                        .andExpect(jsonPath("$.dataCriacao").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return mapper.readTree(resposta).get("id").asText();
    }

    @Test
    void cadastrarBuscarEListarPorDia() throws Exception {
        String id = cadastrar("2030-01-15T10:00:00");
        cadastrar("2030-01-16T00:00:00");
        cadastrar("2030-01-15T00:00:00");
        mvc.perform(get(URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
        mvc.perform(get(URL)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get(URL).param("data", "2030-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].id").value(id));
        mvc.perform(get(URL).param("data", "2030-01-14"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2030-01-15T10:00:00", "2030-01-15T10:15:00", "2030-01-15T09:45:00"})
    void impedirSobreposicao(String inicio) throws Exception {
        cadastrar("2030-01-15T10:00:00");
        mvc.perform(post(URL).contentType("application/json").content(corpo(inicio)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").exists());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void impedirIntervaloQueEnglobaOutroServico() throws Exception {
        cadastrar("2030-01-15T10:00:00");
        servicoId =
                servicos.saveAndFlush(new Servico("Completo", new BigDecimal("100.00"), 90))
                        .getId();
        mvc.perform(post(URL).contentType("application/json").content(corpo("2030-01-15T09:45:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void permitirHorariosConsecutivos() throws Exception {
        cadastrar("2030-01-15T10:00:00");
        cadastrar("2030-01-15T10:30:00");
        cadastrar("2030-01-15T09:30:00");
    }

    @Test
    void conflitoAtravessaMeiaNoite() throws Exception {
        cadastrar("2030-01-15T23:45:00");
        mvc.perform(post(URL).contentType("application/json").content(corpo("2030-01-16T00:00:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void confirmarCancelarELiberarHorario() throws Exception {
        String id = cadastrar("2030-01-15T10:00:00");
        for (int i = 0; i < 2; i++) {
            mvc.perform(put(URL + "/" + id + "/confirmar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMADO"));
        }
        mvc.perform(post(URL).contentType("application/json").content(corpo("2030-01-15T10:00:00")))
                .andExpect(status().isConflict());
        for (int i = 0; i < 2; i++) {
            mvc.perform(put(URL + "/" + id + "/cancelar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELADO"));
        }
        mvc.perform(get(URL + "/" + id)).andExpect(jsonPath("$.status").value("CANCELADO"));
        cadastrar("2030-01-15T10:00:00");
        mvc.perform(put(URL + "/" + id + "/confirmar")).andExpect(status().isConflict());
    }

    @Test
    void rejeitarClienteOuServicoInexistente() throws Exception {
        UUID original = clienteId;
        clienteId = UUID.randomUUID();
        mvc.perform(post(URL).contentType("application/json").content(corpo("2030-01-15T10:00:00")))
                .andExpect(status().isBadRequest());
        clienteId = original;
        servicoId = UUID.randomUUID();
        mvc.perform(post(URL).contentType("application/json").content(corpo("2030-01-15T10:00:00")))
                .andExpect(status().isBadRequest());
        assertThat(repository.count()).isZero();
    }

    @Test
    void rejeitarServicoInativo() throws Exception {
        Servico servico = servicos.findById(servicoId).orElseThrow();
        servico.desativar();
        servicos.saveAndFlush(servico);
        mvc.perform(post(URL).contentType("application/json").content(corpo("2030-01-15T10:00:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Serviço inativo"));
    }

    @Test
    void rejeitarCamposEFormatosInvalidos() throws Exception {
        mvc.perform(post(URL).contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
        mvc.perform(get(URL).param("data", "abc")).andExpect(status().isBadRequest());
        mvc.perform(get(URL + "/abc")).andExpect(status().isBadRequest());
        var invalido =
                new AgendamentoInput(
                        clienteId,
                        servicoId,
                        LocalDateTime.parse("2030-01-15T10:00:00"),
                        "a".repeat(1001));
        mvc.perform(
                        post(URL)
                                .contentType("application/json")
                                .content(mapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retornar404ParaAgendamentoInexistente() throws Exception {
        String path = URL + "/" + UUID.randomUUID();
        mvc.perform(get(path)).andExpect(status().isNotFound());
        mvc.perform(put(path + "/confirmar")).andExpect(status().isNotFound());
        mvc.perform(put(path + "/cancelar")).andExpect(status().isNotFound());
    }

    @Test
    void apenasUmaReservaSimultaneaPodeSerCriada() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch prontas = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);
        Callable<Integer> tentativa =
                () -> {
                    prontas.countDown();
                    if (!iniciar.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Espera excedida");
                    }
                    try {
                        service.salvar(
                                agendamentoInputDisassembler.toDomainObject(
                                        request("2030-01-15T10:00:00")));
                        return 201;
                    } catch (ConflitoException ex) {
                        return 409;
                    }
                };
        try {
            Future<Integer> primeira = executor.submit(tentativa);
            Future<Integer> segunda = executor.submit(tentativa);
            assertThat(prontas.await(5, TimeUnit.SECONDS)).isTrue();
            iniciar.countDown();
            assertThat(
                            List.of(
                                    primeira.get(10, TimeUnit.SECONDS),
                                    segunda.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
            assertThat(repository.count()).isEqualTo(1);
        } finally {
            iniciar.countDown();
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void bancoRejeitaSobreposicaoMesmoSemPassarPeloService() {
        Cliente cliente = clientes.findById(clienteId).orElseThrow();
        Servico corte = servicos.findById(servicoId).orElseThrow();
        Servico barba = servicos.saveAndFlush(new Servico("Barba", new BigDecimal("25.00"), 30));
        LocalDateTime inicio = LocalDateTime.parse("2030-01-15T10:00:00");
        repository.saveAndFlush(
                new Agendamento(cliente, corte, inicio, inicio.plusMinutes(30), null));

        var erro =
                Assertions.assertThrows(
                        org.springframework.dao.DataIntegrityViolationException.class,
                        () -> {
                            repository.saveAndFlush(
                                    new Agendamento(
                                            cliente,
                                            barba,
                                            inicio.plusMinutes(15),
                                            inicio.plusMinutes(45),
                                            null));
                        });
        java.sql.SQLException causa = (java.sql.SQLException) erro.getMostSpecificCause();
        assertThat(causa.getSQLState()).isEqualTo("23P01");
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void reservasSimultaneasDeServicosDiferentesRetornam201E409() throws Exception {
        UUID outroServico =
                servicos.saveAndFlush(new Servico("Barba", new BigDecimal("25.00"), 30)).getId();
        String primeiro = corpo("2030-01-15T10:00:00");
        String segundo =
                mapper.writeValueAsString(
                        new AgendamentoInput(
                                clienteId,
                                outroServico,
                                LocalDateTime.parse("2030-01-15T10:15:00"),
                                null));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        java.util.concurrent.CyclicBarrier inicio = new java.util.concurrent.CyclicBarrier(2);
        try {
            Callable<Integer> criarPrimeiro =
                    () -> {
                        inicio.await(5, TimeUnit.SECONDS);
                        return mvc.perform(
                                        post(URL).contentType("application/json").content(primeiro))
                                .andReturn()
                                .getResponse()
                                .getStatus();
                    };
            Callable<Integer> criarSegundo =
                    () -> {
                        inicio.await(5, TimeUnit.SECONDS);
                        return mvc.perform(
                                        post(URL).contentType("application/json").content(segundo))
                                .andReturn()
                                .getResponse()
                                .getStatus();
                    };
            Future<Integer> a = executor.submit(criarPrimeiro);
            Future<Integer> b = executor.submit(criarSegundo);
            assertThat(List.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
            assertThat(repository.count()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
