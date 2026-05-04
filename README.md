# reMediar API

Backend do reMediar, um sistema web para intermediar doacoes institucionais de medicamentos entre doadores, ONGs validadas e beneficiarios. A aplicacao evita fluxo P2P, aplica barreiras sanitarias e mantem rastreabilidade do lote do medicamento ate a instituicao responsavel.

## Stack

- Java 17+
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Security com RBAC
- Bean Validation
- Flyway
- H2 para desenvolvimento local
- PostgreSQL para producao
- Maven Wrapper

## Arquitetura

O projeto segue separacao por responsabilidade:

- `domain`: entidades, enums e contratos de repositorio.
- `application`: casos de uso, regras de negocio e portas.
- `infrastructure`: configuracoes, seguranca, OCR simplificado, geracao de codigo e scheduler.
- `web`: controllers REST, DTOs e tratamento padronizado de erros.
- `resources/db/migration`: migracoes versionadas do banco.

## Funcionalidades Implementadas

- Cadastro de medicamento com validacao de lote, validade, fotos e declaracao de armazenamento.
- Bloqueio automatico de medicamentos com validade inferior a 45 dias.
- Blacklist para medicamentos de controle especial.
- Alteracao restrita a quantidade e fotos enquanto o item estiver disponivel.
- Cancelamento logico, preservando historico.
- Pesquisa por nome, principio ativo, lote e validade.
- Consulta de ONGs ativas dentro do raio configurado.
- Criacao de match de doacao entre medicamento, doador e ONG.
- Aceite institucional com geracao de codigo unico para QR Code.
- Confirmacao de entrega fisica por codigo de validacao.
- Expiracao automatica de matches vencidos apos 5 dias uteis.
- Auditoria append-only para rastreabilidade.

## Execucao Local

Requisitos:

- JDK 17 ou superior instalado e configurado no `JAVA_HOME`.
- Acesso a internet na primeira execucao para download das dependencias Maven.

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

A API sobe por padrao em:

```text
http://localhost:8080
```

Healthcheck:

```text
GET /actuator/health
```

## Testes

Windows:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

## Endpoints Principais

- `POST /api/v1/medications/ocr-preview`
- `POST /api/v1/medications`
- `PATCH /api/v1/medications/{id}`
- `DELETE /api/v1/medications/{id}`
- `GET /api/v1/medications`
- `GET /api/v1/institutions/nearby`
- `POST /api/v1/donations`
- `PATCH /api/v1/donations/{id}/institution`
- `POST /api/v1/donations/{id}/accept`
- `POST /api/v1/donations/{id}/confirm-delivery`
- `DELETE /api/v1/donations/{id}`
- `GET /api/v1/donations/institution/{institutionId}`

## Perfis de Acesso

- `DONOR`: cadastro de medicamentos e abertura de matches.
- `INSTITUTION`: aceite de doacoes, consulta de doacoes destinadas e confirmacao de entrega.
- `ADMIN`: operacoes administrativas e intervencao em fluxos.

## Configuracao de Producao

Use o profile `prod` com as variaveis:

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
```

Exemplo:

```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

## Observacoes de Seguranca

A autenticacao atual e adequada apenas para desenvolvimento inicial. Para producao, substituir por JWT/OAuth2, vincular o usuario autenticado aos identificadores de doador/ONG, externalizar credenciais e integrar um servico de armazenamento seguro para as imagens dos medicamentos.
