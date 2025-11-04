package es.udc.ws.app.model.encuesta;

import es.udc.ws.app.model.util.exceptions.InstanceNotFoundException;
import java.sql.*;
import java.time.LocalDateTime;

public class Jdbc3CcSqlEncuestaDao extends AbstractSqlEncuestaDao {


    @Override
    public void remove(Connection connection, Long encuestaId)
            throws InstanceNotFoundException {

        String query = "DELETE FROM Encuesta WHERE encuestaId = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, encuestaId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new InstanceNotFoundException(encuestaId, Encuesta.class.getName());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Encuesta create(Connection connection, Encuesta encuesta) {

        String query = "INSERT INTO Encuesta (pregunta, fechaFin, fechaCreacion, " +
                "respuestasPositivas, respuestasNegativas, cancelada) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                query, Statement.RETURN_GENERATED_KEYS)) {

            // Asignar parámetros
            preparedStatement.setString(1, encuesta.getPregunta());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(encuesta.getFechaFin()));
            preparedStatement.setTimestamp(3, Timestamp.valueOf(encuesta.getFechaCreacion()));
            preparedStatement.setInt(4, (int) encuesta.getRespuestasPositivas());
            preparedStatement.setInt(5, (int) encuesta.getRespuestasNegativas());
            preparedStatement.setBoolean(6, encuesta.isCancelada());

            // Ejecutar inserción
            preparedStatement.executeUpdate();

            // Obtener ID generado
            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se pudo obtener el ID generado para la encuesta");
                }
                Long encuestaId = rs.getLong(1);
                encuesta.setEncuestaId(encuestaId);
            }

            return encuesta;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

