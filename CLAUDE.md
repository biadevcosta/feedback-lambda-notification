# feedback-lambda-notificacao

## Projeto
Plataforma de feedback de aulas (Tech Challenge, fase 4), serverless na AWS. O aluno envia `POST /evaluations` com `{"description": string, "score": int 0 a 10}`. Os administradores recebem e-mail de urgência e um relatório semanal.

## Os 4 repositórios
- feedback-infra: Terraform de toda a infraestrutura e a documentação geral
- feedback-lambda-ingestao: recebe a avaliação e grava no DynamoDB
- feedback-lambda-notificacao: avisa os admins por e-mail quando a urgência é alta
- feedback-lambda-relatorio: gera e envia o relatório semanal

## Convenções (definidas na tarefa 0.5; ajuste aqui se o grupo mudar)
- Região AWS: sa-east-1 (São Paulo) - CONFIRMADA. Prefixo de nomes: `feedback-`
- Funções: `feedback-ingestao`, `feedback-notificacao`, `feedback-relatorio`. Java 21 (Maven), AWS SDK v2
- Grupos do Cognito: `students` e `admins`
- Idioma: código Java, comentários, nomes de variáveis/recursos de código e README em inglês. CLAUDE.md e PLANO.md continuam em português.
- Custos: a conta AWS **não tem free tier**. Antes de pedir pra criar/alterar qualquer recurso na AWS (fora do Terraform da infra, ex.: testar algo manualmente), avise o custo estimado e peça confirmação.

## Contrato de dados (igual nos 4 repositórios)
Tabela `Evaluations`, chave `evaluationId` (string, UUID). Atributos: `userId` (sub do Cognito), `description`, `score` (número), `urgency` (`high`, `medium` ou `low`), `submittedAt` (ISO 8601 em UTC) e `day` (AAAA-MM-DD no horário de Brasília, UTC-3 fixo).
Índice `by-day`: chave `day`, ordenação `submittedAt`. Urgência (sugestão, editável): score 0 a 3 high, 4 a 6 medium, 7 a 10 low.

## Regras de trabalho
- Uma tarefa por vez do PLANO.md, só deste repositório. Confira "Depende de" antes de começar.
- Ao terminar e validar, marque `[x]` no PLANO.md e resuma em 1 ou 2 frases.
- Nunca commitar segredos, `.env`, `*.tfstate` nem `*.tfvars`. Sem chaves fixas da AWS: a esteira usa OIDC.
- Mudanças por branch e Pull Request (a main é protegida). Commits pequenos, mensagens curtas em português.
- Respostas curtas. Não cole saídas longas (resuma plan, logs e testes).
- Em dúvida entre opções, pergunte antes de decidir.
- LGPD: não registrar e-mails nem o texto das avaliações nos logs.

## Esta função
Disparada pelo DynamoDB Streams (NEW_IMAGE) quando entra uma avaliação crítica. Avisa os admins por e-mail (SES).
- Estrutura: `pom.xml`, `src/main/java/com/feedback/notificacao/NotificationHandler.java` (implementa `RequestHandler<DynamodbEvent, Void>`), `src/test/java/...`, `.github/workflows/ci-cd.yml` e `README.md`.
- Dependências: `aws-lambda-java-core`, `aws-lambda-java-events` (já traz `DynamodbEvent` tipado), AWS SDK v2 (`cognitoidentityprovider`, `ses`), `maven-shade-plugin`.
- Variáveis de ambiente: `USER_POOL_ID`, `ADMIN_GROUP` (`admins`) e `SES_SENDER`.
- O filtro (INSERT e urgency high) é feito no Terraform. No código, ignore também registros que não sejam INSERT com urgency high (defesa).
- `aws-lambda-java-events` já desserializa os registros do Stream em `AttributeValue` tipado — não precisa parsear o DynamoDB JSON manualmente.
- Destinatários: `CognitoIdentityProviderClient.listUsersInGroup` (com paginação), atributo `email` dos usuários com `email_verified` verdadeiro.
- E-mail: assunto claro (`[Critical feedback] ...`) e corpo com Description, Urgency e Submitted at (dd/mm/aaaa HH:MM, horário de Brasília). Texto simples mais HTML simples.
- Falha: deixar a exceção subir. Os retries do mapeamento e a DLQ (SQS) são configurados no Terraform. Não engolir erros.
- Permissões: `cognito-idp:ListUsersInGroup` (só neste User Pool) e `ses:SendEmail` (só a identidade do remetente).
- Logar só contagens e IDs. Esteira como na ingestão (`mvn test` no PR; `mvn test` + `mvn package` + `update-function-code` na main).
- Testes (JUnit5 + Mockito, mockando `CognitoIdentityProviderClient` e `SesClient` — não existe "moto" em Java, o mock é direto na interface do SDK v2). O SES em sandbox só envia para e-mails verificados.
