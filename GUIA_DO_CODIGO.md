# Entendendo a barbearia pelo seu Algafood

A referência usada foi `/home/janu/Downloads/algafood-api`. O projeto da barbearia segue seus padrões de organização, nomes e responsabilidades. O Algafood não foi alterado.

## Compare as classes

| No Algafood | Na barbearia | Função |
| --- | --- | --- |
| `CozinhaController` | `ClienteController` | Receber e responder requisições |
| `CozinhaInput` | `ClienteInput` | Definir os campos recebidos |
| `CozinhaInputDisassembler` | `ClienteInputDisassembler` | Transformar entrada em entidade |
| `Cozinha` | `Cliente` | Representar o registro do banco |
| `CadastroCozinhaService` | `CadastroClienteService` | Aplicar as regras e salvar/excluir |
| `CozinhaRepository` | `ClienteRepository` | Consultar e gravar no banco |
| `CozinhaModelAssembler` | `ClienteModelAssembler` | Transformar entidade em resposta |
| `CozinhaModel` | `ClienteModel` | Definir os campos devolvidos |
| `FluxoPedidoService` | `FluxoAgendamentoService` | Confirmar e cancelar |

## O cadastro, passo a passo

O método em `api/controller/ClienteController.java` segue o mesmo formato do cadastro de cozinha:

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public ClienteModel adicionar(@RequestBody @Valid ClienteInput clienteInput) {
    Cliente cliente = clienteInputDisassembler.toDomainObject(clienteInput);
    cliente = cadastroCliente.salvar(cliente);
    return clienteModelAssembler.toModel(cliente);
}
```

1. O Postman envia nome, telefone e email.
2. O Spring preenche `ClienteInput`. `@Valid` verifica nome obrigatório, email válido e outras validações.
3. O disassembler cria a entidade `Cliente` e copia os campos permitidos.
4. `CadastroClienteService.salvar` verifica telefone duplicado e usa o repository para salvar.
5. O assembler cria um `ClienteModel` com os campos da resposta.
6. `@ResponseStatus(HttpStatus.CREATED)` faz a resposta retornar 201.

Não há montagem de URL nem cabeçalho Location. O corpo continua no formato do desafio.

## Por que Input, entidade e Model?

São três papéis, como no Algafood:

- **Input:** o que o usuário pode enviar. Exemplo: nome e telefone, mas não ID ou data de criação.
- **Entidade:** o que fica salvo, com suas anotações JPA.
- **Model:** o que sai no JSON da resposta.

Essa separação aumenta o número de arquivos, mas permite abrir cada classe e ver uma responsabilidade específica.

Os assemblers e disassemblers usam cópias explícitas com getters e setters. O seu Algafood usa ModelMapper para automatizar essas cópias. Aqui elas estão escritas campo a campo para facilitar a leitura e garantir que ID, datas e status não sejam substituídos pela entrada do usuário.

## O que é buscarOuFalhar?

É o mesmo padrão do Algafood:

```java
public Cliente buscarOuFalhar(UUID clienteId) {
    return clienteRepository.findById(clienteId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente não encontrado"));
}
```

`findById` procura no banco. Se achar, devolve o cliente. Se não achar, lança a exceção, que o handler transforma em 404 com JSON.

## Como funciona a edição?

O controller:

1. Busca o registro com `buscarOuFalhar`.
2. Copia os campos do input para o registro existente.
3. Chama `salvar`.
4. Converte o resultado para Model.

A cópia do input não altera ID nem data de criação. Na edição de serviço, o campo `ativo` também é obrigatório: false desativa, true reativa.

## E a exclusão?

O controller usa `@ResponseStatus(HttpStatus.NO_CONTENT)` e chama `excluir`.

O serviço busca o registro e tenta excluir. Se o PostgreSQL impedir a exclusão porque existe agendamento relacionado, o serviço captura `DataIntegrityViolationException` e lança `EntidadeEmUsoException`, como no Algafood. A resposta é 409.

Isso vale mesmo para agendamentos cancelados: cancelar libera o horário, mas mantém o histórico. Se não houver vínculo, a exclusão retorna 204.

## Lombok e @Autowired

O projeto usa `@Getter` e `@Setter`, como a referência. O Lombok gera os métodos `getNome()`, `setNome(...)` e os demais durante a compilação. `@NoArgsConstructor` gera um construtor sem argumentos, e `@AllArgsConstructor` gera um com todos os campos.

`@Autowired` pede ao Spring para fornecer o objeto usado naquele campo. Exemplo: o controller recebe `CadastroClienteService` sem precisar criar `new CadastroClienteService()`.

## Agendamentos

`CadastroAgendamentoService.salvar` faz as verificações:

1. Cliente existe?
2. Serviço existe e está ativo?
3. Qual é o fim do atendimento, considerando a duração?
4. Existe outra reserva sobreposta?
5. Se tudo estiver correto, salva com status AGENDADO.

`FluxoAgendamentoService` cuida de confirmar/cancelar, como `FluxoPedidoService` na referência. A entidade `Agendamento` impede confirmar um cancelado. As respostas desses endpoints continuam sendo 200 com o JSON, porque é o que o desafio solicita.

## Parte específica deste desafio: conflito de horários

A barbearia tem uma única agenda. Um atendimento das 10:00 às 10:30 bloqueia outro às 10:15, mas permite outro às 10:30.

A consulta no service detecta conflitos existentes. O arquivo `schema.sql` adiciona uma regra do PostgreSQL que impede duas reservas sobrepostas de serem salvas simultaneamente, inclusive para serviços diferentes.

Há também bloqueios por registro em `buscarParaAlterar`: eles evitam editar a duração de um serviço durante uma reserva e evitam confirmar/cancelar o mesmo registro ao mesmo tempo. Não existe a antiga classe `AgendaLockService` nem a chamada `agenda.bloquear()`.

## Tratamento de erros

As exceções do domínio não escolhem o status HTTP. Essa tarefa fica em `api/exceptionhandler/ApiExceptionHandler`:

| Exceção | Resposta |
| --- | --- |
| `NegocioException` | 400 |
| `EntidadeNaoEncontradaException` | 404 |
| `EntidadeEmUsoException` | 409 |
| `ConflitoException` | 409 |

O formato continua sendo `erro` e `timestamp`, como no desafio. O handler também traduz falhas de validação, JSON inválido e violações das regras do banco.

## O que continua diferente do Algafood?

- PostgreSQL em vez de MySQL, conforme o desafio.
- Java 17 e Spring Boot 3.5.13: por isso as anotações usam `jakarta`, enquanto a referência usa `javax`.
- UUID em vez de Long, mantendo os IDs já usados na API. O desafio permite ambos.
- Conversões explícitas em vez de ModelMapper.
- Apenas as funcionalidades da barbearia: não foram copiados filtros, relatórios ou outras funcionalidades do Algafood.

## Ordem para estudar

Abra `ClienteController` e siga a chamada de cada linha do método `adicionar`. Depois faça o mesmo com `atualizar` e `remover`. Quando entender cliente, repita com serviço e, por último, agendamento.

Os pedidos da coleção do Postman continuam iguais; o que mudou foi a organização interna do Java.
