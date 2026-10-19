INSERT INTO nodo_catalogo (id, tipo, nombre, padre_id) VALUES
    (1, 'CATEGORIA', 'Programación', NULL),
    (2, 'CATEGORIA', 'Backend', 1),
    (3, 'CATEGORIA', 'Frontend', 1),
    (4, 'CATEGORIA', 'Datos', NULL),
    (5, 'CATEGORIA', 'Diseño', NULL),
    (6, 'CATEGORIA', 'Idiomas', NULL),
    (10, 'HABILIDAD', 'Java', 2),
    (11, 'HABILIDAD', 'SQL', 4),
    (12, 'HABILIDAD', 'Python', 2),
    (13, 'HABILIDAD', 'JavaScript', 3),
    (14, 'HABILIDAD', 'Hibernate', 2),
    (15, 'HABILIDAD', 'Análisis de datos', 4),
    (16, 'HABILIDAD', 'Diseño de interfaces', 5),
    (17, 'HABILIDAD', 'Inglés', 6),
    (18, 'HABILIDAD', 'Arquitectura de software', 1);

ALTER TABLE nodo_catalogo AUTO_INCREMENT = 100;
