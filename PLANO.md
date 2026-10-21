# Plano - feedback-lambda-notificacao

Tarefas extraídas da planilha de planejamento. Siga a ordem e respeite "Depende de".
Ao validar uma tarefa, troque `[ ]` por `[x]`.

Como pedir ao Claude: `Execute a tarefa X.Y do PLANO.md` (uma por sessão; use /clear entre tarefas).

## Tarefas deste repositório

### Fase 4 - Notificação

- [ ] **4.5** NotificationHandler.java: listar os e-mails do grupo admins no Cognito, montar o e-mail (description, urgency, submitted at) e enviar pelo SES
  - Validar: Teste local gera o corpo do e-mail esperado
  - Depende de: 4.3

- [ ] **4.6** Testes unitários (JUnit5 + Mockito, mockando Cognito e SES)
  - Validar: mvn test verde
  - Depende de: 4.5

- [ ] **4.7** Workflow de CI/CD (copiar o modelo da ingestão)
  - Validar: Push na main atualiza a função
  - Depende de: 4.4, 4.6

### Fase 7 - Documentação e entrega

- [ ] **7.4** README da função: gatilho, dados do e-mail, variáveis de ambiente, permissões e como testar
  - Validar: README completo
  - Depende de: 4.8

## Pré-requisitos em outros repositórios (não fazer aqui)

Confirme com o time que estas tarefas já foram concluídas antes de começar.

- **4.3** (feedback-infra): Função de notificação com gatilho do Streams (só INSERT com urgência alta), DLQ e permissões ListUsersInGroup e ses:SendEmail
- **4.4** (feedback-infra): Role OIDC do repositório feedback-lambda-notificacao, só com lambda:UpdateFunctionCode nesta função
- **4.8** (grupo): Teste ponta a ponta: enviar uma nota baixa e receber o e-mail de urgência
