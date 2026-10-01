-- Dados de exemplo: só carregados no perfil local (config/application-local.yml)

INSERT INTO cliente (nome, documento, email) VALUES
    ('Mercado Boa Vista Ltda', '11222333000181', 'compras@boavista.example'),
    ('Ana Souza',              '52998224725',    'ana.souza@example.com');

INSERT INTO produto (codigo, descricao, preco) VALUES
    ('CAF-500', 'Café torrado 500 g',     18.90),
    ('ACU-1K',  'Açúcar cristal 1 kg',     5.49),
    ('LEI-1L',  'Leite integral 1 L',      4.99),
    ('ARZ-5K',  'Arroz tipo 1 5 kg',      27.50);
