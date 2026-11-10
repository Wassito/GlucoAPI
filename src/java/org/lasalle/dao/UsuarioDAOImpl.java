package org.lasalle.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.lasalle.db.Conexion;
import org.lasalle.model.Usuario;

/**
 * Implementación JDBC del DAO de usuarios.
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    private static final String SQL_SELECT_ALL
            = "SELECT idUsuario, nombre, primerApellido, segundoApellido, email "
            + "FROM usuario ORDER BY idUsuario";

    private static final String SQL_INSERT
            = "INSERT INTO usuario (nombre, primerApellido, segundoApellido, email) "
            + "VALUES (?, ?, ?, ?)";

    @Override
    public List<Usuario> getAllUsuarios() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement sentencia = conexion.prepareStatement(SQL_SELECT_ALL);
                ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(resultado.getInt("idUsuario"));
                usuario.setNombre(resultado.getString("nombre"));
                usuario.setPrimerApellido(resultado.getString("primerApellido"));
                usuario.setSegundoApellido(resultado.getString("segundoApellido"));
                usuario.setEmail(resultado.getString("email"));
                usuarios.add(usuario);
            }
        }

        return usuarios;
    }

    @Override
    public Usuario save(Usuario usuario) throws SQLException {
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setString(1, usuario.getNombre());
            sentencia.setString(2, usuario.getPrimerApellido());

            if (usuario.getSegundoApellido() == null || usuario.getSegundoApellido().trim().isEmpty()) {
                sentencia.setNull(3, Types.VARCHAR);
            } else {
                sentencia.setString(3, usuario.getSegundoApellido().trim());
            }

            sentencia.setString(4, usuario.getEmail());

            int filasAfectadas = sentencia.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo registrar el usuario");
            }

            try (ResultSet llavesGeneradas = sentencia.getGeneratedKeys()) {
                if (llavesGeneradas.next()) {
                    usuario.setIdUsuario(llavesGeneradas.getInt(1));
                }
            }
        }

        return usuario;
    }
}
