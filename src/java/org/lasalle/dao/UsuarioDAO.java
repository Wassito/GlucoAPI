package org.lasalle.dao;

import java.sql.SQLException;
import java.util.List;
import org.lasalle.model.Usuario;

/**
 * Contrato de las operaciones de acceso a datos de usuario.
 */
public interface UsuarioDAO {

    /**
     * Obtiene todos los usuarios registrados.
     *
     * @return lista de usuarios
     * @throws SQLException si ocurre un error de base de datos
     */
    List<Usuario> getAllUsuarios() throws SQLException;

    /**
     * Inserta un nuevo usuario.
     *
     * @param usuario datos del usuario a guardar
     * @return el usuario guardado, incluyendo el id generado
     * @throws SQLException si ocurre un error de base de datos
     */
    Usuario save(Usuario usuario) throws SQLException;
}
