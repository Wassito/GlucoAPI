-- Ejecutar una sola vez en TiDB Cloud SQL Editor (base de datos test).
-- Si prefieres otra base, créala y ajusta DB_URL en Render.
CREATE TABLE IF NOT EXISTS usuario (
    idUsuario INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    primerApellido VARCHAR(50) NOT NULL,
    segundoApellido VARCHAR(50),
    email VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS medida (
    idMedida INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NOT NULL,
    glucosa INT NOT NULL,
    fechaHora DATETIME,
    contexto VARCHAR(50),
    notas VARCHAR(100),
    FOREIGN KEY (idUsuario) REFERENCES usuario(idUsuario)
);
