CREATE DATABASE  IF NOT EXISTS `elpapelillo_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `elpapelillo_db`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: elpapelillo_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `administrador`
--

DROP TABLE IF EXISTS `administrador`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `administrador` (
  `idUsuario` int NOT NULL,
  `id_organizacion` int DEFAULT NULL,
  `cargo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idUsuario`),
  KEY `FKu1fs8vpo78kjhlstavhuvaqu` (`id_organizacion`),
  CONSTRAINT `FKgljc5njq6mdeugmefvn4x6ac5` FOREIGN KEY (`idUsuario`) REFERENCES `usuario` (`idUsuario`),
  CONSTRAINT `FKu1fs8vpo78kjhlstavhuvaqu` FOREIGN KEY (`id_organizacion`) REFERENCES `organizacion` (`idOrganizacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agrupacion`
--

DROP TABLE IF EXISTS `agrupacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agrupacion` (
  `anio` int DEFAULT NULL,
  `idAgrupacion` int NOT NULL AUTO_INCREMENT,
  `id_representante` int DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `nombreUltimaParticipacion` varchar(255) DEFAULT NULL,
  `categoria` enum('ADULTO','INFANTIL','JUVENIL') DEFAULT NULL,
  `tipoConcurso` enum('CANTO','DIOSES','DRAG','OTRO') DEFAULT NULL,
  PRIMARY KEY (`idAgrupacion`),
  KEY `FK4bnik0y21tl2j4uspoa2svbwy` (`id_representante`),
  CONSTRAINT `FK4bnik0y21tl2j4uspoa2svbwy` FOREIGN KEY (`id_representante`) REFERENCES `representante` (`idUsuario`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agrupacioncanto`
--

