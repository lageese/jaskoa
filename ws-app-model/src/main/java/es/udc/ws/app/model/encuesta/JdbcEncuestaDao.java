package es.udc.ws.app.model.encuesta;

// Imports de la práctica
import es.udc.ws.app.model.util.exceptions.InstanceNotFoundException;
import es.udc.ws.util.jdbc.DataSourceLocator;
import es.udc.ws.util.jdbc.JdbcUtils;

// Imports de Java SQL
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

// Imports de Java Util
import java.util.List;
// Esta clase IMPLEMENTA la interfaz que acabas de arreglar
public class JdbcEncuestaDao implements EncuestaDao {

    public JdbcEncuestaDao() {
    }

    @Override
    public Encuesta create(Encuesta encuesta) {

        String queryString = "INSERT INTO Encuesta"
                + " (pregunta, fechaCreacion, fechaFin, respuestasPositivas, respuestasNegativas, cancelada)"
                + " VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DataSourceLocator.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     queryString, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, encuesta.getPregunta());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(encuesta.getFechaCreacion()));
            preparedStatement.setTimestamp(3, Timestamp.valueOf(encuesta.getFechaFin()));
            preparedStatement.setLong(4, encuesta.getRespuestasPositivas());
            preparedStatement.setLong(5, encuesta.getRespuestasNegativas());
            preparedStatement.setBoolean(6, encuesta.isCancelada());

            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();

            if (!resultSet.next()) {
                throw new SQLException(
                        "Error en la base de datos: no se ha podido obtener el ID de la encuesta creada.");
            }
            Long encuestaId = resultSet.getLong(1);

            encuesta.setEncuestaId(encuestaId);
            return encuesta;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Encuesta encuesta) throws InstanceNotFoundException {
        throw new UnsupportedOperationException("Operación no implementada todavía");
    }

    @Override
    public Encuesta find(Long encuestaId) throws InstanceNotFoundException {

        String queryString = "SELECT pregunta, fechaCreacion, fechaFin, "
                + "respuestasPositivas, respuestasNegativas, cancelada "
                + "FROM Encuesta WHERE encuestaId = ?";

        try (Connection connection = DataSourceLocator.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            preparedStatement.setLong(1, encuestaId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                // Si no hay resultado, lanzamos la excepción
                throw new InstanceNotFoundException(encuestaId,
                        Encuesta.class.getName());
            }

            // Obtenemos los datos de la fila
            String pregunta = resultSet.getString(1);
            LocalDateTime fechaCreacion = resultSet.getTimestamp(2).toLocalDateTime();
            LocalDateTime fechaFin = resultSet.getTimestamp(3).toLocalDateTime();
            long pos = resultSet.getLong(4);
            long neg = resultSet.getLong(5);
            boolean cancelada = resultSet.getBoolean(6);

            // Creamos y devolvemos el objeto Encuesta
            return new Encuesta(encuestaId, pregunta, fechaCreacion, fechaFin,
                    pos, neg, cancelada);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Encuesta> findByKeywords(String keywords, boolean soloNoFinalizadas) {
        throw new UnsupportedOperationException("Operación no implementada todavía");
    }
}