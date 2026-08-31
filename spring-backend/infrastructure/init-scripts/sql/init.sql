SELECT 'CREATE DATABASE banditgames'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'banditgames')\gexec

SELECT 'CREATE DATABASE keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec
