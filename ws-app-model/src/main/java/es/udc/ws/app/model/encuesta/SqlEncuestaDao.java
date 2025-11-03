package es.udc.ws.app.model.encuesta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import es.udc.ws.app.model.util.exceptions.InstanceNotFoundException;
import es.udc.ws.util.sql.DataSourceLocator;

// NOMBRE DE LA CLASE CORREGIDO: SqlEncuestaDao en lugar de JdbcEncuestaDao
public class SqlEncuestaDao implements EncuestaDao {

    // Este campo no se usaba en tu versión, lo quitamos para más claridad
    // private DataSource dataSource;

    @Override
    public Encuesta create(Encuesta encuesta) {

        String query = "INSERT INTO Encuesta (pregunta, fechaFin, fechaCreacion, " +
                "respuestasPositivas, respuestasNegativas, cancelada) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        // El 'try-with-resources' ya cierra la conexión automáticamente
        try (Connection connection = DataSourceLocator.getDataSource("jdbc/app").getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query, Statement.RETURN_GENERATED_KEYS)) {

            // Asignar parámetros
            preparedStatement.setString(1, encuesta.getPregunta());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(encuesta.getFechaFin()));
            preparedStatement.setTimestamp(3, Timestamp.valueOf(encuesta.getFechaCreacion()));
            preparedStatement.setInt(4, (int) encuesta.getRespuestasPositivas());
            preparedStatement.setInt(5, (int) encuesta.getRespuestasNegativas());
            preparedStatement.setBoolean(6, encuesta.isCancelada());

            // Ejecutar la inserción
            preparedStatement.executeUpdate();

            // Obtener el ID generado
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (!rs.next()) {
                throw new SQLException("No se pudo obtener el ID generado para la encuesta");
            }
            Long encuestaId = rs.getLong(1);

            // Devolver la encuesta actualizada con su ID
            encuesta.setEncuestaId(encuestaId);
            return encuesta;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Encuesta find(Long encuestaId) throws InstanceNotFoundException {

        String query = "SELECT pregunta, fechaFin, fechaCreacion, " +
                "respuestasPositivas, respuestasNegativas, cancelada " +
                "FROM Encuesta WHERE encuestaId = ?";

        try (Connection connection = DataSourceLocator.getDataSource("jdbc/app").getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, encuestaId);

            ResultSet rs = preparedStatement.executeQuery();

            if (!rs.next()) {
                throw new InstanceNotFoundException(encuestaId, Encuesta.class.getName());
            }

            // Recuperar datos del ResultSet
            String pregunta = rs.getString("pregunta");
            LocalDateTime fechaFin = rs.getTimestamp("fechaFin").toLocalDateTime();
            LocalDateTime fechaCreacion = rs.getTimestamp("fechaCreacion").toLocalDateTime();
            int respuestasPositivas = rs.getInt("respuestasPositivas");
            int respuestasNegativas = rs.getInt("respuestasNegativas");
            boolean cancelada = rs.getBoolean("cancelada");

            // Crear y devolver el objeto Encuesta
            return new Encuesta(encuestaId, pregunta, fechaFin, fechaCreacion,
                    respuestasPositivas, respuestasNegativas, cancelada);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Encuesta> findByKeyword(String keyword, boolean soloNoFinalizadas) {
        
        // Versión simplificada de la query base
        String baseQuery = "SELECT encuestaId, pregunta, fechaFin, fechaCreacion, " +
                "respuestasPositivas, respuestasNegativas, cancelada " +
                "FROM Encuesta WHERE pregunta LIKE ?";
        
        // Añadir condiciones dinámicas
        if (soloNoFinalizadas) {
            baseQuery += " AND fechaFin > ?";
        }
        
        baseQuery += " ORDER BY fechaCreacion DESC";

        List<Encuesta> encuestas = new ArrayList<>();

        try (Connection connection = DataSourceLocator.getDataSource("jdbc/app").getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(baseQuery)) {

            preparedStatement.setString(1, "%" + keyword + "%");
            if (soloNoFinalizadas) {
                preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            }

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                // Recuperar datos del ResultSet
                Long encuestaId = rs.getLong("encuestaId");
                String pregunta = rs.getString("pregunta");
                LocalDateTime fechaFin = rs.getTimestamp("fechaFin").toLocalDateTime();
                LocalDateTime fechaCreacion = rs.getTimestamp("fechaCreacion").toLocalDateTime();
                int respuestasPositivas = rs.getInt("respuestasPositivas");
                int respuestasNegativas = rs.getInt("respuestasNegativas");
                boolean cancelada = rs.getBoolean("cancelada");

                // Añadir a la lista
                encuestas.add(new Encuesta(encuestaId, pregunta, fechaFin, fechaCreacion,
                        respuestasPositivas, respuestasNegativas, cancelada));
            }
            
            return encuestas;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Encuesta> findByKeywords(String keyword, boolean soloNoFinalizadas) {
        // Delegar a la implementación existente para evitar duplicación
        return findByKeyword(keyword, soloNoFinalizadas);
    }

    @Override
    public void update(Encuesta encuesta) throws InstanceNotFoundException {

        String query = "UPDATE Encuesta SET pregunta = ?, fechaFin = ?, " +
                "respuestasPositivas = ?, respuestasNegativas = ?, cancelada = ? " +
                "WHERE encuestaId = ?";

        try (Connection connection = DataSourceLocator.getDataSource("jdbc/app").getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Asignar parámetros
            preparedStatement.setString(1, encuesta.getPregunta());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(encuesta.getFechaFin()));
            preparedStatement.setInt(3, (int) encuesta.getRespuestasPositivas());
            preparedStatement.setInt(4, (int) encuesta.getRespuestasNegativas());
            preparedStatement.setBoolean(5, encuesta.isCancelada());
            preparedStatement.setLong(6, encuesta.getEncuestaId());

            // Ejecutar la actualización
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new InstanceNotFoundException(encuesta.getEncuestaId(), Encuesta.class.getName());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void remove(Long encuestaId) throws InstanceNotFoundException {

        String query = "DELETE FROM Encuesta WHERE encuestaId = ?";

        try (Connection connection = DataSourceLocator.getDataSource("jdbc/app").getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, encuestaId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new InstanceNotFoundException(encuestaId, Encuesta.class.getName());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}