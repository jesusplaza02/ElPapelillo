CREATE DATABASE elpapelillo_db;
USE elpapelillo_db;

-- 1. CREACIÓN DE TABLAS 
CREATE TABLE Usuario (
    idUsuario INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100),
    telefono VARCHAR(20),
    direccion VARCHAR(255),
    rol VARCHAR(50),
    DNI VARCHAR(20) UNIQUE,
    fechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE Administrador (
    idUsuario INT PRIMARY KEY,
    cargo VARCHAR(100),
    CONSTRAINT fk_admin_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE CASCADE
);

CREATE TABLE Representante (
    idUsuario INT PRIMARY KEY,
    contacto_emergencia VARCHAR(20),
    CONSTRAINT fk_repre_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE CASCADE
);

CREATE TABLE Concurso (
    idConcurso INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    fechaInicio DATE,
    fechaFin DATE,
    fechaInicioInscripcion DATE,
    fechaFinInscripcion DATE,
    tipoConcurso ENUM('CANTO', 'DRAG', 'DIOSES', 'OTRO'),
    estadoConcurso ENUM('ACTIVO', 'HISTORICO')
);

CREATE TABLE Agrupacion (
    idAgrupacion INT AUTO_INCREMENT PRIMARY KEY,
    idRepresentante INT,
    idConcurso INT NOT NULL,
    nombre VARCHAR(100),
    nombreUltimaParticipacion VARCHAR(100),
    anio YEAR,
    categoria ENUM('ADULTO', 'JUVENIL', 'INFANTIL'),
    estadoInscripcion ENUM('APROBADO', 'PENDIENTE', 'RECHAZADO'),
    CONSTRAINT fk_agrupa_repre FOREIGN KEY (idRepresentante) REFERENCES Representante(idUsuario) ON DELETE SET NULL,
    CONSTRAINT fk_agrupa_concurso FOREIGN KEY (idConcurso) REFERENCES Concurso(idConcurso)
);

CREATE TABLE AgrupacionCanto (
    idAgrupacion INT PRIMARY KEY,
    modalidad ENUM('ROMANCERO', 'MURGA', 'COMPARSA', 'CUARTETO', 'CORO'),
    autorMusica VARCHAR(100),
    autorLetra VARCHAR(100),
    direccion VARCHAR(255),
    CONSTRAINT fk_canto_agrupa FOREIGN KEY (idAgrupacion) REFERENCES Agrupacion(idAgrupacion) ON DELETE CASCADE
);

CREATE TABLE AgrupacionDioses (
    idAgrupacion INT PRIMARY KEY,
    modalidad ENUM('DIOS', 'DIOSA'),
    modelo VARCHAR(100),
    diseñador VARCHAR(100),
    CONSTRAINT fk_dioses_agrupa FOREIGN KEY (idAgrupacion) REFERENCES Agrupacion(idAgrupacion) ON DELETE CASCADE
);

CREATE TABLE AgrupacionDrag (
    idAgrupacion INT PRIMARY KEY,
    diseñador VARCHAR(100),
    nombreArtisticoDrag VARCHAR(100),
    CONSTRAINT fk_drag_agrupa FOREIGN KEY (idAgrupacion) REFERENCES Agrupacion(idAgrupacion) ON DELETE CASCADE
);

CREATE TABLE AgrupacionOtros (
    idAgrupacion INT PRIMARY KEY,
    comentariosDestacables TEXT,
    CONSTRAINT fk_otros_agrupa FOREIGN KEY (idAgrupacion) REFERENCES Agrupacion(idAgrupacion) ON DELETE CASCADE
);

CREATE TABLE Participante (
    idParticipante INT AUTO_INCREMENT PRIMARY KEY,
    idAgrupacion INT NOT NULL,
    nombre VARCHAR(100),
    dni VARCHAR(20),
    fechaNacimiento DATE,
    rol ENUM('VOZ', 'GUITARRA', 'CAJA', 'BOMBO', 'MAQUILLADORA', 'AYUDANTE DE ESCENA', 'MONTADOR', 'OTRO'),
    CONSTRAINT fk_parti_agrupa FOREIGN KEY (idAgrupacion) REFERENCES Agrupacion(idAgrupacion)
);

CREATE TABLE Documento (
    idDocumento INT AUTO_INCREMENT PRIMARY KEY,
    idAgrupacion INT NOT NULL,
    nombreArchivo VARCHAR(255),
    rutaArchivo VARCHAR(255),
    estado ENUM('APROBADO', 'PENDIENTE', 'RECHAZADO'),
    fechaSubida DATETIME,
    mensajeRechazo TEXT,
    CONSTRAINT fk_doc_agrupa FOREIGN KEY (idAgrupacion) REFERENCES Agrupacion(idAgrupacion)
);

CREATE TABLE Fianza (
    idFianza INT AUTO_INCREMENT PRIMARY KEY,
    idAgrupacion INT UNIQUE NOT NULL,
    importe DECIMAL(10,2),
    pagada BOOLEAN DEFAULT FALSE,
    rutaRecibo VARCHAR(255),
    fechaPago DATETIME,
    CONSTRAINT fk_fianza_agrupa FOREIGN KEY (idAgrupacion) REFERENCES Agrupacion(idAgrupacion)
);

CREATE TABLE RegistroActividad (
    idLog INT AUTO_INCREMENT PRIMARY KEY,
    idAdmin INT,
    fechaHora DATETIME DEFAULT CURRENT_TIMESTAMP,
    accion VARCHAR(255),
    CONSTRAINT fk_log_admin FOREIGN KEY (idAdmin) REFERENCES Administrador(idUsuario)
);