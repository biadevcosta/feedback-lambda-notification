# feedback-lambda-notificacao

## Projeto
Plataforma de feedback de aulas (Tech Challenge, fase 4), serverless na AWS. O aluno envia `POST /avaliacao` com `{"descricao": string, "nota": int 0 a 10}`. Os administradores recebem e-mail de urgência e um relatório semanal.

## Os 4 repositórios
- feedback-infra: Terraform de toda a infraestrutura e a documentação geral
- feedback-lambda-ingestao: recebe a avaliação e grava no DynamoDB
- feedback-lambda-notificacao: avisa os admins por e-mail quando a urgência é alta
- feedback-lambda-relatorio: gera e envia o relatório semanal

## Convenções (definidas na tarefa 0.5; ajuste aqui se o grupo mudar)
- Região AWS: sa-east-1 (sugestão; confirmar). Prefixo de nomes: `feedback-`
- Funções: `feedback-ingestao`, `feedback-notificacao`, `feedback-relatorio`. Python 3.12
- Grupos do Cognito: `alunos` e `admins`

## Contrato de dados (igual nos 4 repositórios)
Tabela `Avaliacoes`, chave `avaliacaoId` (string, UUID). Atributos: `usuarioId` (sub do Cognito), `descricao`, `nota` (número), `urgencia` (`alta`, `media` ou `baixa`), `dataEnvio` (ISO 8601 em UTC) e `dia` (AAAA-MM-DD no horário de Brasília, UTC-3 fixo).
Índice `por-dia`: chave `dia`, ordenação `dataEnvio`. Urgência (sugestão, editável): nota 0 a 3 alta, 4 a 6 media, 7 a 10 baixa.

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
- Estrutura: `src/handler.py` (`lambda_handler`), `tests/`, `requirements-dev.txt`, `.github/workflows/ci-cd.yml` e `README.md`.
- Variáveis de ambiente: `USER_POOL_ID`, `ADMIN_GROUP` (`admins`) e `SES_SENDER`.
- O filtro (INSERT e urgencia alta) é feito no Terraform. No código, ignore também registros que não sejam INSERT com urgência alta (defesa).
- Os registros do Stream vêm em formato DynamoDB JSON: use `boto3.dynamodb.types.TypeDeserializer`.
- Destinatários: `cognito-idp list_users_in_group` (com paginação), atributo `email` dos usuários com `email_verified` verdadeiro.
- E-mail: assunto claro (`[Feedback crítico] ...`) e corpo com Descrição, Urgência e Data de envio (dd/mm/aaaa HH:MM, horário de Brasília). Texto simples mais HTML simples.
- Falha: deixar a exceção subir. Os retries do mapeamento e a DLQ (SQS) são configurados no Terraform. Não engolir erros.
- Permissões: `cognito-idp:ListUsersInGroup` (só neste User Pool) e `ses:SendEmail` (só a identidade do remetente).
- Logar só contagens e IDs. Esteira e testes como na ingestão (moto para cognito-idp e SES). O SES em sandbox só envia para e-mails verificados.
