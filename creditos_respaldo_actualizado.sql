/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.20-11.8.9-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: creditos_db
-- ------------------------------------------------------
-- Server version	11.8.9-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `alerta`
--

DROP TABLE IF EXISTS `alerta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `alerta` (
  `id_alerta` int(11) NOT NULL AUTO_INCREMENT,
  `id_cuota` int(11) NOT NULL,
  `tipo` enum('PREVENTIVA','MORA') NOT NULL,
  `fecha_generacion` date NOT NULL,
  `dias_retraso` int(11) NOT NULL DEFAULT 0,
  `estado` enum('GENERADA','ATENDIDA') NOT NULL DEFAULT 'GENERADA',
  PRIMARY KEY (`id_alerta`),
  UNIQUE KEY `uq_alerta_dia` (`id_cuota`,`tipo`,`fecha_generacion`),
  KEY `idx_alerta_tipo_estado` (`tipo`,`estado`),
  CONSTRAINT `alerta_ibfk_1` FOREIGN KEY (`id_cuota`) REFERENCES `cuota` (`id_cuota`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alerta`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `alerta` WRITE;
/*!40000 ALTER TABLE `alerta` DISABLE KEYS */;
/*!40000 ALTER TABLE `alerta` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `anulacion_pago`
--

DROP TABLE IF EXISTS `anulacion_pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `anulacion_pago` (
  `id_anulacion` int(11) NOT NULL AUTO_INCREMENT,
  `id_pago` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT current_timestamp(),
  `motivo` varchar(300) NOT NULL,
  PRIMARY KEY (`id_anulacion`),
  UNIQUE KEY `id_pago` (`id_pago`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `anulacion_pago_ibfk_1` FOREIGN KEY (`id_pago`) REFERENCES `pago` (`id_pago`),
  CONSTRAINT `anulacion_pago_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `anulacion_pago`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `anulacion_pago` WRITE;
/*!40000 ALTER TABLE `anulacion_pago` DISABLE KEYS */;
/*!40000 ALTER TABLE `anulacion_pago` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `aplicacion_pago`
--

DROP TABLE IF EXISTS `aplicacion_pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `aplicacion_pago` (
  `id_aplicacion` int(11) NOT NULL AUTO_INCREMENT,
  `id_pago` int(11) NOT NULL,
  `id_cuota` int(11) NOT NULL,
  `monto_interes` decimal(14,2) NOT NULL DEFAULT 0.00,
  `monto_capital` decimal(14,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (`id_aplicacion`),
  KEY `id_pago` (`id_pago`),
  KEY `id_cuota` (`id_cuota`),
  CONSTRAINT `aplicacion_pago_ibfk_1` FOREIGN KEY (`id_pago`) REFERENCES `pago` (`id_pago`),
  CONSTRAINT `aplicacion_pago_ibfk_2` FOREIGN KEY (`id_cuota`) REFERENCES `cuota` (`id_cuota`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `aplicacion_pago`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `aplicacion_pago` WRITE;
/*!40000 ALTER TABLE `aplicacion_pago` DISABLE KEYS */;
INSERT INTO `aplicacion_pago` VALUES
(1,1,13,541.67,1041.66),
(2,2,14,530.38,1041.66);
/*!40000 ALTER TABLE `aplicacion_pago` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `auditoria`
--

DROP TABLE IF EXISTS `auditoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `auditoria` (
  `id_auditoria` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_usuario` int(11) DEFAULT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT current_timestamp(),
  `operacion` varchar(60) NOT NULL,
  `entidad` varchar(60) DEFAULT NULL,
  `id_entidad` bigint(20) DEFAULT NULL,
  `datos_anteriores` text DEFAULT NULL,
  `datos_nuevos` text DEFAULT NULL,
  `resultado` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id_auditoria`),
  KEY `id_usuario` (`id_usuario`),
  KEY `idx_auditoria_fecha` (`fecha_hora`),
  KEY `idx_auditoria_entidad` (`entidad`,`id_entidad`),
  CONSTRAINT `auditoria_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auditoria`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `auditoria` WRITE;
/*!40000 ALTER TABLE `auditoria` DISABLE KEYS */;
INSERT INTO `auditoria` VALUES
(1,6,'2026-10-19 21:42:31','DEVOLVER_A_ANALISIS','solicitud_credito',21,NULL,'evaluacion favorable','OK'),
(2,27,'2026-10-19 22:20:31','REGISTRAR_SOLICITUD','solicitud_credito',22,NULL,'Solicitud del cliente 14182191','OK'),
(3,2,'2026-10-19 22:24:50','ENVIAR_A_EVALUACION','solicitud_credito',22,NULL,'Producto 3; monto 250000.00; plazo 48 meses','OK'),
(4,7,'2026-10-19 22:29:21','ACTUALIZAR_GARANTIA','garantia',11,'Valor: 0.00; verificacion: PENDIENTE','Solicitud: 22; tipo: 1; titular: elvis erik; valor: 700000; verificacion: VALIDADA; evidencia: revison del respaldo adjunto; archivo: garantias/be096dcf-d0a7-43d1-a79e-9a858c141529_solicutuAmbulancia.docx','CORRECTO'),
(5,6,'2026-10-20 00:09:35','DECIDIR_SOLICITUD','solicitud_credito',22,NULL,'EN_EVALUACION -> RECHAZADA; motivo motivo de exceso','OK'),
(6,6,'2026-10-20 00:10:03','DECIDIR_SOLICITUD','solicitud_credito',18,NULL,'EN_EVALUACION -> APROBADA; motivo aprobado','OK'),
(7,7,'2026-10-20 00:13:54','REGISTRAR_DOCUMENTO','documento_solicitud',1,NULL,'Tipo: Carnet; archivo: solicitudes/8cc5dde8-8759-4f72-8dc0-8e876f51ad05_Patrones_Psicologicos__2_.docx','CORRECTO'),
(8,7,'2026-10-20 00:14:05','VERIFICAR_DOCUMENTO','documento_solicitud',1,'PENDIENTE','VERIFICADO','CORRECTO'),
(9,7,'2026-10-20 00:14:09','VALIDAR_DATOS_CLIENTE','solicitud_credito',18,NULL,'CI: 12345432; nombre: ramses segundo; trabajo: En mi casa; ingresos: 5000.00','CORRECTO'),
(10,7,'2026-10-20 00:14:12','VALIDAR_DATOS_CLIENTE','solicitud_credito',18,NULL,'CI: 12345432; nombre: ramses segundo; trabajo: En mi casa; ingresos: 5000.00','CORRECTO'),
(11,7,'2026-10-20 00:21:52','ACTUALIZAR_GARANTIA','garantia',8,'Valor: 0.00; verificacion: PENDIENTE','Solicitud: 19; tipo: 1; titular: ramses segundo; valor: 500000; verificacion: VALIDADA; evidencia: respaldo adjunto; archivo: garantias/91d9419e-8144-4218-8e58-649b4e2b0895_ROLTURNOSMARZOSANTIAGO.docx','CORRECTO'),
(12,7,'2026-10-20 00:23:34','ACTUALIZAR_GARANTIA','garantia',7,'Valor: 0.00; verificacion: PENDIENTE','Solicitud: 18; tipo: 1; titular: ramses segundo; valor: 500000; verificacion: VALIDADA; evidencia: respaldo adjunto; archivo: garantias/1138fe02-f331-4704-972e-4ae614c6db91_adenda_de_Contrato.docx','CORRECTO'),
(13,7,'2026-10-20 00:23:55','PREPARAR_CREDITO','credito',2,NULL,'Solicitud 18; 50000.00 Bs; tasa nominal anual 13.000%; 48 meses; GRADUAL; contrato CT-e9a62a43718649749cdf7763f0','OK'),
(14,7,'2026-10-20 00:25:00','FORMALIZAR_CONTRATO','contrato',1,NULL,'Firma 2026-10-20; respaldo contratos/821e22c1-b7dd-4cc0-887b-f6f55e1e0255_CartaEntel.docx','OK'),
(15,7,'2026-10-20 00:26:23','SOLICITAR_DESEMBOLSO','desembolso',2,NULL,'Credito 2; prevista 2026-10-20; destino 123412341234','OK'),
(16,7,'2026-10-20 00:27:14','CONFIRMAR_DESEMBOLSO','desembolso',2,NULL,'Referencia Core-test-9901; cronograma 2; cuotas 48; fecha real 2026-10-20','OK'),
(17,3,'2026-10-20 00:30:31','AVISO_CLIENTE','notificacion',1,NULL,'CONFIRMACION_PAGO: Pago recibido: Bs 1583.33 para la cuota 1. Comprobante: P-61043c7d88fc47da99254f16c9. Saldo restante de esa cuota: Bs 0.00.','CORRECTO'),
(18,3,'2026-10-20 00:30:53','AVISO_CLIENTE','notificacion',2,NULL,'RECORDATORIO_CUOTA: Credito CR-a9dcfa41b5d8480d9432d33a4e, cuota 2: saldo pendiente Bs 1572.04. Vencimiento: 2026-12-20. Revise sus cuotas y consulte en caja para realizar el pago.','CORRECTO'),
(19,3,'2026-10-20 00:40:01','AVISO_CLIENTE','notificacion',3,NULL,'CONFIRMACION_PAGO: Pago recibido: Bs 1572.04 para la cuota 2. Comprobante: P-665cc4ddaec84ec49787c98c87. Saldo restante de esa cuota: Bs 0.00.','CORRECTO');
/*!40000 ALTER TABLE `auditoria` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_uca1400_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_auditoria_no_update` BEFORE UPDATE ON `auditoria` FOR EACH ROW BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La auditoria no se puede modificar (RN59)';
END 
*/;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_uca1400_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_auditoria_no_delete` BEFORE DELETE ON `auditoria` FOR EACH ROW BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La auditoria no se puede eliminar (RN59)';
END 
*/;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `id_cliente` int(11) NOT NULL AUTO_INCREMENT,
  `ci` varchar(20) NOT NULL,
  `nombres` varchar(80) NOT NULL,
  `apellidos` varchar(80) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `lugar_trabajo` varchar(100) DEFAULT NULL,
  `ingresos_mensuales` decimal(14,2) NOT NULL DEFAULT 0.00,
  `dia_pago_preferido` tinyint(4) DEFAULT NULL,
  `fecha_registro` date NOT NULL,
  `id_usuario_registro` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_cliente`),
  UNIQUE KEY `ci` (`ci`),
  KEY `id_usuario_registro` (`id_usuario_registro`),
  KEY `idx_cliente_nombre` (`apellidos`,`nombres`),
  CONSTRAINT `cliente_ibfk_1` FOREIGN KEY (`id_usuario_registro`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` VALUES
(1,'9000001','Maria','Quispe','70123456',NULL,'Zona Sur','vendedor',6000.00,NULL,'2026-10-16',7),
(2,'9000002','Jose','Mamani','71234567',NULL,'El Alto','se rasca',1000.00,NULL,'2026-10-16',7),
(3,'9000003','Lucia','Flores','72345678',NULL,'Sopocachi','Comerciante',5000.00,NULL,'2026-10-16',7),
(4,'123456','Andrea','Herrera Fernandez','77715532',NULL,NULL,'desarrolladora',2500.00,NULL,'2026-10-17',NULL),
(5,'54321','Juanito','Perez','77227722',NULL,NULL,'CNS',6000.00,NULL,'2026-10-17',NULL),
(6,'7777777','Juanito','Perez Mamani','8889898',NULL,NULL,'empresa pil',7000.00,NULL,'2026-10-17',NULL),
(7,'12345678','Maria','Guebara Perdidas','78787878',NULL,NULL,'Axes',3000.00,NULL,'2026-10-17',NULL),
(8,'87654321','Pablo','Pancho','0987654',NULL,NULL,'Empresa Pil',7000.00,NULL,'2026-10-18',NULL),
(11,'1234560','Juanita','Perez','12345678',NULL,NULL,'delicia',7890.00,NULL,'2026-10-18',NULL),
(12,'4521367','Carlos','Mamani Quispe','71234567',NULL,NULL,'Empresa Pil',4500.00,NULL,'2026-09-08',NULL),
(13,'5638291','Lucia','Flores Rojas','72345678',NULL,NULL,'Banco Union',7800.00,NULL,'2026-09-13',NULL),
(14,'6712345','Roberto','Choque Condori','73456789',NULL,NULL,'Taller Mecanico',3200.00,NULL,'2026-09-18',NULL),
(15,'7845123','Ana','Vargas Salinas','74567890',NULL,NULL,'Colegio San Andres',5200.00,NULL,'2026-09-23',NULL),
(16,'8956234','Miguel','Torrez Lima','75678901',NULL,NULL,'Ferreteria Central',6100.00,NULL,'2026-09-28',NULL),
(17,'9067345','Rosa','Apaza Huanca','76789012',NULL,NULL,'Comercio propio',2800.00,NULL,'2026-10-03',NULL),
(18,'808080','Pancho','Villa','80808080',NULL,NULL,'Restaurante DOña pancha',2500.00,NULL,'2026-10-18',NULL),
(19,'111111111','EJemplo','uno','12423432',NULL,NULL,'vendedor',2000.00,NULL,'2026-10-18',NULL),
(21,'12345670','elvis','tec','76707322',NULL,NULL,'empresa pil',10000.00,NULL,'2026-10-19',NULL),
(22,'12345432','ramses','segundo','78093232',NULL,NULL,'En mi casa',5000.00,NULL,'2026-10-19',NULL),
(23,'11223344','JOSEMA','NANDO','76784323',NULL,NULL,'trabajo remoto',4000.00,NULL,'2026-10-19',NULL),
(24,'14182190','ellis','condori','76843432',NULL,NULL,'comerciante',50000.00,NULL,'2026-10-19',NULL),
(25,'14182191','elvis','erik','76707322',NULL,NULL,'empresa de coca cola',10000.00,NULL,'2026-10-19',NULL);
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `consulta_riesgo`
--

DROP TABLE IF EXISTS `consulta_riesgo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `consulta_riesgo` (
  `id_consulta` int(11) NOT NULL AUTO_INCREMENT,
  `id_evaluacion` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `referencia_consulta` varchar(80) NOT NULL,
  `fecha_consulta` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_vigencia` date DEFAULT NULL,
  `datos_respuesta` text DEFAULT NULL,
  `estado` enum('EXITOSA','FALLIDA','INVALIDA') NOT NULL,
  PRIMARY KEY (`id_consulta`),
  UNIQUE KEY `referencia_consulta` (`referencia_consulta`),
  KEY `id_evaluacion` (`id_evaluacion`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `consulta_riesgo_ibfk_1` FOREIGN KEY (`id_evaluacion`) REFERENCES `evaluacion_credito` (`id_evaluacion`),
  CONSTRAINT `consulta_riesgo_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consulta_riesgo`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `consulta_riesgo` WRITE;
/*!40000 ALTER TABLE `consulta_riesgo` DISABLE KEYS */;
INSERT INTO `consulta_riesgo` VALUES
(1,1,2,'REF-TEST-A-001','2026-10-16 19:05:21','2027-04-16','Sin antecedentes negativos (simulado)','EXITOSA'),
(2,3,2,'REF-TEST-C-001','2026-10-16 19:05:21',NULL,'Servicio no disponible (simulado)','FALLIDA');
/*!40000 ALTER TABLE `consulta_riesgo` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `contrato`
--

DROP TABLE IF EXISTS `contrato`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `contrato` (
  `id_contrato` int(11) NOT NULL AUTO_INCREMENT,
  `id_credito` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `numero_contrato` varchar(30) NOT NULL,
  `version` int(11) NOT NULL DEFAULT 1,
  `version_plantilla` varchar(20) DEFAULT NULL,
  `fecha_generacion` datetime NOT NULL DEFAULT current_timestamp(),
  `referencia_documental` varchar(200) DEFAULT NULL,
  `fecha_firma` datetime DEFAULT NULL,
  `estado` enum('GENERADO','FORMALIZADO','REEMPLAZADO') NOT NULL DEFAULT 'GENERADO',
  PRIMARY KEY (`id_contrato`),
  UNIQUE KEY `numero_contrato` (`numero_contrato`),
  UNIQUE KEY `uq_contrato_version` (`id_credito`,`version`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `contrato_ibfk_1` FOREIGN KEY (`id_credito`) REFERENCES `credito` (`id_credito`),
  CONSTRAINT `contrato_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contrato`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `contrato` WRITE;
/*!40000 ALTER TABLE `contrato` DISABLE KEYS */;
INSERT INTO `contrato` VALUES
(1,2,7,'CT-e9a62a43718649749cdf7763f0',1,'MENSUAL-NOM-V1','2026-10-20 00:23:55','contratos/821e22c1-b7dd-4cc0-887b-f6f55e1e0255_CartaEntel.docx','2026-10-20 00:00:00','FORMALIZADO');
/*!40000 ALTER TABLE `contrato` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `credito`
--

DROP TABLE IF EXISTS `credito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `credito` (
  `id_credito` int(11) NOT NULL AUTO_INCREMENT,
  `id_solicitud` int(11) NOT NULL,
  `id_politica` int(11) NOT NULL,
  `numero_credito` varchar(30) NOT NULL,
  `monto_aprobado` decimal(14,2) NOT NULL,
  `tasa_interes` decimal(6,3) NOT NULL,
  `plazo_meses` int(11) NOT NULL,
  `tipo_cuota` enum('FIJA','GRADUAL') NOT NULL,
  `fecha_aprobacion` datetime NOT NULL,
  `saldo_capital` decimal(14,2) NOT NULL,
  `estado` enum('APROBADO','VIGENTE','EN_MORA','CANCELADO') NOT NULL DEFAULT 'APROBADO',
  PRIMARY KEY (`id_credito`),
  UNIQUE KEY `id_solicitud` (`id_solicitud`),
  UNIQUE KEY `numero_credito` (`numero_credito`),
  KEY `id_politica` (`id_politica`),
  KEY `idx_credito_estado` (`estado`),
  CONSTRAINT `credito_ibfk_1` FOREIGN KEY (`id_solicitud`) REFERENCES `solicitud_credito` (`id_solicitud`),
  CONSTRAINT `credito_ibfk_2` FOREIGN KEY (`id_politica`) REFERENCES `politica_crediticia` (`id_politica`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credito`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `credito` WRITE;
/*!40000 ALTER TABLE `credito` DISABLE KEYS */;
INSERT INTO `credito` VALUES
(1,1,1,'CR-TEST-0001',10000.00,18.000,12,'FIJA','2026-06-11 00:00:00',8454.90,'EN_MORA'),
(2,18,1,'CR-a9dcfa41b5d8480d9432d33a4e',50000.00,13.000,48,'GRADUAL','2026-10-20 00:10:03',47916.68,'VIGENTE');
/*!40000 ALTER TABLE `credito` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `cronograma_pago`
--

DROP TABLE IF EXISTS `cronograma_pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cronograma_pago` (
  `id_cronograma` int(11) NOT NULL AUTO_INCREMENT,
  `id_credito` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `version` int(11) NOT NULL DEFAULT 1,
  `motivo` varchar(200) DEFAULT NULL,
  `fecha_generacion` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_desembolso_prevista` date NOT NULL,
  `vigente` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_cronograma`),
  UNIQUE KEY `uq_cronograma_version` (`id_credito`,`version`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `cronograma_pago_ibfk_1` FOREIGN KEY (`id_credito`) REFERENCES `credito` (`id_credito`),
  CONSTRAINT `cronograma_pago_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cronograma_pago`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `cronograma_pago` WRITE;
/*!40000 ALTER TABLE `cronograma_pago` DISABLE KEYS */;
INSERT INTO `cronograma_pago` VALUES
(1,1,6,1,'Cronograma original','2026-10-16 19:05:21','2026-06-11',1),
(2,2,7,1,'Plan mensual nominal: anual/12; sin cargos adicionales','2026-10-20 00:27:14','2026-10-20',1);
/*!40000 ALTER TABLE `cronograma_pago` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `cuota`
--

DROP TABLE IF EXISTS `cuota`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cuota` (
  `id_cuota` int(11) NOT NULL AUTO_INCREMENT,
  `id_cronograma` int(11) NOT NULL,
  `numero` int(11) NOT NULL,
  `fecha_vencimiento` date NOT NULL,
  `capital` decimal(14,2) NOT NULL,
  `interes` decimal(14,2) NOT NULL,
  `monto` decimal(14,2) NOT NULL,
  `capital_pagado` decimal(14,2) NOT NULL DEFAULT 0.00,
  `interes_pagado` decimal(14,2) NOT NULL DEFAULT 0.00,
  `saldo` decimal(14,2) NOT NULL,
  `dias_atraso` int(11) NOT NULL DEFAULT 0,
  `estado` enum('PENDIENTE','POR_VENCER','VENCIDA','PAGADA') NOT NULL DEFAULT 'PENDIENTE',
  PRIMARY KEY (`id_cuota`),
  UNIQUE KEY `uq_cuota_numero` (`id_cronograma`,`numero`),
  KEY `idx_cuota_estado_venc` (`estado`,`fecha_vencimiento`),
  CONSTRAINT `cuota_ibfk_1` FOREIGN KEY (`id_cronograma`) REFERENCES `cronograma_pago` (`id_cronograma`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cuota`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `cuota` WRITE;
/*!40000 ALTER TABLE `cuota` DISABLE KEYS */;
INSERT INTO `cuota` VALUES
(1,1,1,'2026-07-11',766.80,150.00,916.80,766.80,150.00,0.00,0,'PAGADA'),
(2,1,2,'2026-08-11',778.30,138.50,916.80,778.30,138.50,0.00,0,'PAGADA'),
(3,1,3,'2026-09-11',789.98,126.82,916.80,0.00,0.00,916.80,35,'VENCIDA'),
(4,1,4,'2026-10-11',801.83,114.97,916.80,0.00,0.00,916.80,5,'VENCIDA'),
(5,1,5,'2026-11-11',813.85,102.95,916.80,0.00,0.00,916.80,0,'PENDIENTE'),
(6,1,6,'2026-12-11',826.06,90.74,916.80,0.00,0.00,916.80,0,'PENDIENTE'),
(7,1,7,'2027-01-11',838.45,78.35,916.80,0.00,0.00,916.80,0,'PENDIENTE'),
(8,1,8,'2027-02-11',851.03,65.77,916.80,0.00,0.00,916.80,0,'PENDIENTE'),
(9,1,9,'2027-03-11',863.79,53.01,916.80,0.00,0.00,916.80,0,'PENDIENTE'),
(10,1,10,'2027-04-11',876.75,40.05,916.80,0.00,0.00,916.80,0,'PENDIENTE'),
(11,1,11,'2027-05-11',889.90,26.90,916.80,0.00,0.00,916.80,0,'PENDIENTE'),
(12,1,12,'2027-06-11',903.26,13.55,916.81,0.00,0.00,916.81,0,'PENDIENTE'),
(13,2,1,'2026-11-20',1041.66,541.67,1583.33,1041.66,541.67,0.00,0,'PAGADA'),
(14,2,2,'2026-12-20',1041.66,530.38,1572.04,1041.66,530.38,0.00,0,'PAGADA'),
(15,2,3,'2027-01-20',1041.66,519.10,1560.76,0.00,0.00,1560.76,0,'PENDIENTE'),
(16,2,4,'2027-02-20',1041.66,507.81,1549.47,0.00,0.00,1549.47,0,'PENDIENTE'),
(17,2,5,'2027-03-20',1041.66,496.53,1538.19,0.00,0.00,1538.19,0,'PENDIENTE'),
(18,2,6,'2027-04-20',1041.66,485.24,1526.90,0.00,0.00,1526.90,0,'PENDIENTE'),
(19,2,7,'2027-05-20',1041.66,473.96,1515.62,0.00,0.00,1515.62,0,'PENDIENTE'),
(20,2,8,'2027-06-20',1041.66,462.67,1504.33,0.00,0.00,1504.33,0,'PENDIENTE'),
(21,2,9,'2027-07-20',1041.66,451.39,1493.05,0.00,0.00,1493.05,0,'PENDIENTE'),
(22,2,10,'2027-08-20',1041.66,440.10,1481.76,0.00,0.00,1481.76,0,'PENDIENTE'),
(23,2,11,'2027-09-20',1041.66,428.82,1470.48,0.00,0.00,1470.48,0,'PENDIENTE'),
(24,2,12,'2027-10-20',1041.66,417.54,1459.20,0.00,0.00,1459.20,0,'PENDIENTE'),
(25,2,13,'2027-11-20',1041.66,406.25,1447.91,0.00,0.00,1447.91,0,'PENDIENTE'),
(26,2,14,'2027-12-20',1041.66,394.97,1436.63,0.00,0.00,1436.63,0,'PENDIENTE'),
(27,2,15,'2028-01-20',1041.66,383.68,1425.34,0.00,0.00,1425.34,0,'PENDIENTE'),
(28,2,16,'2028-02-20',1041.66,372.40,1414.06,0.00,0.00,1414.06,0,'PENDIENTE'),
(29,2,17,'2028-03-20',1041.66,361.11,1402.77,0.00,0.00,1402.77,0,'PENDIENTE'),
(30,2,18,'2028-04-20',1041.66,349.83,1391.49,0.00,0.00,1391.49,0,'PENDIENTE'),
(31,2,19,'2028-05-20',1041.66,338.54,1380.20,0.00,0.00,1380.20,0,'PENDIENTE'),
(32,2,20,'2028-06-20',1041.66,327.26,1368.92,0.00,0.00,1368.92,0,'PENDIENTE'),
(33,2,21,'2028-07-20',1041.66,315.97,1357.63,0.00,0.00,1357.63,0,'PENDIENTE'),
(34,2,22,'2028-08-20',1041.66,304.69,1346.35,0.00,0.00,1346.35,0,'PENDIENTE'),
(35,2,23,'2028-09-20',1041.66,293.40,1335.06,0.00,0.00,1335.06,0,'PENDIENTE'),
(36,2,24,'2028-10-20',1041.66,282.12,1323.78,0.00,0.00,1323.78,0,'PENDIENTE'),
(37,2,25,'2028-11-20',1041.66,270.84,1312.50,0.00,0.00,1312.50,0,'PENDIENTE'),
(38,2,26,'2028-12-20',1041.66,259.55,1301.21,0.00,0.00,1301.21,0,'PENDIENTE'),
(39,2,27,'2029-01-20',1041.66,248.27,1289.93,0.00,0.00,1289.93,0,'PENDIENTE'),
(40,2,28,'2029-02-20',1041.66,236.98,1278.64,0.00,0.00,1278.64,0,'PENDIENTE'),
(41,2,29,'2029-03-20',1041.66,225.70,1267.36,0.00,0.00,1267.36,0,'PENDIENTE'),
(42,2,30,'2029-04-20',1041.66,214.41,1256.07,0.00,0.00,1256.07,0,'PENDIENTE'),
(43,2,31,'2029-05-20',1041.66,203.13,1244.79,0.00,0.00,1244.79,0,'PENDIENTE'),
(44,2,32,'2029-06-20',1041.66,191.84,1233.50,0.00,0.00,1233.50,0,'PENDIENTE'),
(45,2,33,'2029-07-20',1041.66,180.56,1222.22,0.00,0.00,1222.22,0,'PENDIENTE'),
(46,2,34,'2029-08-20',1041.66,169.27,1210.93,0.00,0.00,1210.93,0,'PENDIENTE'),
(47,2,35,'2029-09-20',1041.66,157.99,1199.65,0.00,0.00,1199.65,0,'PENDIENTE'),
(48,2,36,'2029-10-20',1041.66,146.70,1188.36,0.00,0.00,1188.36,0,'PENDIENTE'),
(49,2,37,'2029-11-20',1041.66,135.42,1177.08,0.00,0.00,1177.08,0,'PENDIENTE'),
(50,2,38,'2029-12-20',1041.66,124.13,1165.79,0.00,0.00,1165.79,0,'PENDIENTE'),
(51,2,39,'2030-01-20',1041.66,112.85,1154.51,0.00,0.00,1154.51,0,'PENDIENTE'),
(52,2,40,'2030-02-20',1041.66,101.57,1143.23,0.00,0.00,1143.23,0,'PENDIENTE'),
(53,2,41,'2030-03-20',1041.66,90.28,1131.94,0.00,0.00,1131.94,0,'PENDIENTE'),
(54,2,42,'2030-04-20',1041.66,79.00,1120.66,0.00,0.00,1120.66,0,'PENDIENTE'),
(55,2,43,'2030-05-20',1041.66,67.71,1109.37,0.00,0.00,1109.37,0,'PENDIENTE'),
(56,2,44,'2030-06-20',1041.66,56.43,1098.09,0.00,0.00,1098.09,0,'PENDIENTE'),
(57,2,45,'2030-07-20',1041.66,45.14,1086.80,0.00,0.00,1086.80,0,'PENDIENTE'),
(58,2,46,'2030-08-20',1041.66,33.86,1075.52,0.00,0.00,1075.52,0,'PENDIENTE'),
(59,2,47,'2030-09-20',1041.66,22.57,1064.23,0.00,0.00,1064.23,0,'PENDIENTE'),
(60,2,48,'2030-10-20',1041.98,11.29,1053.27,0.00,0.00,1053.27,0,'PENDIENTE');
/*!40000 ALTER TABLE `cuota` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `desembolso`
--

DROP TABLE IF EXISTS `desembolso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `desembolso` (
  `id_desembolso` int(11) NOT NULL AUTO_INCREMENT,
  `id_credito` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `clave_operacion` varchar(80) NOT NULL,
  `fecha_prevista` date NOT NULL,
  `fecha_efectiva` datetime DEFAULT NULL,
  `monto` decimal(14,2) NOT NULL,
  `cuenta_destino` varchar(40) NOT NULL,
  `referencia_externa` varchar(80) DEFAULT NULL,
  `motivo_rechazo` varchar(300) DEFAULT NULL,
  `estado` enum('SOLICITADO','CONFIRMADO','RECHAZADO','PENDIENTE_VERIFICACION') NOT NULL DEFAULT 'SOLICITADO',
  PRIMARY KEY (`id_desembolso`),
  UNIQUE KEY `clave_operacion` (`clave_operacion`),
  KEY `id_credito` (`id_credito`),
  KEY `id_usuario` (`id_usuario`),
  KEY `idx_desembolso_estado` (`estado`),
  CONSTRAINT `desembolso_ibfk_1` FOREIGN KEY (`id_credito`) REFERENCES `credito` (`id_credito`),
  CONSTRAINT `desembolso_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `desembolso`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `desembolso` WRITE;
/*!40000 ALTER TABLE `desembolso` DISABLE KEYS */;
INSERT INTO `desembolso` VALUES
(1,1,7,'DESEMB-TEST-0001','2026-06-11','2026-06-11 00:00:00',10000.00,'1000234567','CORE-TEST-9001',NULL,'CONFIRMADO'),
(2,2,7,'dfa69214-85ed-4b69-a56f-f6e0dc12c16b','2026-10-20','2026-10-20 00:27:14',50000.00,'123412341234','Core-test-9901',NULL,'CONFIRMADO');
/*!40000 ALTER TABLE `desembolso` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `documento_garantia`
--

DROP TABLE IF EXISTS `documento_garantia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `documento_garantia` (
  `id_documento` int(11) NOT NULL AUTO_INCREMENT,
  `id_garantia` int(11) NOT NULL,
  `tipo_documento` varchar(60) NOT NULL,
  `nombre_archivo` varchar(200) NOT NULL,
  `fecha_carga` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_documento`),
  KEY `id_garantia` (`id_garantia`),
  CONSTRAINT `documento_garantia_ibfk_1` FOREIGN KEY (`id_garantia`) REFERENCES `garantia` (`id_garantia`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `documento_garantia`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `documento_garantia` WRITE;
/*!40000 ALTER TABLE `documento_garantia` DISABLE KEYS */;
INSERT INTO `documento_garantia` VALUES
(2,11,'RESPALDO DE GARANTIA','garantias/be096dcf-d0a7-43d1-a79e-9a858c141529_solicutuAmbulancia.docx','2026-10-19 22:29:21'),
(3,8,'RESPALDO DE GARANTIA','garantias/91d9419e-8144-4218-8e58-649b4e2b0895_ROLTURNOSMARZOSANTIAGO.docx','2026-10-20 00:21:52'),
(4,7,'RESPALDO DE GARANTIA','garantias/1138fe02-f331-4704-972e-4ae614c6db91_adenda_de_Contrato.docx','2026-10-20 00:23:34');
/*!40000 ALTER TABLE `documento_garantia` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `documento_solicitud`
--

DROP TABLE IF EXISTS `documento_solicitud`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `documento_solicitud` (
  `id_documento` int(11) NOT NULL AUTO_INCREMENT,
  `id_solicitud` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `tipo_documento` varchar(60) NOT NULL,
  `nombre_archivo` varchar(200) NOT NULL,
  `estado_verificacion` enum('PENDIENTE','VERIFICADO','OBSERVADO') NOT NULL DEFAULT 'PENDIENTE',
  `fecha_carga` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_documento`),
  KEY `id_solicitud` (`id_solicitud`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `documento_solicitud_ibfk_1` FOREIGN KEY (`id_solicitud`) REFERENCES `solicitud_credito` (`id_solicitud`),
  CONSTRAINT `documento_solicitud_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `documento_solicitud`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `documento_solicitud` WRITE;
/*!40000 ALTER TABLE `documento_solicitud` DISABLE KEYS */;
INSERT INTO `documento_solicitud` VALUES
(1,18,7,'Carnet','solicitudes/8cc5dde8-8759-4f72-8dc0-8e876f51ad05_Patrones_Psicologicos__2_.docx','VERIFICADO','2026-10-20 00:13:54');
/*!40000 ALTER TABLE `documento_solicitud` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `ejecucion_mora`
--

DROP TABLE IF EXISTS `ejecucion_mora`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ejecucion_mora` (
  `id_ejecucion` int(11) NOT NULL AUTO_INCREMENT,
  `id_usuario` int(11) DEFAULT NULL,
  `tipo_ejecucion` enum('AUTOMATICA','MANUAL') NOT NULL,
  `fecha_operativa` date NOT NULL,
  `fecha_inicio` datetime NOT NULL,
  `fecha_fin` datetime DEFAULT NULL,
  `cuotas_procesadas` int(11) NOT NULL DEFAULT 0,
  `resultado` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id_ejecucion`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `ejecucion_mora_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ejecucion_mora`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `ejecucion_mora` WRITE;
/*!40000 ALTER TABLE `ejecucion_mora` DISABLE KEYS */;
/*!40000 ALTER TABLE `ejecucion_mora` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `evaluacion_credito`
--

DROP TABLE IF EXISTS `evaluacion_credito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `evaluacion_credito` (
  `id_evaluacion` int(11) NOT NULL AUTO_INCREMENT,
  `id_solicitud` int(11) NOT NULL,
  `id_politica` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `numero_version` int(11) NOT NULL DEFAULT 1,
  `fecha_evaluacion` datetime NOT NULL DEFAULT current_timestamp(),
  `ingresos` decimal(14,2) NOT NULL DEFAULT 0.00,
  `egresos` decimal(14,2) NOT NULL DEFAULT 0.00,
  `obligaciones_financieras` decimal(14,2) NOT NULL DEFAULT 0.00,
  `cuota_proyectada` decimal(14,2) DEFAULT NULL,
  `capacidad_pago` decimal(14,2) DEFAULT NULL,
  `resultado_capacidad` enum('SUFICIENTE','INSUFICIENTE','PENDIENTE') NOT NULL DEFAULT 'PENDIENTE',
  `nivel_riesgo` enum('BAJO','MEDIO','ALTO') DEFAULT NULL,
  `razones_riesgo` varchar(500) DEFAULT NULL,
  `resultado` enum('FAVORABLE','DESFAVORABLE','PENDIENTE') NOT NULL DEFAULT 'PENDIENTE',
  `recomendacion` varchar(500) DEFAULT NULL,
  `observaciones` varchar(500) DEFAULT NULL,
  `vigente` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_evaluacion`),
  UNIQUE KEY `uq_eval_version` (`id_solicitud`,`numero_version`),
  KEY `id_politica` (`id_politica`),
  KEY `id_usuario` (`id_usuario`),
  KEY `idx_eval_resultado` (`resultado`,`vigente`),
  CONSTRAINT `evaluacion_credito_ibfk_1` FOREIGN KEY (`id_solicitud`) REFERENCES `solicitud_credito` (`id_solicitud`),
  CONSTRAINT `evaluacion_credito_ibfk_2` FOREIGN KEY (`id_politica`) REFERENCES `politica_crediticia` (`id_politica`),
  CONSTRAINT `evaluacion_credito_ibfk_3` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `evaluacion_credito`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `evaluacion_credito` WRITE;
/*!40000 ALTER TABLE `evaluacion_credito` DISABLE KEYS */;
INSERT INTO `evaluacion_credito` VALUES
(1,1,1,2,1,'2026-10-16 19:05:21',6000.00,2500.00,0.00,916.80,1400.00,'SUFICIENTE','BAJO','Sin deudas vigentes, ingresos estables','FAVORABLE','Aprobar el monto solicitado','Documentacion completa',1),
(2,2,1,2,1,'2026-10-16 19:05:21',2000.00,1500.00,200.00,458.40,120.00,'INSUFICIENTE','ALTO','Cuota proyectada supera la capacidad de pago','DESFAVORABLE','Rechazar o reducir el monto',NULL,1),
(3,3,1,2,1,'2026-10-16 19:05:21',5000.00,2000.00,300.00,733.44,NULL,'PENDIENTE',NULL,NULL,'PENDIENTE',NULL,'Falta historial crediticio: servicio de riesgo no disponible',1);
/*!40000 ALTER TABLE `evaluacion_credito` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `garantia`
--

DROP TABLE IF EXISTS `garantia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `garantia` (
  `id_garantia` int(11) NOT NULL AUTO_INCREMENT,
  `id_solicitud` int(11) NOT NULL,
  `id_credito` int(11) DEFAULT NULL,
  `id_tipo_garantia` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `descripcion` varchar(300) DEFAULT NULL,
  `titular` varchar(120) DEFAULT NULL,
  `valor` decimal(14,2) NOT NULL,
  `estado_verificacion` enum('PENDIENTE','VALIDADA') NOT NULL DEFAULT 'PENDIENTE',
  `evidencia_verificacion` varchar(300) DEFAULT NULL,
  `estado` enum('REGISTRADA','EN_CUSTODIA','LIBERADA') NOT NULL DEFAULT 'REGISTRADA',
  PRIMARY KEY (`id_garantia`),
  KEY `id_solicitud` (`id_solicitud`),
  KEY `id_credito` (`id_credito`),
  KEY `id_tipo_garantia` (`id_tipo_garantia`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `garantia_ibfk_1` FOREIGN KEY (`id_solicitud`) REFERENCES `solicitud_credito` (`id_solicitud`),
  CONSTRAINT `garantia_ibfk_2` FOREIGN KEY (`id_credito`) REFERENCES `credito` (`id_credito`),
  CONSTRAINT `garantia_ibfk_3` FOREIGN KEY (`id_tipo_garantia`) REFERENCES `tipo_garantia` (`id_tipo_garantia`),
  CONSTRAINT `garantia_ibfk_4` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `garantia`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `garantia` WRITE;
/*!40000 ALTER TABLE `garantia` DISABLE KEYS */;
INSERT INTO `garantia` VALUES
(1,4,NULL,3,12,'Garantia PERSONAL declarada por el cliente','Pablo Pancho',0.00,'PENDIENTE',NULL,'REGISTRADA'),
(2,5,NULL,1,17,'Garantia HIPOTECARIA declarada por el cliente','Juanita Perez',0.00,'PENDIENTE',NULL,'REGISTRADA'),
(3,6,NULL,2,17,'Garantia PRENDARIA declarada por el cliente','Juanita Perez',0.00,'PENDIENTE',NULL,'REGISTRADA'),
(4,15,NULL,3,22,'Garantia PERSONAL declarada por el cliente','Pancho Villa',0.00,'PENDIENTE',NULL,'REGISTRADA'),
(5,16,NULL,1,23,'Garantia HIPOTECARIA declarada por el cliente','elvis tec',0.00,'PENDIENTE',NULL,'REGISTRADA'),
(6,17,NULL,3,23,'Garantia PERSONAL declarada por el cliente','elvis tec',0.00,'PENDIENTE',NULL,'REGISTRADA'),
(7,18,2,1,24,'Garantia HIPOTECARIA declarada por el cliente','ramses segundo',500000.00,'VALIDADA','respaldo adjunto','EN_CUSTODIA'),
(8,19,NULL,1,24,'Garantia HIPOTECARIA declarada por el cliente','ramses segundo',500000.00,'VALIDADA','respaldo adjunto','REGISTRADA'),
(9,20,NULL,1,26,'Garantia HIPOTECARIA declarada por el cliente','elvis erik',0.00,'PENDIENTE',NULL,'REGISTRADA'),
(10,21,NULL,2,25,'Garantia PRENDARIA declarada por el cliente','JOSEMA NANDO',0.00,'PENDIENTE',NULL,'REGISTRADA'),
(11,22,NULL,1,27,'Garantia HIPOTECARIA declarada por el cliente','elvis erik',700000.00,'VALIDADA','revison del respaldo adjunto','REGISTRADA');
/*!40000 ALTER TABLE `garantia` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `gestion_cobranza`
--

DROP TABLE IF EXISTS `gestion_cobranza`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `gestion_cobranza` (
  `id_gestion` int(11) NOT NULL AUTO_INCREMENT,
  `id_credito` int(11) NOT NULL,
  `id_alerta` int(11) DEFAULT NULL,
  `id_usuario` int(11) NOT NULL,
  `fecha_gestion` datetime NOT NULL DEFAULT current_timestamp(),
  `tipo` enum('PREVENTIVA','COBRANZA') NOT NULL,
  `medio_contacto` varchar(40) NOT NULL,
  `resultado` varchar(200) NOT NULL,
  `observaciones` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id_gestion`),
  KEY `id_credito` (`id_credito`),
  KEY `id_alerta` (`id_alerta`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `gestion_cobranza_ibfk_1` FOREIGN KEY (`id_credito`) REFERENCES `credito` (`id_credito`),
  CONSTRAINT `gestion_cobranza_ibfk_2` FOREIGN KEY (`id_alerta`) REFERENCES `alerta` (`id_alerta`),
  CONSTRAINT `gestion_cobranza_ibfk_3` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gestion_cobranza`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `gestion_cobranza` WRITE;
/*!40000 ALTER TABLE `gestion_cobranza` DISABLE KEYS */;
/*!40000 ALTER TABLE `gestion_cobranza` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `historial_parametro`
--

DROP TABLE IF EXISTS `historial_parametro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `historial_parametro` (
  `id_historial` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_parametro` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `valor_anterior` varchar(100) DEFAULT NULL,
  `valor_nuevo` varchar(100) DEFAULT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_historial`),
  KEY `id_parametro` (`id_parametro`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `historial_parametro_ibfk_1` FOREIGN KEY (`id_parametro`) REFERENCES `parametro` (`id_parametro`),
  CONSTRAINT `historial_parametro_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historial_parametro`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `historial_parametro` WRITE;
/*!40000 ALTER TABLE `historial_parametro` DISABLE KEYS */;
/*!40000 ALTER TABLE `historial_parametro` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_uca1400_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_historial_param_no_update` BEFORE UPDATE ON `historial_parametro` FOR EACH ROW BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial de parametros no se puede modificar (RN66)';
END 
*/;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_uca1400_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_historial_param_no_delete` BEFORE DELETE ON `historial_parametro` FOR EACH ROW BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial de parametros no se puede eliminar (RN66)';
END 
*/;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `movimiento_financiero`
--

DROP TABLE IF EXISTS `movimiento_financiero`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimiento_financiero` (
  `id_movimiento` int(11) NOT NULL AUTO_INCREMENT,
  `id_usuario` int(11) NOT NULL,
  `id_pago` int(11) DEFAULT NULL,
  `id_desembolso` int(11) DEFAULT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT current_timestamp(),
  `tipo` enum('INGRESO','EGRESO') NOT NULL,
  `concepto` varchar(150) NOT NULL,
  `monto` decimal(14,2) NOT NULL,
  PRIMARY KEY (`id_movimiento`),
  KEY `id_usuario` (`id_usuario`),
  KEY `id_pago` (`id_pago`),
  KEY `id_desembolso` (`id_desembolso`),
  CONSTRAINT `movimiento_financiero_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `movimiento_financiero_ibfk_2` FOREIGN KEY (`id_pago`) REFERENCES `pago` (`id_pago`),
  CONSTRAINT `movimiento_financiero_ibfk_3` FOREIGN KEY (`id_desembolso`) REFERENCES `desembolso` (`id_desembolso`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimiento_financiero`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `movimiento_financiero` WRITE;
/*!40000 ALTER TABLE `movimiento_financiero` DISABLE KEYS */;
INSERT INTO `movimiento_financiero` VALUES
(1,7,NULL,2,'2026-10-20 00:27:14','EGRESO','Desembolso confirmado manualmente',50000.00),
(2,3,1,NULL,'2026-10-20 00:30:31','INGRESO','Pago cuota 1 - P-61043c7d88fc47da99254f16c9',1583.33),
(3,3,2,NULL,'2026-10-20 00:40:01','INGRESO','Pago cuota 2 - P-665cc4ddaec84ec49787c98c87',1572.04);
/*!40000 ALTER TABLE `movimiento_financiero` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `movimiento_garantia`
--

DROP TABLE IF EXISTS `movimiento_garantia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimiento_garantia` (
  `id_movimiento` int(11) NOT NULL AUTO_INCREMENT,
  `id_garantia` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `tipo` enum('RECEPCION','CUSTODIA','DEVOLUCION') NOT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT current_timestamp(),
  `observacion` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id_movimiento`),
  KEY `id_garantia` (`id_garantia`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `movimiento_garantia_ibfk_1` FOREIGN KEY (`id_garantia`) REFERENCES `garantia` (`id_garantia`),
  CONSTRAINT `movimiento_garantia_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimiento_garantia`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `movimiento_garantia` WRITE;
/*!40000 ALTER TABLE `movimiento_garantia` DISABLE KEYS */;
/*!40000 ALTER TABLE `movimiento_garantia` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `notificacion`
--

DROP TABLE IF EXISTS `notificacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificacion` (
  `id_notificacion` int(11) NOT NULL AUTO_INCREMENT,
  `id_usuario` int(11) DEFAULT NULL,
  `id_cliente` int(11) DEFAULT NULL,
  `tipo` varchar(40) NOT NULL,
  `mensaje` varchar(500) NOT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT current_timestamp(),
  `estado_envio` enum('PENDIENTE','ENVIADA','FALLIDA') NOT NULL DEFAULT 'PENDIENTE',
  PRIMARY KEY (`id_notificacion`),
  KEY `id_usuario` (`id_usuario`),
  KEY `id_cliente` (`id_cliente`),
  CONSTRAINT `notificacion_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `notificacion_ibfk_2` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notificacion`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `notificacion` WRITE;
/*!40000 ALTER TABLE `notificacion` DISABLE KEYS */;
INSERT INTO `notificacion` VALUES
(1,24,22,'CONFIRMACION_PAGO','Pago recibido: Bs 1583.33 para la cuota 1. Comprobante: P-61043c7d88fc47da99254f16c9. Saldo restante de esa cuota: Bs 0.00.','2026-10-20 00:30:31','ENVIADA'),
(2,24,22,'RECORDATORIO_CUOTA','Credito CR-a9dcfa41b5d8480d9432d33a4e, cuota 2: saldo pendiente Bs 1572.04. Vencimiento: 2026-12-20. Revise sus cuotas y consulte en caja para realizar el pago.','2026-10-20 00:30:53','ENVIADA'),
(3,24,22,'CONFIRMACION_PAGO','Pago recibido: Bs 1572.04 para la cuota 2. Comprobante: P-665cc4ddaec84ec49787c98c87. Saldo restante de esa cuota: Bs 0.00.','2026-10-20 00:40:01','PENDIENTE');
/*!40000 ALTER TABLE `notificacion` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `pago`
--

DROP TABLE IF EXISTS `pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago` (
  `id_pago` int(11) NOT NULL AUTO_INCREMENT,
  `id_credito` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `fecha_pago` datetime NOT NULL DEFAULT current_timestamp(),
  `monto` decimal(14,2) NOT NULL,
  `forma_pago` varchar(30) NOT NULL,
  `referencia` varchar(80) NOT NULL,
  `numero_comprobante` varchar(30) NOT NULL,
  `estado` enum('CONFIRMADO','ANULADO') NOT NULL DEFAULT 'CONFIRMADO',
  PRIMARY KEY (`id_pago`),
  UNIQUE KEY `referencia` (`referencia`),
  UNIQUE KEY `numero_comprobante` (`numero_comprobante`),
  KEY `id_usuario` (`id_usuario`),
  KEY `idx_pago_credito` (`id_credito`),
  KEY `idx_pago_fecha` (`fecha_pago`),
  CONSTRAINT `pago_ibfk_1` FOREIGN KEY (`id_credito`) REFERENCES `credito` (`id_credito`),
  CONSTRAINT `pago_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `pago` WRITE;
/*!40000 ALTER TABLE `pago` DISABLE KEYS */;
INSERT INTO `pago` VALUES
(1,2,3,'2026-10-20 00:30:31',1583.33,'EFECTIVO','P-61043c7d88fc47da99254f16c9','P-61043c7d88fc47da99254f16c9','CONFIRMADO'),
(2,2,3,'2026-10-20 00:40:01',1572.04,'TRANSFERENCIA','P-665cc4ddaec84ec49787c98c87','P-665cc4ddaec84ec49787c98c87','CONFIRMADO');
/*!40000 ALTER TABLE `pago` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_uca1400_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_pago_no_delete` BEFORE DELETE ON `pago` FOR EACH ROW BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Un pago confirmado no se elimina; use anulacion (RN60)';
END 
*/;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `parametro`
--

DROP TABLE IF EXISTS `parametro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `parametro` (
  `id_parametro` int(11) NOT NULL AUTO_INCREMENT,
  `clave` varchar(60) NOT NULL,
  `valor` varchar(100) NOT NULL,
  `descripcion` varchar(200) DEFAULT NULL,
  `fecha_modificacion` datetime NOT NULL DEFAULT current_timestamp(),
  `id_usuario` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_parametro`),
  UNIQUE KEY `clave` (`clave`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `parametro_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parametro`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `parametro` WRITE;
/*!40000 ALTER TABLE `parametro` DISABLE KEYS */;
INSERT INTO `parametro` VALUES
(1,'DIAS_MORA_UMBRAL','30','Dias de atraso para considerar moroso a un cliente (RN36)','2026-10-16 18:56:53',NULL),
(2,'DIAS_RECORDATORIO','3','Dias de anticipacion para recordatorios preventivos (RN40)','2026-10-16 18:56:53',NULL),
(3,'MAX_APLAZAMIENTOS_ANIO','2','Maximo de aplazamientos por credito en 12 meses (RN47)','2026-10-16 18:56:53',NULL),
(4,'DIAS_MIN_ANTICIPACION_MOD','5','Dias minimos antes del vencimiento para solicitar modificacion (RN47)','2026-10-16 18:56:53',NULL),
(5,'MESES_VIGENCIA_HISTORIAL','6','Antiguedad maxima del historial crediticio externo','2026-10-16 18:56:53',NULL),
(6,'INACTIVIDAD_MINUTOS','15','Cierre de sesion por inactividad (RNF03)','2026-10-16 18:56:53',NULL);
/*!40000 ALTER TABLE `parametro` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `permiso`
--

DROP TABLE IF EXISTS `permiso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `permiso` (
  `id_permiso` int(11) NOT NULL AUTO_INCREMENT,
  `codigo` varchar(60) NOT NULL,
  `descripcion` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id_permiso`),
  UNIQUE KEY `codigo` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permiso`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `permiso` WRITE;
/*!40000 ALTER TABLE `permiso` DISABLE KEYS */;
INSERT INTO `permiso` VALUES
(1,'USUARIOS_GESTIONAR','Crear, modificar y deshabilitar usuarios (CU15)'),
(2,'PARAMETROS_CONFIGURAR','Configurar parametros y politicas (CU20)'),
(3,'MORA_EJECUTAR','Ejecutar manualmente el control de mora (CU13)'),
(4,'CLIENTE_REGISTRAR','Registrar y actualizar clientes (CU01, CU16)'),
(5,'CLIENTE_CONSULTAR','Buscar y consultar clientes'),
(6,'SOLICITUD_REGISTRAR','Registrar solicitudes y documentacion (CU02, CU30, CU31)'),
(7,'GARANTIA_REGISTRAR','Registrar garantias (CU06)'),
(8,'EVALUACION_REALIZAR','Iniciar y registrar evaluaciones (CU17, CU03, CU04, CU05)'),
(9,'CREDITO_DECIDIR','Aprobar o rechazar creditos (CU07, CU08)'),
(10,'CONTRATO_FORMALIZAR','Formalizar contrato (CU18)'),
(11,'DESEMBOLSO_REGISTRAR','Desembolsar y verificar desembolsos (CU11, CU19)'),
(12,'PAGO_REGISTRAR','Registrar pagos (CU12)'),
(13,'COBRANZA_GESTIONAR','Registrar gestiones de cobranza y seguimiento preventivo (CU29)'),
(14,'REPORTE_GENERAR','Generar y exportar reportes (CU14)'),
(15,'MODIFICACION_SOLICITAR','Solicitar cambio de fecha o aplazamiento (CU23, CU24)'),
(16,'MODIFICACION_DECIDIR','Evaluar y decidir solicitudes de flexibilidad (CU25, CU26)'),
(17,'CRONOGRAMA_CONSULTAR','Consultar cronograma y estado de cuenta (CU21)');
/*!40000 ALTER TABLE `permiso` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `politica_crediticia`
--

DROP TABLE IF EXISTS `politica_crediticia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `politica_crediticia` (
  `id_politica` int(11) NOT NULL AUTO_INCREMENT,
  `version` int(11) NOT NULL,
  `porcentaje_max_cuota` decimal(5,2) NOT NULL,
  `vigente_desde` date NOT NULL,
  `vigente_hasta` date DEFAULT NULL,
  `activa` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_politica`),
  UNIQUE KEY `version` (`version`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `politica_crediticia`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `politica_crediticia` WRITE;
/*!40000 ALTER TABLE `politica_crediticia` DISABLE KEYS */;
INSERT INTO `politica_crediticia` VALUES
(1,1,40.00,'2026-10-16',NULL,1);
/*!40000 ALTER TABLE `politica_crediticia` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `producto_credito`
--

DROP TABLE IF EXISTS `producto_credito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto_credito` (
  `id_producto` int(11) NOT NULL AUTO_INCREMENT,
  `id_politica` int(11) NOT NULL,
  `nombre` varchar(80) NOT NULL,
  `tasa_interes` decimal(6,3) NOT NULL,
  `monto_minimo` decimal(14,2) NOT NULL,
  `monto_maximo` decimal(14,2) NOT NULL,
  `plazo_maximo_meses` int(11) NOT NULL,
  `tipo_cuota` enum('FIJA','GRADUAL') NOT NULL DEFAULT 'FIJA',
  `exige_garantia` tinyint(1) NOT NULL DEFAULT 0,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_producto`),
  UNIQUE KEY `nombre` (`nombre`),
  KEY `id_politica` (`id_politica`),
  CONSTRAINT `producto_credito_ibfk_1` FOREIGN KEY (`id_politica`) REFERENCES `politica_crediticia` (`id_politica`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto_credito`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `producto_credito` WRITE;
/*!40000 ALTER TABLE `producto_credito` DISABLE KEYS */;
INSERT INTO `producto_credito` VALUES
(1,1,'Consumo',18.000,1000.00,50000.00,36,'FIJA',0,1),
(2,1,'Microcredito',24.000,500.00,30000.00,24,'FIJA',0,1),
(3,1,'Vivienda',10.500,20000.00,500000.00,240,'FIJA',1,1),
(4,1,'Vehicular',13.000,10000.00,200000.00,60,'GRADUAL',1,1);
/*!40000 ALTER TABLE `producto_credito` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `id_rol` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(40) NOT NULL,
  `descripcion` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id_rol`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES
(1,'ADMIN','Administrador: usuarios, roles y parametros'),
(2,'OFICIAL','Oficial de credito: clientes, solicitudes, garantias, formalizacion y desembolso'),
(3,'ANALISTA','Analista de credito: evalua capacidad de pago y riesgo'),
(4,'JEFE','Jefe de creditos: aprueba/rechaza creditos y modificaciones de pago'),
(5,'CAJERO','Cajero: registra pagos y emite comprobantes'),
(6,'GESTOR','Gestor de cobranza: seguimiento preventivo y de mora'),
(7,'GERENTE','Gerente: reportes de gestion'),
(8,'CLIENTE','Cliente: consulta cronograma y solicita flexibilidad de pago');
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `rol_permiso`
--

DROP TABLE IF EXISTS `rol_permiso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol_permiso` (
  `id_rol` int(11) NOT NULL,
  `id_permiso` int(11) NOT NULL,
  PRIMARY KEY (`id_rol`,`id_permiso`),
  KEY `id_permiso` (`id_permiso`),
  CONSTRAINT `rol_permiso_ibfk_1` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id_rol`),
  CONSTRAINT `rol_permiso_ibfk_2` FOREIGN KEY (`id_permiso`) REFERENCES `permiso` (`id_permiso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol_permiso`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `rol_permiso` WRITE;
/*!40000 ALTER TABLE `rol_permiso` DISABLE KEYS */;
INSERT INTO `rol_permiso` VALUES
(1,1),
(1,2),
(1,3),
(2,4),
(2,5),
(3,5),
(4,5),
(5,5),
(6,5),
(2,6),
(2,7),
(3,8),
(4,9),
(2,10),
(2,11),
(5,12),
(6,13),
(7,14),
(8,15),
(4,16),
(2,17),
(4,17),
(5,17),
(6,17),
(8,17);
/*!40000 ALTER TABLE `rol_permiso` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `solicitud_credito`
--

DROP TABLE IF EXISTS `solicitud_credito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `solicitud_credito` (
  `id_solicitud` int(11) NOT NULL AUTO_INCREMENT,
  `id_cliente` int(11) NOT NULL,
  `id_producto` int(11) DEFAULT NULL,
  `id_usuario` int(11) NOT NULL,
  `fecha_solicitud` date NOT NULL,
  `monto_solicitado` decimal(14,2) DEFAULT NULL,
  `plazo_meses` int(11) DEFAULT NULL,
  `destino` varchar(150) DEFAULT NULL,
  `estado` enum('REGISTRADA','PENDIENTE','OBSERVADA','EN_EVALUACION','APROBADA','RECHAZADA') NOT NULL DEFAULT 'REGISTRADA',
  `id_evaluacion_decision` int(11) DEFAULT NULL,
  `id_usuario_decision` int(11) DEFAULT NULL,
  `fecha_decision` datetime DEFAULT NULL,
  `motivo_decision` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id_solicitud`),
  KEY `id_producto` (`id_producto`),
  KEY `id_usuario` (`id_usuario`),
  KEY `id_usuario_decision` (`id_usuario_decision`),
  KEY `fk_solicitud_eval` (`id_evaluacion_decision`),
  KEY `idx_solicitud_estado` (`estado`),
  KEY `idx_solicitud_cliente` (`id_cliente`),
  CONSTRAINT `fk_solicitud_eval` FOREIGN KEY (`id_evaluacion_decision`) REFERENCES `evaluacion_credito` (`id_evaluacion`),
  CONSTRAINT `solicitud_credito_ibfk_1` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  CONSTRAINT `solicitud_credito_ibfk_2` FOREIGN KEY (`id_producto`) REFERENCES `producto_credito` (`id_producto`),
  CONSTRAINT `solicitud_credito_ibfk_3` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `solicitud_credito_ibfk_4` FOREIGN KEY (`id_usuario_decision`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `solicitud_credito`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `solicitud_credito` WRITE;
/*!40000 ALTER TABLE `solicitud_credito` DISABLE KEYS */;
INSERT INTO `solicitud_credito` VALUES
(1,1,1,7,'2026-05-22',10000.00,12,'Capital de trabajo','APROBADA',1,6,'2026-10-16 19:05:21','Evaluacion favorable'),
(2,2,1,7,'2026-10-16',5000.00,12,'Consumo personal','RECHAZADA',2,6,'2026-10-16 19:05:21','La cuota proyectada supera el porcentaje maximo de capacidad de pago'),
(3,3,1,7,'2026-10-16',8000.00,12,'Remodelacion','APROBADA',NULL,15,'2026-10-18 08:53:34','me agrado la idea'),
(4,8,1,12,'2026-10-18',4000.00,4,'Comprar una casa','EN_EVALUACION',NULL,NULL,NULL,NULL),
(5,11,1,17,'2026-10-18',7890.00,12,'necesito capital','APROBADA',NULL,15,'2026-10-18 20:07:18','garantia aceptable'),
(6,11,1,17,'2026-10-18',7890.00,7,'necesito mas capital','APROBADA',NULL,15,'2026-10-18 20:07:29','garantia aceptable'),
(7,12,NULL,7,'2026-10-16',NULL,NULL,'Capital de trabajo','REGISTRADA',NULL,NULL,NULL,NULL),
(8,13,NULL,7,'2026-10-17',NULL,NULL,'Compra de vehiculo','REGISTRADA',NULL,NULL,NULL,NULL),
(9,14,1,7,'2026-10-13',12000.00,18,'Remodelacion del hogar','EN_EVALUACION',NULL,NULL,NULL,NULL),
(10,17,2,7,'2026-10-14',8000.00,12,'Ampliar negocio','EN_EVALUACION',NULL,NULL,NULL,NULL),
(11,16,4,7,'2026-10-15',45000.00,48,'Compra de camioneta','EN_EVALUACION',NULL,NULL,NULL,NULL),
(12,15,3,7,'2026-10-08',60000.00,120,'Compra de terreno','OBSERVADA',NULL,6,'2026-10-10 20:02:38','Falta comprobante de ingresos'),
(13,13,1,7,'2026-09-18',15000.00,24,'Consolidar deudas','APROBADA',NULL,6,'2026-09-20 20:02:38','Buen historial y capacidad de pago'),
(14,14,1,7,'2026-09-28',40000.00,36,'Gastos personales','RECHAZADA',NULL,6,'2026-09-30 20:02:38','La cuota supera la capacidad de pago'),
(15,18,1,22,'2026-10-18',4000.00,9,'necesita para comprarse una casa','APROBADA',NULL,15,'2026-10-18 20:18:11','garantia personal aceptable'),
(16,21,4,23,'2026-10-19',15000.00,24,'vehiculo','RECHAZADA',NULL,15,'2026-10-19 10:51:23','exceso'),
(17,21,3,23,'2026-10-19',200000.00,60,'terreno','EN_EVALUACION',NULL,NULL,NULL,NULL),
(18,22,4,24,'2026-10-19',50000.00,48,'SACAR UN AUTO','APROBADA',NULL,6,'2026-10-20 00:10:03','aprobado'),
(19,22,NULL,24,'2026-10-19',NULL,NULL,'comprar un terreno','REGISTRADA',NULL,NULL,NULL,NULL),
(20,24,NULL,26,'2026-10-19',NULL,NULL,'sacar un vehiculo','REGISTRADA',NULL,NULL,NULL,NULL),
(21,23,1,25,'2026-10-19',20000.00,36,'comprar vehiculo','OBSERVADA',NULL,6,'2026-10-19 21:42:31','evaluacion favorable'),
(22,25,3,27,'2026-10-19',250000.00,48,'comprar un terreno','RECHAZADA',NULL,6,'2026-10-20 00:09:35','motivo de exceso');
/*!40000 ALTER TABLE `solicitud_credito` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `solicitud_modificacion`
--

DROP TABLE IF EXISTS `solicitud_modificacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `solicitud_modificacion` (
  `id_modificacion` int(11) NOT NULL AUTO_INCREMENT,
  `id_credito` int(11) NOT NULL,
  `id_cuota` int(11) NOT NULL,
  `id_usuario_registro` int(11) NOT NULL,
  `tipo` enum('CAMBIO_FECHA','APLAZAMIENTO') NOT NULL,
  `fecha_solicitud` datetime NOT NULL DEFAULT current_timestamp(),
  `motivo` varchar(300) NOT NULL,
  `fecha_propuesta` date DEFAULT NULL,
  `estado` enum('PENDIENTE','APROBADA','RECHAZADA') NOT NULL DEFAULT 'PENDIENTE',
  `id_usuario_decision` int(11) DEFAULT NULL,
  `fecha_decision` datetime DEFAULT NULL,
  `justificacion_decision` varchar(300) DEFAULT NULL,
  `tratamiento_importe` varchar(150) DEFAULT NULL,
  `id_cronograma_nuevo` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_modificacion`),
  KEY `id_credito` (`id_credito`),
  KEY `id_cuota` (`id_cuota`),
  KEY `id_usuario_registro` (`id_usuario_registro`),
  KEY `id_usuario_decision` (`id_usuario_decision`),
  KEY `id_cronograma_nuevo` (`id_cronograma_nuevo`),
  KEY `idx_modif_estado` (`estado`),
  CONSTRAINT `solicitud_modificacion_ibfk_1` FOREIGN KEY (`id_credito`) REFERENCES `credito` (`id_credito`),
  CONSTRAINT `solicitud_modificacion_ibfk_2` FOREIGN KEY (`id_cuota`) REFERENCES `cuota` (`id_cuota`),
  CONSTRAINT `solicitud_modificacion_ibfk_3` FOREIGN KEY (`id_usuario_registro`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `solicitud_modificacion_ibfk_4` FOREIGN KEY (`id_usuario_decision`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `solicitud_modificacion_ibfk_5` FOREIGN KEY (`id_cronograma_nuevo`) REFERENCES `cronograma_pago` (`id_cronograma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `solicitud_modificacion`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `solicitud_modificacion` WRITE;
/*!40000 ALTER TABLE `solicitud_modificacion` DISABLE KEYS */;
/*!40000 ALTER TABLE `solicitud_modificacion` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tipo_garantia`
--

DROP TABLE IF EXISTS `tipo_garantia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_garantia` (
  `id_tipo_garantia` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(60) NOT NULL,
  `descripcion` varchar(200) DEFAULT NULL,
  `datos_requeridos` varchar(300) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_tipo_garantia`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_garantia`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tipo_garantia` WRITE;
/*!40000 ALTER TABLE `tipo_garantia` DISABLE KEYS */;
INSERT INTO `tipo_garantia` VALUES
(1,'HIPOTECARIA','Inmueble en garantia','Folio real, ubicacion, avaluo, titular',1),
(2,'PRENDARIA','Bien mueble en prenda','Descripcion del bien, factura, avaluo',1),
(3,'PERSONAL','Garante personal','CI del garante, ingresos, domicilio',1);
/*!40000 ALTER TABLE `tipo_garantia` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` int(11) NOT NULL AUTO_INCREMENT,
  `id_rol` int(11) NOT NULL,
  `id_cliente` int(11) DEFAULT NULL,
  `nombre_completo` varchar(120) NOT NULL,
  `login` varchar(40) NOT NULL,
  `clave_hash` varchar(100) NOT NULL,
  `limite_aprobacion` decimal(14,2) NOT NULL DEFAULT 0.00,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `login` (`login`),
  UNIQUE KEY `id_cliente` (`id_cliente`),
  KEY `id_rol` (`id_rol`),
  CONSTRAINT `fk_usuario_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  CONSTRAINT `usuario_ibfk_1` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES
(1,1,NULL,'Administrador del Sistema','admin','$2a$10$GAvCLHvoyoalXm6tbH0ije1T8gXAdbK2bbxIJQC8eHAx9fxiE/4W6',0.00,1),
(2,3,NULL,'Analista de Prueba','analista','$2a$10$0yfvh4AxeD/5mSmS9CykaOtEXG5TsA1M9svD/0Eolfqu1OUhj9j4S',0.00,1),
(3,5,NULL,'Cajero de Prueba','cajero','$2a$10$SxWB5ZoKQnJ/5pRYfOBXxehWsarMxAlg4boEbrgXG6YxUucRSMKRG',0.00,1),
(4,7,NULL,'Gerente de Prueba','gerente','$2a$10$PqsC6Bw5vcnY4wKNkdlkfeHjoHHkGW5aavq25Kvn8Mde3WsmUkXSe',0.00,1),
(5,6,NULL,'Gestor de Cobranza de Prueba','gestor','$2a$10$2jNLiz1UYmS84j22Mpd7hu/zQj5GrPVy054rAzIfNtWII.Q8uN/wa',0.00,1),
(6,4,NULL,'Jefe de Creditos de Prueba','jefe','$2a$10$EZlgg6A8UwdFlrlKM11OeuX3qMj2F2/CDWEsaejtQt9l7Ka11Ijyq',60000.00,1),
(7,2,NULL,'Oficial de Prueba','oficial','$2a$10$1HKRBYu99Dpc.LVS9RVfyuuhU2gapJUQCilTNlSyCwojdT..Rs1De',0.00,1),
(8,8,6,'Juanito Perez Mamani','7777777','$2a$10$4PANzSlBpA3xTYUyCDNFGOMH.Pbbgh8wIXRkrFw/wFtIoiJ82yXKm',0.00,1),
(9,8,7,'Maria Guebara Perdidas','12345678','$2a$10$adYi53v1aO.znqoI7hLPHOy37speig/gAgG5yr7N8OlA9wL0G4GNa',0.00,1),
(10,1,NULL,'Administrador Prueba','admin2','$2a$10$yE4YnWpBhPwbgrYQ/2DQReHZb/Jr7IbJWBsnOkyupCzqHrCDTT0d6',0.00,1),
(11,3,NULL,'Franz Tamayo','321','$2a$10$.0XofxqhQpoT1NjrZkowUu536VaB8yW2gpxShmDI1qIOf9Kbs1eou',0.00,1),
(12,8,8,'Pablo Pancho','87654321','$2a$10$.px6tgwAk6dDGYPN.eSbv.zOz.6kotT6.vnIW3G6CVSRCurBc9qdW',0.00,1),
(13,3,NULL,'Brayan Tamayo','987654321','$2a$10$3tVaU7YL4SzB.Tj2AWXGfusTwYxI1pqzlRB1EyZehrE..5mwA0kyu',0.00,1),
(14,4,NULL,'Manuel Mamani','creditosjefe1','$2a$10$WZ1bJ180tWkj9QqnQrTBruBWhVpE9O1.WhO2jALM549EZlZsLOW8u',890.00,1),
(15,4,NULL,'Pato tanque','jefe1','$2a$10$vWfJOxZAPnHBGc3QOAALGe/lNpS6zVSIFcenrK04YfJmC3PJi1mF.',8971.00,1),
(17,8,11,'Juanita Perez','1234560','$2a$10$JHEXN.6egVe6ZhlnWKPOAuwvzKWQNAe9CFn85y43Eg4stYFmJ5oLi',0.00,1),
(18,2,NULL,'Pepe Perez','121415','$2a$10$fiVEnc4/d4.Xn4gyXVp7b.hxM1nC66RjCQlCI0XjzOYSlxIJxmkPW',0.00,1),
(19,5,NULL,'Usu1 us','171717','$2a$10$2GGp8.7UsVXTygBCcnQjY.QtjGydqnyGQHeYnOWFshfMsaySQbr.2',0.00,1),
(20,7,NULL,'G1 g','141414','$2a$10$9xBKrg/HYYKrEHKIIKmzguW8u9kSmaeSO1t7O1ExAnXvzgiY9LAF6',0.00,1),
(21,6,NULL,'GC G','181818','$2a$10$Xg0Ldfx75bjuRaRyXK6km.1eankLPUauMEPe7p8l3FtP8tL0gNNVa',0.00,1),
(22,8,18,'Pancho Villa','808080','$2a$10$.WrJmvxyqBk7kYO6uWn14Ol3pfYs6Sm2V4S750NXA6kQfVAzCIDwu',0.00,1),
(23,8,21,'elvis tec','12345670','$2a$10$k6q5OXY4O7ch0eaEVsHGxu3WM5fmph545rA07kNLR/M60K9rzXsWK',0.00,1),
(24,8,22,'ramses segundo','12345432','$2a$10$aUpi.DsMNgTGdhzxI/YBuOD94buewB.26S52xR0JhcT34/h.a4/Cy',0.00,1),
(25,8,23,'JOSEMA NANDO','11223344','$2a$10$SzX3BQY/o/YjuVAUnQZ91OPCI.z5LYqUMcSh/kl/vDOIZDrZ0ioBq',0.00,1),
(26,8,24,'elvis erik','14182190','$2a$10$0avJQEAzFrEsrg7V7p/2KOg9Z6mcY25JzxFRpwZbu.lAhE2jhLmxm',0.00,1),
(27,8,25,'elvis erik','14182191','$2a$10$Q1gm2K9vOe6PdWInyxGnA.H1yrfCFpSiydwQsOlNooos7qoEyBeLi',0.00,1);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Temporary table structure for view `v_cuotas_vencidas`
--

DROP TABLE IF EXISTS `v_cuotas_vencidas`;
/*!50001 DROP VIEW IF EXISTS `v_cuotas_vencidas`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8mb4;
/*!50001 CREATE VIEW `v_cuotas_vencidas` AS SELECT
 NULL AS `id_cliente`,
 NULL AS `ci`,
 NULL AS `nombres`,
 NULL AS `apellidos`,
 NULL AS `telefono`,
 NULL AS `id_credito`,
 NULL AS `numero_credito`,
 NULL AS `id_cuota`,
 NULL AS `nro_cuota`,
 NULL AS `fecha_vencimiento`,
 NULL AS `dias_atraso`,
 NULL AS `saldo` */;
SET character_set_client = @saved_cs_client;

--
-- Temporary table structure for view `v_estado_credito`
--

DROP TABLE IF EXISTS `v_estado_credito`;
/*!50001 DROP VIEW IF EXISTS `v_estado_credito`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8mb4;
/*!50001 CREATE VIEW `v_estado_credito` AS SELECT
 NULL AS `id_credito`,
 NULL AS `numero_credito`,
 NULL AS `ci`,
 NULL AS `cliente`,
 NULL AS `monto_aprobado`,
 NULL AS `saldo_capital`,
 NULL AS `estado`,
 NULL AS `saldo_exigible` */;
SET character_set_client = @saved_cs_client;

--
-- Temporary table structure for view `v_morosos`
--

DROP TABLE IF EXISTS `v_morosos`;
/*!50001 DROP VIEW IF EXISTS `v_morosos`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8mb4;
/*!50001 CREATE VIEW `v_morosos` AS SELECT
 NULL AS `id_cliente`,
 NULL AS `ci`,
 NULL AS `nombres`,
 NULL AS `apellidos`,
 NULL AS `telefono`,
 NULL AS `id_credito`,
 NULL AS `numero_credito`,
 NULL AS `id_cuota`,
 NULL AS `nro_cuota`,
 NULL AS `fecha_vencimiento`,
 NULL AS `dias_atraso`,
 NULL AS `saldo` */;
SET character_set_client = @saved_cs_client;

--
-- Dumping events for database 'creditos_db'
--

--
-- Dumping routines for database 'creditos_db'
--

--
-- Final view structure for view `v_cuotas_vencidas`
--

/*!50001 DROP VIEW IF EXISTS `v_cuotas_vencidas`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_uca1400_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_cuotas_vencidas` AS select `c`.`id_cliente` AS `id_cliente`,`c`.`ci` AS `ci`,`c`.`nombres` AS `nombres`,`c`.`apellidos` AS `apellidos`,`c`.`telefono` AS `telefono`,`cr`.`id_credito` AS `id_credito`,`cr`.`numero_credito` AS `numero_credito`,`q`.`id_cuota` AS `id_cuota`,`q`.`numero` AS `nro_cuota`,`q`.`fecha_vencimiento` AS `fecha_vencimiento`,`q`.`dias_atraso` AS `dias_atraso`,`q`.`saldo` AS `saldo` from ((((`cuota` `q` join `cronograma_pago` `cp` on(`cp`.`id_cronograma` = `q`.`id_cronograma` and `cp`.`vigente` = 1)) join `credito` `cr` on(`cr`.`id_credito` = `cp`.`id_credito`)) join `solicitud_credito` `s` on(`s`.`id_solicitud` = `cr`.`id_solicitud`)) join `cliente` `c` on(`c`.`id_cliente` = `s`.`id_cliente`)) where `q`.`estado` = 'VENCIDA' */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_estado_credito`
--

/*!50001 DROP VIEW IF EXISTS `v_estado_credito`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_uca1400_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_estado_credito` AS select `cr`.`id_credito` AS `id_credito`,`cr`.`numero_credito` AS `numero_credito`,`c`.`ci` AS `ci`,concat(`c`.`nombres`,' ',`c`.`apellidos`) AS `cliente`,`cr`.`monto_aprobado` AS `monto_aprobado`,`cr`.`saldo_capital` AS `saldo_capital`,`cr`.`estado` AS `estado`,coalesce(sum(case when `q`.`estado` <> 'PAGADA' then `q`.`saldo` end),0) AS `saldo_exigible` from ((((`credito` `cr` join `solicitud_credito` `s` on(`s`.`id_solicitud` = `cr`.`id_solicitud`)) join `cliente` `c` on(`c`.`id_cliente` = `s`.`id_cliente`)) left join `cronograma_pago` `cp` on(`cp`.`id_credito` = `cr`.`id_credito` and `cp`.`vigente` = 1)) left join `cuota` `q` on(`q`.`id_cronograma` = `cp`.`id_cronograma`)) group by `cr`.`id_credito`,`cr`.`numero_credito`,`c`.`ci`,`c`.`nombres`,`c`.`apellidos`,`cr`.`monto_aprobado`,`cr`.`saldo_capital`,`cr`.`estado` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_morosos`
--

/*!50001 DROP VIEW IF EXISTS `v_morosos`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_uca1400_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_morosos` AS select `v`.`id_cliente` AS `id_cliente`,`v`.`ci` AS `ci`,`v`.`nombres` AS `nombres`,`v`.`apellidos` AS `apellidos`,`v`.`telefono` AS `telefono`,`v`.`id_credito` AS `id_credito`,`v`.`numero_credito` AS `numero_credito`,`v`.`id_cuota` AS `id_cuota`,`v`.`nro_cuota` AS `nro_cuota`,`v`.`fecha_vencimiento` AS `fecha_vencimiento`,`v`.`dias_atraso` AS `dias_atraso`,`v`.`saldo` AS `saldo` from `v_cuotas_vencidas` `v` where `v`.`dias_atraso` > (select cast(`parametro`.`valor` as unsigned) from `parametro` where `parametro`.`clave` = 'DIAS_MORA_UMBRAL') */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2026-10-20  9:24:18
