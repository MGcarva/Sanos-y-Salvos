-- ============================================
-- Sanos y Salvos - Database Initialization
-- ============================================

CREATE DATABASE auth_db;
CREATE DATABASE mascotas_db;
CREATE DATABASE geolocalizacion_db;
CREATE DATABASE coincidencias_db;

-- Enable PostGIS on geolocalizacion_db
\connect geolocalizacion_db
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;
