package es.udc.ws.app.test.model.appservice;

// Imports de la práctica
import es.udc.ws.app.model.encuesta.Encuesta;
import es.udc.ws.app.model.surveyservice.SurveyService;
import es.udc.ws.app.model.surveyservice.SurveyServiceFactory;
import es.udc.ws.app.model.surveyservice.exceptions.FechaFinExpiradaException;
import es.udc.ws.util.exceptions.InputValidationException;
import es.udc.ws.util.exceptions.InstanceNotFoundException;
import es.udc.ws.util.jdbc.DataSourceLocator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AppServiceTest {

    private static SurveyService surveyService = null;

    @BeforeAll
    public static void init() {
        surveyService = SurveyServiceFactory.getService();

        DataSourceLocator.init("SimpleDataSource.properties");
    }


    private void clearTables() {
        try (Connection connection = DataSourceLocator.getConnection()) {
            connection.setAutoCommit(true);
            // Borramos primero Respuesta por la clave foránea
            connection.createStatement().executeUpdate("DELETE FROM Respuesta");
            connection.createStatement().executeUpdate("DELETE FROM Encuesta");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    @Test
    public void testCrearEncuestaBasico()
            throws InputValidationException, FechaFinExpiradaException, InstanceNotFoundException {


        clearTables();


        String pregunta = "Pregunta de prueba básica";
        LocalDateTime fechaFin = LocalDateTime.now().plusDays(10).withNano(0);


        Encuesta encuestaCreada = surveyService.crearEncuesta(
                new Encuesta(pregunta, fechaFin));

        Encuesta encuestaDeBD = surveyService.buscarEncuestaPorId(encuestaCreada.getEncuestaId());


        assertEquals(encuestaCreada, encuestaDeBD);


        assertNotNull(encuestaDeBD.getEncuestaId());
        assertEquals(pregunta, encuestaDeBD.getPregunta());
        assertEquals(fechaFin, encuestaDeBD.getFechaFin());
        assertEquals(0, encuestaDeBD.getRespuestasPositivas());
        assertEquals(0, encuestaDeBD.getRespuestasNegativas());
        assertFalse(encuestaDeBD.isCancelada());
        assertNotNull(encuestaDeBD.getFechaCreacion());
    }

    // Aquí se añadirán el resto de tests (el de fecha expirada,
    // el de buscar por ID, etc.)

}

