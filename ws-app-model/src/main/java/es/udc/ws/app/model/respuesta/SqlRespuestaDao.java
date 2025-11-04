package es.udc.ws.app.model.respuesta;

import es.udc.ws.app.model.util.exceptions.InstanceNotFoundException;
import es.udc.ws.util.sql.DataSourceLocator;
//import es.udc.ws.util.jdbc.JdbcUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class SqlRespuestaDao implements RespuestaDao {

    private final static String JNDI_NAME = "jdbc/ws-javaexamples-ds";

    @Override
    public Respuesta create(Respuesta respuesta) {

        String queryString = "INSERT INTO Respuesta (encuestaId, emailEmpleado, afirmativa, fechaRespuesta)"
                + " VALUES (?, ?, ?, ?)";

        try (Connection connection = DataSourceLocator.getDataSource(JNDI_NAME).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     queryString, Statement.RETURN_GENERATED_KEYS)) {

            // Asignar parámetros
            preparedStatement.setLong(1, respuesta.getEncuestaId());
            preparedStatement.setString(2, respuesta.getEmailEmpleado());
            preparedStatement.setBoolean(3, respuesta.isAfirmativa());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(respuesta.getFechaRespuesta()));

            preparedStatement.executeUpdate();

            // Obtener el ID generado
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (!rs.next()) {
                throw new SQLException("No se pudo obtener el ID generado para la respuesta");
            }
            Long respuestaId = rs.getLong(1);

            // Devolver la respuesta actualizada con su ID
            respuesta.setRespuestaId(respuestaId);
            return respuesta;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Respuesta respuesta) throws InstanceNotFoundException {

        String queryString = "UPDATE Respuesta SET emailEmpleado = ?, afirmativa = ?, fechaRespuesta = ? "
                + "WHERE respuestaId = ?";

        try (Connection connection = DataSourceLocator.getDataSource(JNDI_NAME).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            // Asignar parámetros
            preparedStatement.setString(1, respuesta.getEmailEmpleado());
            preparedStatement.setBoolean(2, respuesta.isAfirmativa());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(respuesta.getFechaRespuesta()));
            preparedStatement.setLong(4, respuesta.getRespuestaId());

            // Ejecutar la actualización
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new InstanceNotFoundException(respuesta.getRespuestaId(), Respuesta.class.getName());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Respuesta findByEmailAndEncuestaId(Long encuestaId, String emailEmpleado) {

        String queryString = "SELECT respuestaId, afirmativa, fechaRespuesta "
                + "FROM Respuesta WHERE encuestaId = ? AND emailEmpleado = ?";

        try (Connection connection = DataSourceLocator.getDataSource(JNDI_NAME).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            preparedStatement.setLong(1, encuestaId);
            preparedStatement.setString(2, emailEmpleado);

            ResultSet rs = preparedStatement.executeQuery();

            if (!rs.next()) {
                // No es un error, simplemente no ha respondido todavía
                return null;
            }

            // Recuperar datos del ResultSet
            Long respuestaId = rs.getLong(1);
            boolean afirmativa = rs.getBoolean(2);
            LocalDateTime fechaRespuesta = rs.getTimestamp(3).toLocalDateTime();

            // Crear y devolver el objeto Respuesta
            return new Respuesta(respuestaId, encuestaId, emailEmpleado, afirmativa, fechaRespuesta);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Respuesta> findByEncuestaId(Long encuestaId, boolean soloAfirmativas) {

        List<Respuesta> respuestas = new ArrayList<>();

        // Preparamos la consulta SQL base
        String queryString = "SELECT respuestaId, emailEmpleado, afirmativa, fechaRespuesta "
                + "FROM Respuesta WHERE encuestaId = ?";

        // Añadimos la condición de [FUNC-6] si es necesario
        if (soloAfirmativas) {
            queryString += " AND afirmativa = true";
        }

        queryString += " ORDER BY fechaRespuesta DESC"; // Ordenamos por fecha

        try (Connection connection = DataSourceLocator.getDataSource(JNDI_NAME).getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            preparedStatement.setLong(1, encuestaId);

            // Ejecutamos la consulta
            ResultSet rs = preparedStatement.executeQuery();

            // Recorremos los resultados
            while (rs.next()) {
                Long respuestaId = rs.getLong(1);
                String emailEmpleado = rs.getString(2);
                boolean afirmativa = rs.getBoolean(3);
                LocalDateTime fechaRespuesta = rs.getTimestamp(4).toLocalDateTime();

                // Añadimos la respuesta a la lista
                respuestas.add(new Respuesta(respuestaId, encuestaId, emailEmpleado, afirmativa, fechaRespuesta));
            }

            // Devolvemos la lista (estará vacía si no se encontró nada)
            return respuestas;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
