-- Canonical seed applied by the E2E suite.
-- The services own the schema; this script only seeds reference data.
DELETE FROM service_supplies;
DELETE FROM catalog_services;
DELETE FROM supplies;
DELETE FROM vehicles;
DELETE FROM owners;
DELETE FROM users;

INSERT INTO users (user_id, user_name, password, role) VALUES
(1, 'admin', '$2a$10$IETgyptrfhwJ11oTxUZEf.JAW/km7JpgmN8g51oAp4clPB8LOWCPa', ARRAY['ADMIN']),
(2, 'mecanico', '$2a$10$zF9DASX8Vrl.9RAtkZ6kkOyajVXfMtJ1ubNMMHTYM4wYBJhkMs/RK', ARRAY['MECHANIC']),
(3, 'atendente', '$2a$10$CuqGHQMfvzb312mnKrrkPOCBeIeXyr75O6j98Ll.CFuCHwSyAYrC6', ARRAY['ATTENDANT']),
(4, 'almoxarife', '$2a$10$UqbvRK7u7k1rwuPX87WIKOXyK1Wo2bNoYQf9PRdlE//eIt.FFCrei', ARRAY['STOREKEEPER']),
(5, 'chatbot', '$2a$10$eFytJDGtjbThXa80FyOOBuFdK2IwjyWefYkMpiBEFlpBaOkA7I0.6', ARRAY['CHATBOT']);

INSERT INTO owners (owner_id, name, document, document_type, phone, email, created_at, updated_at) VALUES
(1, 'João Silva', '84779441056', 'CPF', '11988887777', 'joao.silva@email.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Maria Robeta', '39544511075', 'CPF', '11977776666', 'maria.robertae@email.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO vehicles (vehicle_id, owner_id, license_plate, brand, model, vehicle_year, color) VALUES
(1, 1, 'ABC-1234', 'Ford', 'Ka', 2020, 'PRATA'),
(2, 2, 'DEF-5678', 'Chevrolet', 'Onix', 2021, 'BRANCO');

INSERT INTO supplies (supply_id, sku, name, description, unit_price, supplied_by, reserved_quantity, available_quantity, created_at, updated_at) VALUES
(1, 'SKU-001', 'Óleo Sintético', 'Óleo sintético 5W30', 49.90, 10, 1, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'SKU-002', 'Filtro de Óleo', 'Filtro compatível motor flex', 24.90, 10, 0, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'SKU-003', 'Pastilha de Freio', 'Jogo dianteiro', 89.90, 11, 2, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO catalog_services (catalog_service_id, name, description, base_price, created_at, updated_at) VALUES
(1, 'Troca de Óleo', 'Serviço de troca de óleo', 120.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Revisão de Freios', 'Serviço de revisão de freios', 250.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

SELECT setval(pg_get_serial_sequence('users', 'user_id'), (SELECT COALESCE(MAX(user_id), 0) FROM users));
SELECT setval(pg_get_serial_sequence('owners', 'owner_id'), (SELECT COALESCE(MAX(owner_id), 0) FROM owners));
SELECT setval(pg_get_serial_sequence('vehicles', 'vehicle_id'), (SELECT COALESCE(MAX(vehicle_id), 0) FROM vehicles));
SELECT setval(pg_get_serial_sequence('supplies', 'supply_id'), (SELECT COALESCE(MAX(supply_id), 0) FROM supplies));
SELECT setval(pg_get_serial_sequence('catalog_services', 'catalog_service_id'), (SELECT COALESCE(MAX(catalog_service_id), 0) FROM catalog_services));
