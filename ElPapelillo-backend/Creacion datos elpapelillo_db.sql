USE elpapelillo_db;
-- 1. COAC Málaga 2026 (El que ya tenías)
INSERT INTO concurso (
     nombre, 
     fechaInicio, 
     fechaFin, 
     fechaInicioInscripcion, 
     fechaFinInscripcion, 
     tipoConcurso, 
     estadoConcurso
) VALUES (
     'COAC Málaga 2026', 
     '2026-01-23', 
     '2026-02-06', 
     '2025-11-10', 
     '2025-11-30', 
     'CANTO', 
     'HISTORICO'
);

-- 2. Gala Drag Málaga 2025
INSERT INTO concurso (
     nombre, 
     fechaInicio, 
     fechaFin, 
     fechaInicioInscripcion, 
     fechaFinInscripcion, 
     tipoConcurso, 
     estadoConcurso
) VALUES (
     'Gala Drag Málaga 2025', 
     '2025-02-14', 
     '2025-02-14', 
     '2024-12-01', 
     '2025-01-15', 
     'DRAG', 
     'HISTORICO'
);

-- 3. Elección de Dioses Málaga 2025
INSERT INTO concurso (
     nombre, 
     fechaInicio, 
     fechaFin, 
     fechaInicioInscripcion, 
     fechaFinInscripcion, 
     tipoConcurso, 
     estadoConcurso
) VALUES (
     'Elección de Dioses 2025', 
     '2025-02-08', 
     '2025-02-08', 
     '2024-11-15', 
     '2025-01-10', 
     'DIOSES', 
     'HISTORICO'
);

-- Representante 1
INSERT INTO usuario (email, password, nombre, telefono, direccion, rol, DNI) 
VALUES ('paco.repre@email.com', 'hash_password_1', 'Paco García', '600111222', 'Calle Larios 1, Málaga', 'Representante', '12345678A');
INSERT INTO representante (idUsuario, contacto_emergencia) 
VALUES (LAST_INSERT_ID(), '600999888');

-- Representante 2
INSERT INTO usuario (email, password, nombre, telefono, direccion, rol, DNI) 
VALUES ('maria.murga@email.com', 'hash_password_2', 'María López', '611222333', 'Av. Velázquez 10, Málaga', 'Representante', '87654321B');
INSERT INTO representante (idUsuario, contacto_emergencia) 
VALUES (LAST_INSERT_ID(), '611000111');

-- Representante 3
INSERT INTO usuario (email, password, nombre, telefono, direccion, rol, DNI) 
VALUES ('juan.comparsa@email.com', 'hash_password_3', 'Juan Moreno', '622333444', 'Calle Victoria 5, Málaga', 'Representante', '45678912C');
INSERT INTO representante (idUsuario, contacto_emergencia) 
VALUES (LAST_INSERT_ID(), '622111222');

-- Administrador 1
INSERT INTO usuario (email, password, nombre, telefono, direccion, rol, DNI) 
VALUES ('admin.oficial@carnaval.com', 'admin_hash_1', 'Carlos Ruiz', '633444555', 'Sede Fundación Carnaval', 'Administrador', '11122233D');
INSERT INTO administrador (idUsuario, cargo) 
VALUES (LAST_INSERT_ID(), 'Vocal');

-- Administrador 2
INSERT INTO usuario (email, password, nombre, telefono, direccion, rol, DNI) 
VALUES ('soporte.tecnico@carnaval.com', 'admin_hash_2', 'Laura Sanz', '644555666', 'Sede Fundación Carnaval', 'Administrador', '44455566E');
INSERT INTO administrador (idUsuario, cargo) 
VALUES (LAST_INSERT_ID(), 'Presidente de la fundación');

-- Agrupación: El poeta majareta
INSERT INTO agrupacion (idRepresentante, idConcurso, nombre, nombreUltimaParticipacion, anio, categoria, estadoInscripcion)
VALUES (1, 1, 'El poeta majareta de La Malagueta', 'Al carajo el marqués', 2026, 'INFANTIL', 'APROBADO');

INSERT INTO agrupacioncanto (idAgrupacion, modalidad, autorMusica, autorLetra, direccion)
VALUES (LAST_INSERT_ID(), 'ROMANCERO', 'Antonio Jesús Díaz Plaza', 'Antonio Jesús Díaz Plaza', 'Los Corazones');

-- Agrupación: Mira si tenemos fe, que lo intentamos otra vez.
INSERT INTO agrupacion (idRepresentante, idConcurso, nombre, nombreUltimaParticipacion, anio, categoria, estadoInscripcion)
VALUES (1, 1, 'Mira si tenemos fe, que lo intentamos otra vez', 'Quiero que mi padre se vaya pero mi padre no quiere', 2026, 'ADULTO', 'APROBADO');

INSERT INTO agrupacioncanto (idAgrupacion, modalidad, autorMusica, autorLetra, direccion)
VALUES (LAST_INSERT_ID(), 'MURGA', 'José León Miranda', 'Francisco León Miranda', 'Huelin');

-- Agrupación: Pink Roche
INSERT INTO agrupacion (idRepresentante, idConcurso, nombre, nombreUltimaParticipacion, anio, categoria, estadoInscripcion)
VALUES (2, 2, 'Fantasía de purpurina', NULL, 2025, 'ADULTO', 'APROBADO');

-- Especificamos los datos del Drag
INSERT INTO agrupacionDrag (idAgrupacion, diseñador, nombreArtisticoDrag)
VALUES (LAST_INSERT_ID(), 'Pedro Fech', 'Pink Roche');