DROP TABLE IF EXISTS `agrupacioncanto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agrupacioncanto` (
  `idAgrupacion` int NOT NULL,
  `autorLetra` varchar(255) DEFAULT NULL,
  `autorMusica` varchar(255) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `modalidad` enum('COMPARSA','CORO','CUARTETO','MURGA','ROMANCERO') DEFAULT NULL,
  PRIMARY KEY (`idAgrupacion`),
  CONSTRAINT `FKc40pl0lpgn9o4ndgxby8w1f0y` FOREIGN KEY (`idAgrupacion`) REFERENCES `agrupacion` (`idAgrupacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agrupaciondioses`
--

DROP TABLE IF EXISTS `agrupaciondioses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agrupaciondioses` (
  `idAgrupacion` int NOT NULL,
  `modalidad` tinyint DEFAULT NULL,
  `disenador` varchar(255) DEFAULT NULL,
  `modelo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idAgrupacion`),
  CONSTRAINT `FKiq4qp23x3nxoy0xxsytokngv1` FOREIGN KEY (`idAgrupacion`) REFERENCES `agrupacion` (`idAgrupacion`),
  CONSTRAINT `agrupaciondioses_chk_1` CHECK ((`modalidad` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agrupaciondrag`
--

DROP TABLE IF EXISTS `agrupaciondrag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agrupaciondrag` (
  `idAgrupacion` int NOT NULL,
  `disenador` varchar(255) DEFAULT NULL,
  `nombreArtisticoDrag` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idAgrupacion`),
  CONSTRAINT `FK4139l6mbk3sn2gyc2vhn6k4kk` FOREIGN KEY (`idAgrupacion`) REFERENCES `agrupacion` (`idAgrupacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agrupacionotros`
--

DROP TABLE IF EXISTS `agrupacionotros`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agrupacionotros` (
  `idAgrupacion` int NOT NULL,
  `comentariosDestacables` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idAgrupacion`),
  CONSTRAINT `FKoy3shjtamkg7cowh8rmxh7iv4` FOREIGN KEY (`idAgrupacion`) REFERENCES `agrupacion` (`idAgrupacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `concurso`
--

DROP TABLE IF EXISTS `concurso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `concurso` (
  `fechaFin` date DEFAULT NULL,
  `fechaFinInscripcion` date DEFAULT NULL,
  `fechaInicio` date DEFAULT NULL,
  `fechaInicioInscripcion` date DEFAULT NULL,
  `idConcurso` int NOT NULL AUTO_INCREMENT,
  `id_organizacion` int NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `estadoConcurso` enum('ACTIVO','HISTORICO') DEFAULT NULL,
  `tipoConcurso` enum('CANTO','DIOSES','DRAG','OTRO') DEFAULT NULL,
  PRIMARY KEY (`idConcurso`),
  KEY `FKoy7gwuyuxudth2h1q4hpoy9lk` (`id_organizacion`),
  CONSTRAINT `FKoy7gwuyuxudth2h1q4hpoy9lk` FOREIGN KEY (`id_organizacion`) REFERENCES `organizacion` (`idOrganizacion`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `documento`
--

DROP TABLE IF EXISTS `documento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `documento` (
  `idDocumento` int NOT NULL AUTO_INCREMENT,
  `id_inscripcion` int DEFAULT NULL,
  `comentarioRevision` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `tipo` varchar(255) DEFAULT NULL,
  `urlArchivo` varchar(255) DEFAULT NULL,
  `estado` enum('APROBADO','PENDIENTE','RECHAZADO') DEFAULT NULL,
  PRIMARY KEY (`idDocumento`),
  KEY `FKlv6ecrxpav4i2ninl4hotxpvb` (`id_inscripcion`),
  CONSTRAINT `FKlv6ecrxpav4i2ninl4hotxpvb` FOREIGN KEY (`id_inscripcion`) REFERENCES `inscripciones` (`idInscripcion`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fianza`
--

DROP TABLE IF EXISTS `fianza`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fianza` (
  `idFianza` int NOT NULL AUTO_INCREMENT,
  `importe` double DEFAULT NULL,
  `pagada` bit(1) DEFAULT NULL,
  `fechaPago` datetime(6) DEFAULT NULL,
  `rutaRecibo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idFianza`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inscripcion_participantes`
--

DROP TABLE IF EXISTS `inscripcion_participantes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inscripcion_participantes` (
  `idParticipacion` int NOT NULL AUTO_INCREMENT,
  `id_inscripcion` int DEFAULT NULL,
  `id_participante` bigint DEFAULT NULL,
  `rol` enum('AYUDANTE_DE_ESCENA','BOMBO','CAJA','GUITARRA','MAQUILLADORA','MONTADOR','OTRO','VOZ') DEFAULT NULL,
  PRIMARY KEY (`idParticipacion`),
  KEY `FKptf9cwfbtiomq6arulr1shb10` (`id_inscripcion`),
  KEY `FK1y7vy5wgorwab9yyn42m6wbfi` (`id_participante`),
  CONSTRAINT `FK1y7vy5wgorwab9yyn42m6wbfi` FOREIGN KEY (`id_participante`) REFERENCES `participante` (`id`),
  CONSTRAINT `FKptf9cwfbtiomq6arulr1shb10` FOREIGN KEY (`id_inscripcion`) REFERENCES `inscripciones` (`idInscripcion`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inscripciones`
--

DROP TABLE IF EXISTS `inscripciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inscripciones` (
  `idInscripcion` int NOT NULL AUTO_INCREMENT,
  `id_agrupacion` int DEFAULT NULL,
  `id_concurso` int DEFAULT NULL,
  `id_fianza` int DEFAULT NULL,
  `fechaInscripcion` datetime(6) NOT NULL,
  `estadoInscripcion` enum('APROBADO','PENDIENTE','RECHAZADO') DEFAULT NULL,
  PRIMARY KEY (`idInscripcion`),
  UNIQUE KEY `UK7ffpm1f62ufydow9i8uquluor` (`id_fianza`),
  KEY `FKhvlbosxq68379npks52p7tnv9` (`id_agrupacion`),
  KEY `FKliw09g2e10mwnmdmayawwtknq` (`id_concurso`),
  CONSTRAINT `FKhvlbosxq68379npks52p7tnv9` FOREIGN KEY (`id_agrupacion`) REFERENCES `agrupacion` (`idAgrupacion`),
  CONSTRAINT `FKliw09g2e10mwnmdmayawwtknq` FOREIGN KEY (`id_concurso`) REFERENCES `concurso` (`idConcurso`),
  CONSTRAINT `FKmmcl8udu3ph7hims4dutja9j` FOREIGN KEY (`id_fianza`) REFERENCES `fianza` (`idFianza`)
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `logauditoria`
--

DROP TABLE IF EXISTS `logauditoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logauditoria` (
  `administrador_id` int DEFAULT NULL,
  `fecha` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `accion` varchar(255) DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK44uns0jvl1kcnjwsh5yk29ey6` (`administrador_id`),
  CONSTRAINT `FK44uns0jvl1kcnjwsh5yk29ey6` FOREIGN KEY (`administrador_id`) REFERENCES `administrador` (`idUsuario`)
) ENGINE=InnoDB AUTO_INCREMENT=360 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `organizacion`
--

DROP TABLE IF EXISTS `organizacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `organizacion` (
  `idOrganizacion` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `ubicacion` varchar(255) NOT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `telefono` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`idOrganizacion`),
  UNIQUE KEY `UKp63r6jif72np9f1a9elbys22l` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `participante`
--

DROP TABLE IF EXISTS `participante`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `participante` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dni` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `fecha_nacimiento` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `representante`
--

DROP TABLE IF EXISTS `representante`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `representante` (
  `idUsuario` int NOT NULL,
  `contacto_emergencia` varchar(255) NOT NULL,
  PRIMARY KEY (`idUsuario`),
  CONSTRAINT `FKqpid4rwdgsvgea4u1ub0099xg` FOREIGN KEY (`idUsuario`) REFERENCES `usuario` (`idUsuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fechaRegistro` date DEFAULT NULL,
  `idUsuario` int NOT NULL AUTO_INCREMENT,
  `DNI` varchar(255) NOT NULL,
  `direccion` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `rol` varchar(255) NOT NULL,
  `telefono` varchar(255) NOT NULL,
  PRIMARY KEY (`idUsuario`),
  UNIQUE KEY `UK5171l57faosmj8myawaucatdw` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-10 12:01:38
