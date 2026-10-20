package com.barbearia;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.barbearia.domain.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClienteApiTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ClienteRepository repository;

    @Autowired
    ObjectMapper mapper;

    private static final String URL = "/api/v1/clientes";

    private static final String CLIENTE =
            "{\"nome\":\"João Silva\",\"telefone\":\"11987654321\",\"email\":\"joao@email.com\"}";

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    @Test
    void cadastrarListarEBuscar() throws Exception {
        String resposta =
                mvc.perform(post(URL).contentType("application/json").content(CLIENTE))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.dataCriacao").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        String id = mapper.readTree(resposta).get("id").asText();
        mvc.perform(get(URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefone").value("11987654321"));
        mvc.perform(get(URL)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void rejeitarTelefoneDuplicado() throws Exception {
        mvc.perform(post(URL).contentType("application/json").content(CLIENTE))
                .andExpect(status().isCreated());
        mvc.perform(post(URL).contentType("application/json").content(CLIENTE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Telefone já cadastrado"));
    }

    @Test
    void rejeitarCamposInvalidos() throws Exception {
        mvc.perform(
                        post(URL)
                                .contentType("application/json")
                                .content(
                                        "{\"nome\": \" \", \"telefone\": \"\", \"email\": \"invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void retornar404() throws Exception {
        mvc.perform(get(URL + "/00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Cliente não encontrado"));
    }

    @Test
    void rejeitarIdEJsonInvalidos() throws Exception {
        mvc.perform(get(URL + "/abc")).andExpect(status().isBadRequest());
        mvc.perform(post(URL).contentType("application/json").content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void aceitarEmailOpcional() throws Exception {
        mvc.perform(
                        post(URL)
                                .contentType("application/json")
                                .content("{\"nome\":\"Ana\",\"telefone\":\"11999999999\"}"))
                .andExpect(status().isCreated());
    }
}
