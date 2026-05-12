# reMediar API

Aplicacao full stack do reMediar para intermediar doacoes institucionais de medicamentos entre doadores, ONGs validadas e beneficiarios. O sistema evita fluxo P2P, aplica barreiras sanitarias e mantem rastreabilidade do lote do medicamento ate a instituicao responsavel.

## Stack

- Java 17+
- Spring Boot 4
- Angular 20
- Spring Web MVC
- Spring Data JPA
- Spring Security com RBAC
- Bean Validation
- Flyway
- H2 para desenvolvimento local
- Maven Wrapper

## Arquitetura

O projeto segue separacao por responsabilidade:

- `domain`: entidades, enums e contratos de repositorio.
- `application`: casos de uso, regras de negocio e portas.
- `infrastructure`: configuracoes, seguranca, OCR simplificado, geracao de codigo e scheduler.
- `web`: controllers REST, DTOs e tratamento padronizado de erros.
- `resources/db/migration`: migracoes versionadas do banco.
- `frontend`: interface Angular standalone integrada aos endpoints REST.

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
- Interface Angular minimalista para login, cadastro, consulta, match, fila institucional e administracao.

## Execucao Local

Requisitos:

- JDK 17 ou superior instalado e configurado no `JAVA_HOME`.
- Acesso a internet na primeira execucao para download das dependencias Maven.
- Node.js 22+ e npm para rodar o frontend Angular localmente.

Backend no Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend no Linux/macOS:

```bash
./mvnw spring-boot:run
```

A API sobe por padrao em:

```text
http://localhost:8081
```

Healthcheck:

```text
GET /actuator/health
```

Frontend local:

```bash
cd frontend
npm install
npm start
```

O frontend usa proxy local para a API e sobe em `http://localhost:4200`.

## Testes

Windows:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

Testes do frontend:

```bash
cd frontend && npm test
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

## Observacoes de Seguranca

A API usa login em `/api/v1/auth/login` e autentica as demais rotas com `Authorization: Bearer <token>`. Para uso local, o segredo JWT padrao fica em `application.yml`; voce pode sobrescrever com `REMEDIAR_JWT_SECRET` se quiser testar outro valor.
