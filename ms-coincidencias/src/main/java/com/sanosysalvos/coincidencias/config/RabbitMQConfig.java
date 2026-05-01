package com.sanosysalvos.coincidencias.config;

/**
 * RabbitMQ fue reemplazado por Amazon SQS.
 * La configuración de SQS es auto-manejada por spring-cloud-aws-starter-sqs.
 * Ver application.yml para las URLs de las colas SQS.
 */
public class RabbitMQConfig {
    // Clase retenida para compatibilidad de imports históricos.
    // No contiene beans activos.
    public static final String COINCIDENCIAS_QUEUE = "coincidencias.queue"; // legacy
    public static final String NOTIFICATION_ROUTING_KEY = "coincidencia.encontrada"; // legacy
    public static final String EXCHANGE = "sanos-salvos"; // legacy
}
