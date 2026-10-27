INSERT INTO tecnicos (codigo, documento, nombre, sede, carga_maxima, estado)
SELECT
    'TEC-' || LPAD(numero::TEXT, 3, '0'),
    'TEC-DOC-' || LPAD(numero::TEXT, 3, '0'),
    'Técnico ' || LPAD(numero::TEXT, 3, '0'),
    CASE numero % 4
        WHEN 0 THEN 'CENTRAL'
        WHEN 1 THEN 'NORTE'
        WHEN 2 THEN 'SUR'
        ELSE 'OCCIDENTE'
    END,
    3 + (numero % 4),
    'ACTIVO'
FROM GENERATE_SERIES(1, 50) AS numero;

INSERT INTO tecnico_especialidades (tecnico_id, categoria)
SELECT tecnico.id, categorias.categoria
FROM tecnicos tecnico
CROSS JOIN LATERAL (
    VALUES
        (CASE ((CAST(SUBSTRING(tecnico.codigo FROM 5) AS INTEGER) - 1) % 5)
            WHEN 0 THEN 'HARDWARE'
            WHEN 1 THEN 'SOFTWARE'
            WHEN 2 THEN 'RED'
            WHEN 3 THEN 'ACCESOS'
            ELSE 'OTRO'
        END),
        (CASE (CAST(SUBSTRING(tecnico.codigo FROM 5) AS INTEGER) % 5)
            WHEN 0 THEN 'HARDWARE'
            WHEN 1 THEN 'SOFTWARE'
            WHEN 2 THEN 'RED'
            WHEN 3 THEN 'ACCESOS'
            ELSE 'OTRO'
        END)
) AS categorias(categoria)
WHERE tecnico.codigo BETWEEN 'TEC-001' AND 'TEC-050';
