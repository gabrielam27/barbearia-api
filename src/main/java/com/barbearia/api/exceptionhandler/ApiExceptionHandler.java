package com.barbearia.api.exceptionhandler;

import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.barbearia.api.model.ErroModel;
import com.barbearia.domain.exception.ConflitoException;
import com.barbearia.domain.exception.EntidadeEmUsoException;
import com.barbearia.domain.exception.EntidadeNaoEncontradaException;
import com.barbearia.domain.exception.NegocioException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<ErroModel> tratarNaoEncontrado(EntidadeNaoEncontradaException erro) {
        return resposta(HttpStatus.NOT_FOUND, erro.getMessage());
    }

    @ExceptionHandler({EntidadeEmUsoException.class, ConflitoException.class})
    public ResponseEntity<ErroModel> tratarConflito(NegocioException erro) {
        return resposta(HttpStatus.CONFLICT, erro.getMessage());
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErroModel> tratarNegocio(NegocioException erro) {
        return resposta(HttpStatus.BAD_REQUEST, erro.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroModel> tratarValidacao(MethodArgumentNotValidException erro) {
        List<String> mensagens = new ArrayList<>();
        for (FieldError campo : erro.getBindingResult().getFieldErrors()) {
            mensagens.add(campo.getField() + ": " + campo.getDefaultMessage());
        }
        return resposta(HttpStatus.BAD_REQUEST, String.join("; ", mensagens));
    }

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class,
        DateTimeException.class
    })
    public ResponseEntity<ErroModel> tratarFormato(Exception erro) {
        return resposta(HttpStatus.BAD_REQUEST, "Formato da requisição inválido");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroModel> tratarBanco(DataIntegrityViolationException erro) {
        Throwable causa = erro.getMostSpecificCause();
        if (causa instanceof SQLException) {
            String codigo = ((SQLException) causa).getSQLState();
            if ("23505".equals(codigo)) {
                return resposta(HttpStatus.BAD_REQUEST, "Telefone já cadastrado");
            }
            if ("23P01".equals(codigo)) {
                return resposta(
                        HttpStatus.CONFLICT,
                        "Horário indisponível. Já existe um agendamento nesse intervalo.");
            }
            if ("23503".equals(codigo)) {
                return resposta(
                        HttpStatus.CONFLICT,
                        "A operação conflita com um registro relacionado. Atualize os dados e tente novamente.");
            }
        }
        return resposta(HttpStatus.CONFLICT, "Os dados violam uma regra do banco");
    }

    private ResponseEntity<ErroModel> resposta(HttpStatus status, String mensagem) {
        ErroModel corpo = new ErroModel();
        corpo.setErro(mensagem);
        corpo.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(status).body(corpo);
    }
}
