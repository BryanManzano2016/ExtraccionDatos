-- Database: AlmacenTags

-- DROP DATABASE "AlmacenTags";
/*
CREATE DATABASE "AlmacenTags"
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Spanish_Spain.1252'
    LC_CTYPE = 'Spanish_Spain.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- dataJson: etiqueta, palabra, identificador	
CREATE TABLE historialTags (
   	nombreWeb VARCHAR NOT NULL,
   	dataJson json NOT NULL,
   	elementos VARCHAR[] NOT NULL,
	direccion VARCHAR NOT NULL,
	fechaCreacion TIMESTAMP NOT NULL,	
	PRIMARY KEY (nombreWeb, fechaCreacion)
);

CREATE OR REPLACE FUNCTION cadenaToArray(cadena VARCHAR) RETURNS VARCHAR[] 
AS $$
	BEGIN
		RETURN string_to_array(cadena, ',', '');
	END; 
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION cadenaToJson(cadena VARCHAR) RETURNS JSON 
AS $$
	BEGIN
		RETURN json_object( '{etiqueta, palabraClave}'::TEXT[], cadena::TEXT[] );
	END; 
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE PROCEDURE insertarTag(VARCHAR, VARCHAR, VARCHAR, VARCHAR) LANGUAGE plpgsql
AS $$
	BEGIN
		INSERT INTO historialTags(nombreWeb, dataJson, elementos, direccion, fechaCreacion) 
			VALUES ( $1, cadenaToJson($2), cadenaToArray($3), $4, now());
	END;
$$;

CREATE TABLE registroingresos ( 
	id SERIAL PRIMARY KEY,
   	ip VARCHAR,
   	fechaIngreso TIMESTAMP NOT NULL
);
 
CREATE OR REPLACE FUNCTION log_ip()
  RETURNS trigger AS
	$BODY$
		BEGIN
			INSERT INTO registroingresos(ip, fechaIngreso) VALUES (NEW.direccion, now());
			RETURN NEW;
		END;
	$BODY$ LANGUAGE plpgsql;
 
CREATE TRIGGER lanzadorRegistros
  BEFORE INSERT
  ON historialTags
  FOR EACH ROW
  EXECUTE PROCEDURE log_ip();
*/
-- CALL insertarTag('http://127.0.0.1:49762/browser/estados', '{p,comercio}', '1, a, true', '127.0.0.199');



