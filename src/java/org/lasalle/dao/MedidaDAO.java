package org.lasalle.dao;

import java.sql.SQLException;
import org.lasalle.model.Medida;

/**
 * Contrato de las operaciones de acceso a datos de medida.
 */
public interface MedidaDAO {

    /**
     * Inserta una nueva medida de glucosa.
     *
     * @param medida datos de la medida a guardar
     * @return la medida guardada, incluyendo el id generado
     * @throws SQLException si ocurre un error de base de datos
     */
    Medida save(Medida medida) throws SQLException;
}
