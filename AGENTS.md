# Instrucciones para agentes
Lee docs/CONTEXTO.md antes de cambiar nada y respeta sus reglas (arquitectura hexagonal, no tocar domain sin consultar, no borrar ni recrear archivos existentes).
Haz solo lo que pida cada tarea. Si necesitas modificar domain o application más allá de lo pedido, pregunta antes.
Verifica con: ./mvnw -f support-ticket-service/pom.xml test
Los tests de integración usan Testcontainers y necesitan Docker.
