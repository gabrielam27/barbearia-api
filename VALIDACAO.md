# Validação executada

- 40 testes automatizados aprovados com PostgreSQL 16.15.
- Duas instâncias HTTP no mesmo PostgreSQL: reservas simultâneas retornaram 201 e 409; somente uma reserva foi criada.
- Cliente, serviço, agendamento e status CONFIRMADO preservados após reiniciar a aplicação e o PostgreSQL.
- Exclusão de registros com histórico retornou 409; cancelamento liberou o horário para nova reserva.

Os testes PostgreSQL usaram um banco isolado local, encerrado após a validação. Não foi instalado um serviço de banco no sistema.
