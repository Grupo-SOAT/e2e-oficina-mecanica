-- Creates one database and one least-privilege user per service.
CREATE USER mnl WITH PASSWORD 'mnl';
CREATE DATABASE oficina_mnl OWNER mnl;

CREATE USER ms_orcamentos WITH PASSWORD 'ms_orcamentos';
CREATE DATABASE oficina_ms_orcamentos OWNER ms_orcamentos;

CREATE USER ms_pedidos WITH PASSWORD 'ms_pedidos';
CREATE DATABASE oficina_ms_pedidos OWNER ms_pedidos;

GRANT ALL PRIVILEGES ON DATABASE oficina_mnl TO mnl;
GRANT ALL PRIVILEGES ON DATABASE oficina_ms_orcamentos TO ms_orcamentos;
GRANT ALL PRIVILEGES ON DATABASE oficina_ms_pedidos TO ms_pedidos;
