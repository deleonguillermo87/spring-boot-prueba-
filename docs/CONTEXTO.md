# Contexto del proyecto HelpFlow (leer antes de modificar código)

Prueba de desempeño RIWI Módulo 6. Monorepo con dos microservicios Java 21 + Maven (`./mvnw -f <servicio>/pom.xml`).

## Servicios
- `technician-dispatch-service` (Spring Boot 3.5.6, puerto 8081): TERMINADO. POST /technician-dispatch determinista. No tocar.
- `support-ticket-service` (Spring Boot 4.1.2, Hibernate 7, Security 7, Testcontainers 2.x, paquete com.helpflow.supportticket).

## Arquitectura hexagonal (reglas obligatorias)
- `domain`: Java puro. Prohibido importar Spring, Jakarta, Jackson, Lombok o MapStruct. No modificar sin consultar.
- `application`: puertos in/out, DTOs y casos de uso. Solo se permite `@Service` y `@Transactional` de Spring. Prohibido importar org.springframework.web, security, data, jakarta.persistence o micrometer.
- Paginación propia: PaginaSolicitud, PaginaResultado, FiltroTickets. Nunca Page de Spring fuera del adaptador JPA.
- `infrastructure`: adaptadores REST, JPA, cliente HTTP, seguridad, observabilidad, scheduler, config.
- Autorización por Actor (id de usuario + rol); el técnico se vincula con tecnicos.usuario_id.

## Decisiones tomadas
- JWT HS256 con Nimbus (oauth2-resource-server), secreto en variable JWT_SECRET (nunca en el repo), 60 min.
- Primer ADMIN solo vía variables ADMIN_EMAIL, ADMIN_PASSWORD, ADMIN_DOCUMENTO.
- Errores siempre ProblemDetail (RFC 7807) con type, title, status, detail, instance, timestamp y traceId.
- Flyway V1 schema, V2 asignaciones/historial/sla, V3 datos iniciales (técnicos TEC-001 a TEC-050). ddl-auto=validate, open-in-view=false, batch_fetch_size=50. Sin join fetch de colecciones con paginación.
- Si el dispatch falla, el ticket queda ABIERTO sin asignar y se registra en el historial.

## Estado
HECHO: dispatch-service, dominio, casos de uso, paginación, persistencia + Testcontainers, seguridad JWT, métricas Micrometer. 65 tests en verde.
PENDIENTE: controllers REST + @ControllerAdvice + cliente de dispatch + Swagger; scheduler de escalamiento SLA; edición de perfil de usuario/técnico; tests MockMvc con JWT; Dockerfiles y docker-compose; README, diagramas, Postman, capturas.

## Reglas de trabajo
- Hacer solo lo pedido en cada tarea. Nunca borrar archivos existentes ni recrearlos desde cero.
- Si hay que tocar domain o application más allá de lo pedido, preguntar antes.
- Verificar siempre con `./mvnw -f support-ticket-service/pom.xml test` (Docker en marcha) y mostrar el resultado.
- No inventar versiones de dependencias ni incluir secretos en el repo.
