package org.lasalle.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase encargada de administrar la conexión con MySQL.
 *
 * En NetBeans lee db.properties; en Render usa DB_URL, DB_USER y DB_PASSWORD.
 */
public class Conexion {

    private static final Logger LOGGER = Logger.getLogger(Conexion.class.getName());

    private static String url;
    private static String usuario;
    private static String password;

    static {
        Properties propiedades = new Properties();

        try (InputStream entrada = Conexion.class.getResourceAsStream("/db.properties")) {
            if (entrada != null) {
                propiedades.load(entrada);
            }

            String driver = propiedades.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            Class.forName(driver);

            // En Render, las variables de entorno tienen prioridad sobre la
            // configuración local de NetBeans.
            url = configurar("DB_URL", propiedades.getProperty("db.url"));
            usuario = configurar("DB_USER", propiedades.getProperty("db.user"));
            password = configurar("DB_PASSWORD", propiedades.getProperty("db.password", ""));

            if (url == null || url.isBlank()) {
                throw new IllegalStateException("Configura DB_URL o db.url para conectar a la base de datos");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar la configuración de la base de datos", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private Conexion() {
    }

    private static String configurar(String variable, String alternativa) {
        String valor = System.getenv(variable);
        return valor == null || valor.isBlank() ? alternativa : valor;
    }

    /**
     * Obtiene una nueva conexión a la base de datos.
     *
     * @return conexión abierta a MySQL
     * @throws SQLException si no se puede establecer la conexión
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, password);
    }

    /**
     * Método de prueba para validar la conexión desde NetBeans.
     *
     * @param args argumentos de la línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        try (Connection conexion = getConnection()) {
            System.out.println("Conexión exitosa a la base de datos: " + conexion.getCatalog());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al conectar con MySQL", e);
        }
    }
}
