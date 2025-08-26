-- datos_ejemplo.sql
USE incidencia_cr;

-- Usuarios de ejemplo (1 admin y 2 usuarios)
INSERT INTO users (username, rol) VALUES ('admin_prueba', 'ADMIN');
INSERT INTO users (username, rol) VALUES ('usuario_juan', 'USER');
INSERT INTO users (username, rol) VALUES ('usuario_maria', 'USER');

-- Paradas de ejemplo en Costa Rica (nombres verosímiles)
INSERT INTO stops (nombre, canton, lat, lon) VALUES
('Parada Mercado Central - San José', 'San José', 9.934739, -84.078207),
('Parada Universidad Nacional - Heredia', 'Heredia', 10.002152, -84.115231),
('Parada Estación Alajuela Centro', 'Alajuela', 10.016241, -84.217109),
('Parada Parque Central - Cartago', 'Cartago', 9.864227, -83.919502),
('Parada Puerto Limón - Centro', 'Limón', 9.990000, -83.033333);

-- Un par de incidencias de ejemplo
INSERT INTO incidents (user_id, stop_id, type, description) VALUES
(2, 1, 'RETRASO', 'Bus con más de 30 min de retraso por tránsito.'),
(3, 5, 'SEGURIDAD', 'Persona sospechosa en la parada, solicitar vigilancia.');
