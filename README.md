# ElPapelillo 🎭

> **Trabajo de Fin de Grado (TFG)**  
> Grado en Ingeniería del Software — Universidad de Málaga (UMA)  
> Curso académico 2025/2026

---

## Descripción del Proyecto

**ElPapelillo** es una aplicación web de gestión integral de **Concursos de Agrupaciones Carnavalescas**, desarrollada con **Java 17** y **Spring Boot 3**. El sistema digitaliza todo el ciclo de vida de un concurso: desde la inscripción de agrupaciones (comparsas, chirigotas, coros, cuartetos y romanceros) hasta la gestión de representantes y el registro de auditoría de acciones.

El nombre hace referencia al tradicional "papelillo" de carnaval, hoja volante con coplas y letras que las agrupaciones reparten durante el concurso.

---

## Funcionalidades Principales

- 🎭 **Gestión de Concursos**: Creación, apertura, cierre y cancelación de concursos.
- 🎶 **Gestión de Agrupaciones**: Inscripción de comparsas, chirigotas, coros, cuartetos y romanceros.
- 👤 **Gestión de Usuarios**: Administradores y representantes con control de acceso por rol.
- 🔐 **Seguridad**: Autenticación HTTP Basic con BCrypt y autorización por roles.
- 📋 **Auditoría**: Log completo de todas las acciones realizadas en el sistema.
- ✅ **Validación de DNI/NIE**: Algoritmo oficial de verificación de letra de control.

---

## Arquitectura y Tecnologías

| Capa | Tecnología |
|------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Security |
| Persistencia | Spring Data JPA, Hibernate, MySQL 8 |
| Validación | Jakarta Bean Validation |
| Build | Apache Maven |
| IDE recomendado | IntelliJ IDEA |

### Estructura de paquetes

```
src/main/java/es/uma/ajdp/tfg/elpapelillo/
├── models/        # Entidades JPA (Usuario, Administrador, Representante, Concurso, Agrupacion, LogSistema)
├── repositories/  # Interfaces JpaRepository
├── services/      # Lógica de negocio y validación
├── controllers/   # Endpoints REST con ResponseEntity
└── config/        # Configuración de Spring Security
```

---

## Requisitos del Sistema

- Java 17+
- MySQL 8+
- Maven 3.8+

---

## Configuración e Instalación

1. **Clonar el repositorio** (solo con autorización del autor):
   ```bash
   git clone https://github.com/jesusplaza02/ElPapelillo.git
   cd ElPapelillo
   ```

2. **Configurar la base de datos** en `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/elpapelillo?createDatabaseIfNotExist=true
   spring.datasource.username=TU_USUARIO
   spring.datasource.password=TU_CONTRASEÑA
   ```

3. **Compilar y ejecutar**:
   ```bash
   mvn spring-boot:run
   ```

4. La API REST estará disponible en `http://localhost:8080/api/`

---

## Endpoints Principales

| Método | Ruta | Descripción | Rol requerido |
|--------|------|-------------|---------------|
| GET | `/api/concursos` | Lista todos los concursos | Público |
| POST | `/api/concursos` | Crea un nuevo concurso | ADMINISTRADOR |
| GET | `/api/agrupaciones` | Lista todas las agrupaciones | Público |
| POST | `/api/agrupaciones/concurso/{id}` | Inscribe una agrupación | ADMIN / REPRESENTANTE |
| GET | `/api/usuarios` | Lista todos los usuarios | ADMINISTRADOR |
| GET | `/api/logs` | Consulta el log del sistema | ADMINISTRADOR |

---

## Documentación Académica

La documentación técnica y la memoria del TFG se encuentran en la carpeta [`/docs`](./docs/).

---

## LICENSE

© 2026 Jesús Plaza. Todos los derechos reservados.

Queda **prohibida** la copia, distribución, modificación o uso comercial de este código sin la **autorización expresa y por escrito** del autor. Este repositorio es de uso exclusivo académico en el contexto del Trabajo de Fin de Grado presentado ante la Universidad de Málaga (UMA). Cualquier uso no autorizado constituye una infracción de los derechos de propiedad intelectual del autor.
