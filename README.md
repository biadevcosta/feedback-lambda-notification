# feedback-lambda-notificacao

Lambda de **notificação** da Plataforma de Feedback de Aulas (Tech Challenge fase 4).

Disparada pelo **DynamoDB Streams** quando uma avaliação com urgência `alta` é inserida. Busca os e-mails do grupo `admins` no Cognito e envia o aviso pelo SES.

- **Função AWS:** `feedback-notificacao` · **Runtime:** Python 3.12 · **Região:** `sa-east-1`
- **Gatilho:** DynamoDB Streams (filtro: `INSERT` + `urgencia = alta`), com fila SQS como destino de falha
- **Permissões:** `cognito-idp:ListUsersInGroup` e `ses:SendEmail`

## Conteúdo do e-mail

Descrição da avaliação, urgência e data de envio.

## Estrutura (em construção)

```
src/handler.py          # lambda_handler
tests/                  # pytest com mocks do Cognito e do SES
requirements-dev.txt
.github/workflows/ci-cd.yml
```

## Repositórios relacionados

`feedback-infra` · `feedback-lambda-ingestao` · `feedback-lambda-relatorio`

## Status

Acompanhe o andamento em [PLANO.md](PLANO.md).
