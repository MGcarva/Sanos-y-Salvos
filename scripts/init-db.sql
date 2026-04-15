-- ============================================
-- Sanos y Salvos - Database Initialization
-- Creates all 4 databases + PostGIS extension
-- ============================================

-- Create databases
CREATE DATABASE auth_db;
CREATE DATABASE mascotas_db;
CREATE DATABASE geolocalizacion_db;
CREATE DATABASE coincidencias_db;

-- Enable PostGIS on geolocalizacion_db
\c geolocalizacion_db;
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Enable pg_trgm on coincidencias_db (for fuzzy matching)
\c coincidencias_db;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
