-- 1. Insertar Departamentos (Incluimos Callao por ser parte de Lima Metropolitana comercialmente)
INSERT INTO ubigeo_departments (id, name) VALUES
                                              ('15', 'LIMA'),
                                              ('07', 'CALLAO');

-- 2. Insertar Provincias
INSERT INTO ubigeo_provinces (id, department_id, name) VALUES
                                                           ('1501', '15', 'LIMA'),
                                                           ('0701', '07', 'CALLAO');

-- 3. Insertar Distritos
INSERT INTO ubigeo_districts (id, province_id, name) VALUES
-- Los 43 distritos de la Provincia de Lima (1501)
('150101', '1501', 'LIMA (CERCADO)'),
('150102', '1501', 'ANCÓN'),
('150103', '1501', 'ATE'),
('150104', '1501', 'BARRANCO'),
('150105', '1501', 'BREÑA'),
('150106', '1501', 'CARABAYLLO'),
('150107', '1501', 'CHACLACAYO'),
('150108', '1501', 'CHORRILLOS'),
('150109', '1501', 'CIENEGUILLA'),
('150110', '1501', 'COMAS'),
('150111', '1501', 'EL AGUSTINO'),
('150112', '1501', 'INDEPENDENCIA'),
('150113', '1501', 'JESÚS MARÍA'),
('150114', '1501', 'LA MOLINA'),
('150115', '1501', 'LA VICTORIA'),
('150116', '1501', 'LINCE'),
('150117', '1501', 'LOS OLIVOS'),
('150118', '1501', 'LURIGANCHO-CHOSICA'),
('150119', '1501', 'LURÍN'),
('150120', '1501', 'MAGDALENA DEL MAR'),
('150121', '1501', 'PUEBLO LIBRE'),
('150122', '1501', 'MIRAFLORES'),
('150123', '1501', 'PACHACÁMAC'),
('150124', '1501', 'PUCUSANA'),
('150125', '1501', 'PUENTE PIEDRA'),
('150126', '1501', 'PUNTA HERMOSA'),
('150127', '1501', 'PUNTA NEGRA'),
('150128', '1501', 'RÍMAC'),
('150129', '1501', 'SAN BARTOLO'),
('150130', '1501', 'SAN BORJA'),
('150131', '1501', 'SAN ISIDRO'),
('150132', '1501', 'SAN JUAN DE LURIGANCHO'),
('150133', '1501', 'SAN JUAN DE MIRAFLORES'),
('150134', '1501', 'SAN LUIS'),
('150135', '1501', 'SAN MARTÍN DE PORRES'),
('150136', '1501', 'SAN MIGUEL'),
('150137', '1501', 'SANTA ANITA'),
('150138', '1501', 'SANTA MARÍA DEL MAR'),
('150139', '1501', 'SANTA ROSA'),
('150140', '1501', 'SANTIAGO DE SURCO'),
('150141', '1501', 'SURQUILLO'),
('150142', '1501', 'VILLA EL SALVADOR'),
('150143', '1501', 'VILLA MARÍA DEL TRIUNFO'),

-- Los 7 distritos de la Provincia Constitucional del Callao (0701)
('070101', '0701', 'CALLAO'),
('070102', '0701', 'BELLAVISTA'),
('070103', '0701', 'CARMEN DE LA LEGUA REYNOSO'),
('070104', '0701', 'LA PERLA'),
('070105', '0701', 'LA PUNTA'),
('070106', '0701', 'VENTANILLA'),
('070107', '0701', 'MI PERÚ');