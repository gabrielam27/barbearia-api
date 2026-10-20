package com.barbearia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.barbearia.domain.model.Servico;
import com.barbearia.domain.repository.ServicoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServicoApiTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ServicoRepository repository;

    private static final String URL = "/api/v1/servicos";

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    @Test
    void cadastrarEListar() throws Exception {
        mvc.perform(
                        post(URL)
                                .contentType("application/json")
                                .content(
                                        """
            {"nome":"  Corte Simples  ","preco":50.25,"duracao":30}
            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Corte Simples"))
                .andExpect(jsonPath("$.preco").value(50.25))
                .andExpect(jsonPath("$.duracao").value(30))
                .andExpect(jsonPath("$.ativo").value(true));
        assertThat(repository.findAll().get(0).getPreco()).isEqualByComparingTo("50.25");
        mvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Corte Simples"));
    }

    @Test
    void listarSomenteAtivos() throws Exception {
        Servico inativo = new Servico("Antigo", new BigDecimal("20.00"), 15);
        inativo.desativar();
        repository.saveAndFlush(inativo);
        repository.saveAndFlush(new Servico("Barba", new BigDecimal("30.00"), 20));
        mvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Barba"));
    }

    @Test
    void retornarListaVazia() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"nome\": \" \", \"preco\": 10, \"duracao\": 30}",
                "{\"nome\":\"Corte\",\"preco\":-1,\"duracao\":30}",
                "{\"nome\":\"Corte\",\"preco\":10.999,\"duracao\":30}",
                "{\"nome\":\"Corte\",\"preco\":10000000000,\"duracao\":30}",
                "{\"nome\":\"Corte\",\"preco\":10,\"duracao\":0}",
                "{\"nome\":\"Corte\",\"preco\":10,\"duracao\":-1}"
            })
    void rejeitarDadosInvalidos(String corpo) throws Exception {
        mvc.perform(post(URL).contentType("application/json").content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
        assertThat(repository.count()).isZero();
    }

    @Test
    void permitirServicoGratuito() throws Exception {
        mvc.perform(
                        post(URL)
                                .contentType("application/json")
                                .content(
                                        """
            {"nome":"Corte promocional","preco":0,"duracao":30}
            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.preco").value(0));
    }
}
