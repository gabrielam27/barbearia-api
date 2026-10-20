package com.barbearia.domain.exception;

public class ConflitoException extends NegocioException {
    private static final long serialVersionUID = 1L;

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
