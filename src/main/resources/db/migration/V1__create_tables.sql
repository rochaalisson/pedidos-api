CREATE TYPE perfil_usuario AS ENUM ('ADMIN', 'GERENTE', 'ATENDENTE');

CREATE TYPE canal_pedido AS ENUM ('APP', 'TOTEM', 'BALCAO', 'PICKUP', 'WEB');

CREATE TYPE status_pedido AS ENUM ('AGUARDANDO_PAGAMENTO', 'PREPARANDO', 'PRONTO', 'ENTREGUE', 'CANCELADO', 'PAGAMENTO_RECUSADO');

CREATE TABLE unidade (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    endereco TEXT NOT NULL,
    data_desativacao TIMESTAMPTZ
);

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    unidade_id BIGINT REFERENCES unidade(id),
    nome VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha TEXT NOT NULL,
    perfil perfil_usuario NOT NULL
);

CREATE TABLE cliente (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE,
    senha TEXT,
    cpf VARCHAR(11) UNIQUE,
    telefone VARCHAR(15),
    aceita_programa_fidelidade BOOLEAN NOT NULL DEFAULT FALSE,
    pontos_fidelidade INT NOT NULL DEFAULT 0
);

CREATE TABLE categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    ordem_exibicao INT NOT NULL DEFAULT 0
);

CREATE TABLE produto (
    id BIGSERIAL PRIMARY KEY,
    categoria_id BIGINT NOT NULL REFERENCES categoria(id),
    nome VARCHAR(50) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10, 2) NOT NULL
);

CREATE TABLE pedido (
    id BIGSERIAL PRIMARY KEY,
    unidade_id BIGINT NOT NULL REFERENCES unidade(id),
    usuario_id BIGINT REFERENCES usuario(id),
    cliente_id BIGINT REFERENCES cliente(id),
    canal canal_pedido NOT NULL,
    status status_pedido NOT NULL,
    valor_total DECIMAL(10, 2) NOT NULL,
    data_criacao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE produto_pedido (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedido(id),
    produto_id BIGINT NOT NULL REFERENCES produto(id),
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10, 2) NOT NULL
);

CREATE TABLE produto_unidade (
    id BIGSERIAL PRIMARY KEY,
    unidade_id BIGINT NOT NULL REFERENCES unidade(id),
    produto_id BIGINT NOT NULL REFERENCES produto(id),
    quantidade_estoque INT NOT NULL,
    disponivel_cardapio BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(unidade_id, produto_id)
);

CREATE TABLE movimentacao_estoque (
    id BIGSERIAL PRIMARY KEY,
    produto_unidade_id BIGINT NOT NULL REFERENCES produto_unidade(id),
    quantidade_alterada INT NOT NULL,
    motivo VARCHAR(50) NOT NULL,
    data_movimentacao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
