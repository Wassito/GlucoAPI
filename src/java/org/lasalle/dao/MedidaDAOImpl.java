package org.lasalle.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.lasalle.db.Conexion;
import org.lasalle.model.Medida;

/**
 * Implementación JDBC del DAO de medidas.
 */
public class MedidaDAOImpl implements MedidaDAO {

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String SQL_SELECT_BY_USUARIO
            = "SELECT idMedida, idUsuario, glucosa, fechaHora, contexto, notas "
            + "FROM medida WHERE idUsuario = ? ORDER BY fechaHora DESC, idMedida DESC";

    private static final String SQL_INSERT
            = "INSERT INTO medida (idUsuario, glucosa, fechaHora, contexto, notas) "
            + "VALUES (?, ?, ?, ?, ?)";

    @Override
    public List<Medida> getByIdUsuario(int idUsuario) throws SQLException {
        List<Medida> medidas = new ArrayList<>();

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement sentencia = conexion.prepareStatement(SQL_SELECT_BY_USUARIO)) {
            sentencia.setInt(1, idUsuario);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    Medida medida = new Medida();
                    medida.setIdMedida(resultado.getInt("idMedida"));
                    medida.setIdUsuario(resultado.getInt("idUsuario"));
                    medida.setGlucosa(resultado.getInt("glucosa"));
                    Timestamp fechaHora = resultado.getTimestamp("fechaHora");
                    medida.setFechaHora(fechaHora == null ? null
                            : fechaHora.toLocalDateTime().format(FORMATO_FECHA_HORA));
                    medida.setContexto(resultado.getString("contexto"));
                    medida.setNotas(resultado.getString("notas"));
                    medidas.add(medida);
                }
            }
        }

        return medidas;
    }

    @Override
    public Medida save(Medida medida) throws SQLException {
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setInt(1, medida.getIdUsuario());
            sentencia.setInt(2, medida.getGlucosa());

            if (medida.getFechaHora() == null || medida.getFechaHora().trim().isEmpty()) {
                sentencia.setNull(3, Types.TIMESTAMP);
            } else {
                sentencia.setTimestamp(3, Timestamp.valueOf(medida.getFechaHora()));
            }

            if (medida.getContexto() == null || medida.getContexto().trim().isEmpty()) {
                sentencia.setNull(4, Types.VARCHAR);
            } else {
                sentencia.setString(4, medida.getContexto().trim());
            }

            if (medida.getNotas() == null || medida.getNotas().trim().isEmpty()) {
                sentencia.setNull(5, Types.VARCHAR);
            } else {
                sentencia.setString(5, medida.getNotas().trim());
            }

            int filasAfectadas = sentencia.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo registrar la medida");
            }

            try (ResultSet llavesGeneradas = sentencia.getGeneratedKeys()) {
                if (llavesGeneradas.next()) {
                    medida.setIdMedida(llavesGeneradas.getInt(1));
                }
            }
        }

        return medida;
    }
}
