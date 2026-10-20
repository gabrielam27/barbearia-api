package com.barbearia.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.barbearia.domain.exception.ConflitoException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "agendamentos",
        indexes = @Index(name = "idx_agendamento_data", columnList = "dataHora"))
public class Agendamento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    // Guarda o fim reservado, mesmo se a duração do serviço mudar depois.
    @Column(nullable = false)
    private LocalDateTime dataHoraFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    @Column(length = 1000)
    private String observacoes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    public Agendamento(
            Cliente cliente,
            Servico servico,
            LocalDateTime inicio,
            LocalDateTime fim,
            String observacoes) {
        this.cliente = cliente;
        this.servico = servico;
        this.dataHora = inicio;
        this.dataHoraFim = fim;
        this.observacoes = observacoes;
    }

    @PrePersist
    void preencherDataCriacao() {
        dataCriacao = LocalDateTime.now();
    }

    public void confirmar() {
        if (status == StatusAgendamento.CANCELADO) {
            throw new ConflitoException("Agendamento cancelado não pode ser confirmado");
        }
        status = StatusAgendamento.CONFIRMADO;
    }

    public void cancelar() {
        status = StatusAgendamento.CANCELADO;
    }
}
