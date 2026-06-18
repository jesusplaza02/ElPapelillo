# ElPapelillo

Este repositorio contiene el código fuente, la configuración de la base de datos y la documentación técnica del Trabajo de Fin de Grado (TFG) "ElPapelillo" desarrollado para la Universidad de Málaga (UMA).

El Papelillo es una plataforma orientada a la gestión integral de concursos carnavalescos. El sistema permite la administración de usuarios (Administradores, Representantes), el registro de agrupaciones, la gestión de inscripciones a concursos y una trazabilidad completa mediante un sistema de logs de auditoría.

--------------------------------------------------------------------------------------
👥 Autor
- Alumno: [Antonio Jesús Díaz Plaza]

- Tutor: [Francisco José Jaime Rodríguez]

- Grado: Ingeniería Informática - Universidad de Málaga (UMA).
--------------------------------------------------------------------------------------
📂 Estructura del Repositorio

├── ElPapelillo-backend/
│   ├── src/                    → Código fuente de la aplicación (Spring Boot, arquitectura por capas).
│   ├── archivos/               → Archivos utilizados por la aplicación.
│   ├── target/                 → Artefactos generados durante la compilación.
│   ├── pom.xml                 → Configuración de dependencias y construcción con Maven.
│   ├── mvnw / mvnw.cmd         → Maven Wrapper para la ejecución del proyecto.
│   ├── Base de datos SQL ElPapelillo.sql
│   └── Creacion datos elpapelillo_db.sql
│       → Scripts de creación e inicialización de la base de datos.
│
├── ElPapelillo-frontend/
│   ├── src/                    → Código fuente de la aplicación Angular.
│   ├── public/                 → Recursos estáticos públicos.
│   ├── angular.json            → Configuración principal de Angular.
│   ├── package.json            → Dependencias y scripts de Node.js.
│   └── tsconfig*.json          → Configuración de TypeScript.
│
├── Documentacion/
│   ├── Diagramas/              → Diagramas de clases, casos de uso y secuencia.
│   ├── MockUps/                → Prototipos y diseños de la interfaz.
│   ├── Pruebas ElPapelillo.xlsx  → Registro de pruebas realizadas.  
│   └── Script BBDD ElPapelillo final.sql   → Script final de la base de datos.
│
├── .github/                    → Configuración y recursos de GitHub.
└── README.md                   → Información general del proyecto.
--------------------------------------------------------------------------------------
⚖️ Licencia y Propiedad Intelectual
© 2026 [Antonio Jesús Díaz Plaza] - Universidad de Málaga.

TODOS LOS DERECHOS RESERVADOS. Este software se publica exclusivamente con fines de evaluación académica. Queda prohibida la copia, reproducción, distribución o uso comercial de cualquier parte de este código sin el consentimiento previo y por escrito del autor.
