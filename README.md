# Raízes do Nordeste — API de Pedidos

API REST para gestão de pedidos da rede de lanchonetes Raízes do Nordeste.

Trabalho acadêmico — Projeto Multidisciplinar Trilha Back-End 2026 (UNINTER).

---

## Requisitos

Antes de começar, você precisa ter instalado na sua máquina:

- Java 25 (JDK)
- Docker (para subir o PostgreSQL)
- Git

O Gradle já vem embutido no projeto pelo wrapper (`gradlew`), não é necessário instalá-lo separadamente.

---

## 1. Clone o repositório

```
git clone https://github.com/rochaalisson/pedidos-api.git
cd pedidos-api
```

---

## 2. Suba o banco de dados com Docker

O comando abaixo cria e inicia um contêiner PostgreSQL já com o banco e o usuário configurados:

```
docker run --name raizes-postgres \
  -e POSTGRES_DB=raizes \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -p 5432:5432 \
  -d postgres:16
```

Para verificar se o contêiner está rodando:

```
docker ps
```

Para parar o banco quando não estiver usando:

```
docker stop raizes-postgres
```

Para iniciar novamente depois de parado:

```
docker start raizes-postgres
```

---

## 3. Configure as variáveis de ambiente

Copie o arquivo de exemplo:

```
cp .env.example .env
```

Abra o `.env` e confira se os valores estão assim (correspondem ao contêiner Docker criado acima):

```
DB_URL=jdbc:postgresql://localhost:5432/raizes
DB_USER=admin
DB_PASSWORD=admin
JWT_SECRET=troque-esta-chave-por-uma-secreta-com-no-minimo-32-bytes
```

Se quiser, altere o `JWT_SECRET` para qualquer string com pelo menos 32 caracteres. Os demais valores só precisam mudar se você tiver alterado o comando Docker do passo anterior.

---

## 4. Instale as dependências

O Gradle baixa todas as dependências automaticamente. Para garantir que tudo está em ordem:

```
./gradlew dependencies
```

Ou simplesmente pule para o próximo passo — as dependências são baixadas junto com o build.

---

## 5. Execute as migrations e inicie a API

As migrations do Flyway rodam automaticamente quando a aplicação sobe, criando todas as tabelas. Basta executar:

```
./gradlew bootRun
```

Na primeira execução um usuário administrador é criado automaticamente com as credenciais:

```
email: admin@raizesdonordeste.com
senha: admin123
```

A API estará disponível em `http://localhost:8080`.

---

## 6. Acesse a documentação (Swagger)

Com a aplicação rodando, abra no navegador:

```
http://localhost:8080/swagger-ui.html
```

Todos os endpoints estão documentados com exemplos de requisição e resposta. Para testar endpoints autenticados diretamente pelo Swagger:

1. Clique em `POST /auth/login`, informe o e-mail e senha do admin e execute.
2. Copie o valor do campo `token` da resposta.
3. Clique no botão "Authorize" no topo da página, cole o token e confirme.
4. A partir daí todas as requisições feitas pelo Swagger já incluem o token automaticamente.

Se preferir importar no Postman ou Insomnia, a especificação OpenAPI está disponível em:

```
http://localhost:8080/v3/api-docs
```

---

## 7. Rode os testes

Os testes automatizados não precisam do banco de dados em execução. Para rodá-los:

```
./gradlew test
```

Depois de executar, o Gradle gera um relatório HTML completo com o resultado de cada teste. Abra no navegador:

```
build/reports/tests/test/index.html
```

---

## Evidências

Repositório do projeto:
https://github.com/rochaalisson/pedidos-api

Documentação Swagger (requer a aplicação rodando localmente):
http://localhost:8080/swagger-ui.html

A rota do Swagger é `/swagger-ui.html`. Não exige autenticação para acessar — o token JWT só é necessário para executar os endpoints protegidos dentro da própria interface, conforme descrito no passo 6 acima.

Coleção Postman com todos os endpoints e exemplos de resposta (incluindo cenários de erro 401, 403, 404 e 422):
https://github.com/rochaalisson/pedidos-api/blob/main/postman/raizes-do-nordeste.postman_collection.json

Para usar a coleção, importe o arquivo no Postman, execute o login com as credenciais do admin, copie o token retornado e defina a variável de ambiente `bearerToken` com esse valor. A variável `baseUrl` já está configurada para `http://localhost:8080`.
