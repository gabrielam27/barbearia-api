-- Uma reserva ocupa [inicio, fim): outra pode começar exatamente quando ela termina.
-- EXCLUDE impede dois intervalos sobrepostos, mesmo se forem salvos ao mesmo tempo.
-- Agendamentos cancelados não participam dessa regra.
-- O bloco abaixo só cria a regra se ela ainda não existir; não apaga nenhum dado.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'horario_sem_sobreposicao'
          AND conrelid = 'agendamentos'::regclass
    ) THEN
        ALTER TABLE agendamentos
        ADD CONSTRAINT horario_sem_sobreposicao
        EXCLUDE USING gist (tsrange(data_hora, data_hora_fim, '[)') WITH &&)
        WHERE (status <> 'CANCELADO');
    END IF;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;
@@
