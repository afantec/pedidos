-- Cria banco, usuário e esquema do microsserviço pedidos (seção 7 da análise).
-- Executar uma vez, como administrador do cluster:
--   psql -h localhost -p 5433 -U postgres -d postgres -v app_password='<senha>' -f db/setup-banco.sql
-- Pode ser executado de novo: só cria o que ainda não existe.

\set ON_ERROR_STOP on

-- Banco compartilhado pelos microsserviços (um esquema por serviço)
SELECT 'CREATE DATABASE plataforma ENCODING ''UTF8'''
 WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'plataforma') \gexec

-- Usuário da aplicação; a senha é sempre (re)definida com o valor informado
SELECT 'CREATE ROLE pedidos_app LOGIN'
 WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'pedidos_app') \gexec
ALTER ROLE pedidos_app WITH LOGIN PASSWORD :'app_password';

\connect plataforma

CREATE SCHEMA IF NOT EXISTS pedidos AUTHORIZATION pedidos_app;
REVOKE ALL ON DATABASE plataforma FROM PUBLIC;
GRANT CONNECT ON DATABASE plataforma TO pedidos_app;
REVOKE ALL ON SCHEMA public FROM pedidos_app;
ALTER ROLE pedidos_app IN DATABASE plataforma SET search_path = pedidos;
