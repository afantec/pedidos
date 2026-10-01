# pedidos

Microsserviço piloto da esteira descrita em [AnaliseTecnicaDetalhada-CICD.md](../AnaliseTecnicaDetalhada-CICD.md):
API REST + interface PrimeFaces no mesmo `app.jar`, PostgreSQL com esquema próprio e migrações Flyway.

| Item | Valor |
| --- | --- |
| Stack | Java 21, Spring Boot 3.5, Spring Data JPA, JoinFaces 5.5 (Jakarta Faces 4 + PrimeFaces 15), Flyway, springdoc |
| Banco | `plataforma`, esquema `pedidos`, usuário `pedidos_app` |
| Interface | `/` → `index.xhtml`, `clientes.xhtml`, `produtos.xhtml`, `pedidos.xhtml`, `pedido.xhtml` |
| API | `/api/v1/clientes`, `/api/v1/produtos`, `/api/v1/pedidos` — documentação em `/swagger-ui.html` |
| Saúde | `/actuator/health` (usado pela esteira), `/actuator/info` (versão do build) |

## Estrutura

```text
src/main/java/br/com/plataforma/pedidos/
├── cliente/   produto/   pedido/     # entidade, repositório, serviço, DTOs e controller REST de cada domínio
├── comum/                            # exceções, validação de CPF/CNPJ, tratamento de erros (RFC 9457)
├── config/                           # Spring MVC, paginação, OpenAPI
└── web/                              # managed beans das telas PrimeFaces (escopo de view)
src/main/resources/
├── META-INF/resources/*.xhtml        # telas
├── db/migration/                     # migrações Flyway (produção)
└── db/dev-data/                      # dados de exemplo, só no perfil local
db/setup-banco.sql                    # cria banco, usuário e esquema (DBA, uma vez)
infra/                                # systemd, sudoers, app.env e preparo do servidor
scripts/ + .gitlab-ci.yml             # esteira (seção 5 da análise)
```

## Regras de negócio

- Pedido nasce `ABERTO`; só pedido aberto pode ser alterado ou confirmado. Confirmado ou aberto pode ser cancelado.
- Pedido precisa de ao menos um item; linhas do mesmo produto são somadas.
- O item guarda o preço do produto no momento em que foi gravado. Ao editar um pedido aberto, os itens são regravados com o preço atual.
- Cliente e produto inativos não entram em pedidos novos. Quem já tem pedidos não pode ser excluído, só inativado.
- Documento do cliente: CPF ou CNPJ válido, só dígitos, único.

## Ambiente local

Pré-requisitos: JDK 21, Maven 3.6.3+, PostgreSQL (aqui: `localhost:5433`), Docker para os testes de integração.

1. Criar banco, usuário e esquema (pede a senha do `postgres`). A senha do `pedidos_app` vem de `config/application-local.yml`, criado a partir do `.example`:

   ```bash
   psql -h localhost -p 5433 -U postgres -d postgres -v app_password="$(sed -n 's/^ *password: *//p' config/application-local.yml)" -f db/setup-banco.sql
   ```

2. Rodar a aplicação com o perfil `local` (aplica as migrações e os dados de exemplo):

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.jvmArguments=-Dserver.port=8081
   ```

3. Abrir `http://localhost:8081/`.

Testes: `mvn verify` roda os unitários e o `PedidosApplicationIT` (PostgreSQL 17 via Testcontainers; é ignorado sem Docker).

## Servidor

Como root, no servidor de destino: `infra/preparar-servidor.sh`, depois preencher `shared/app.env`. O deploy é feito pela esteira (`scripts/deploy.sh`).

## Esteira

- **Merge Request:** roda `test` e `package`.
- **Push na branch de deploy (`main`):** roda `test` e `package`. O `deploy` fica aguardando liberação, que é feita no painel da esteira ([../esteira](../esteira/)) escolhendo a versão. O rollback também é feito pelo painel.
- **Configuração deste projeto no GitLab:**
  - Serviço cadastrado no painel da esteira (botão Novo serviço), que grava as variáveis de deploy no projeto.
  - Environment `producao` protegido, com quem pode implantar.
  - *Prevent outdated deployment jobs* desligado.
  - Resource group em ordem de chegada:

    ```bash
    curl --request PUT --header "PRIVATE-TOKEN: <token>" "https://<gitlab>/api/v4/projects/<id>/resource_groups/producao" --data "process_mode=oldest_first"
    ```

- **Runner de build:** um runner *protected* não executa pipelines de MR. Quando existir um runner de build separado, troque `BUILD_RUNNER_TAG` no `.gitlab-ci.yml`.
- **Testes dos scripts** (systemd, sudo e curl simulados em container):

  ```bash
  docker run --rm -v "$PWD/scripts:/scripts:ro" --entrypoint bash debian:bookworm-slim /scripts/teste-esteira.sh
  ```

## GitHub Actions

O mesmo fluxo da esteira, para quando o repositório está no GitHub:

| Workflow | Quando roda | O que faz |
| --- | --- | --- |
| [ci.yml](.github/workflows/ci.yml) | Pull request e push na `main` | `test` (com Testcontainers, em runner do GitHub). No push, também `package`, que guarda o `app.jar` como artefato `app-jar` por 30 dias |
| [deploy.yml](.github/workflows/deploy.yml) | Disparado pelo painel da esteira, ou em *Actions → deploy → Run workflow* | `acao=deploy`: baixa o `app-jar` da execução `versao` e roda `scripts/deploy.sh`. `acao=rollback`: roda `scripts/rollback.sh`. Executa no runner *self-hosted* do servidor |

Os valores de deploy vêm das *Actions variables* do repositório, gravadas pelo cadastro do painel. No servidor:

```bash
RUNNER_USER=<usuário do runner do GitHub> infra/preparar-servidor.sh
```
