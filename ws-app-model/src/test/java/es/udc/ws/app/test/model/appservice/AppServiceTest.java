package es.udc.ws.app.test.model.appservice;

// Imports de la práctica
import es.udc.ws.app.model.encuesta.Encuesta;
import es.udc.ws.app.model.respuesta.Respuesta;
import es.udc.ws.app.model.surveyservice.SurveyService;
import es.udc.ws.app.model.surveyservice.SurveyServiceFactory;
import es.udc.ws.app.model.surveyservice.exceptions.FechaFinExpiradaException;
import es.udc.ws.app.model.surveyservice.exceptions.EncuestaCanceladaException;
import es.udc.ws.app.model.surveyservice.exceptions.EncuestaFinalizadaException;
import es.udc.ws.app.model.util.exceptions.InputValidationException;
import es.udc.ws.app.model.util.exceptions.InstanceNotFoundException;
import es.udc.ws.util.jdbc.DataSourceLocator;//ESTA RUTA ESTA MAL

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
    @Test
    public void testResponderEncuestaCancelada()
            throws FechaFinExpiradaException, InputValidationException, InstanceNotFoundException,
            EncuestaFinalizadaException, EncuestaCanceladaException {

        Encuesta encuesta = new Encuesta("¿Trabajas remoto?", LocalDateTime.now().plusDays(3));
        Encuesta creada = surveyService.crearEncuesta(encuesta);

        // Cancelar encuesta
        surveyService.cancelarEncuesta(creada.getEncuestaId());

        // Intentar responder
        assertThrows(EncuestaCanceladaException.class, () -> {
            surveyService.responderEncuesta(creada.getEncuestaId(), "empleado@empresa.com", true);
        });
    }

    @Test
    public void testResponderEncuestaFinalizada()
            throws FechaFinExpiradaException, InputValidationException, InstanceNotFoundException {

        Encuesta encuesta = new Encuesta("¿Usas redes sociales?", LocalDateTime.now().minusDays(1));
        // No se puede crear con fecha fin pasada → depende de implementación, podría lanzar excepción
        // Pero si se permite crearla para test, simula finalizada
        assertThrows(FechaFinExpiradaException.class, () -> {
            surveyService.crearEncuesta(encuesta);
        });
    }

    @Test
    public void testResponderEncuestaInexistente() {
        assertThrows(InstanceNotFoundException.class, () -> {
            surveyService.responderEncuesta(-99L, "empleado@empresa.com", true);
        });
    }

    @Test
    public void testResponderEncuestaNegativa()
            throws FechaFinExpiradaException, InputValidationException, InstanceNotFoundException,
            EncuestaFinalizadaException, EncuestaCanceladaException {

        Encuesta encuesta = new Encuesta("¿Te gusta el chocolate?", LocalDateTime.now().plusDays(2));
        Encuesta creada = surveyService.crearEncuesta(encuesta);

        Respuesta respuesta = surveyService.responderEncuesta(creada.getEncuestaId(), "empleado2@empresa.com", false);

        assertNotNull(respuesta);
        assertFalse(respuesta.isAfirmativa());
    }

    @Test
    public void testResponderEncuestaAfirmativa()
            throws FechaFinExpiradaException, InputValidationException, InstanceNotFoundException,
            EncuestaFinalizadaException, EncuestaCanceladaException {

        Encuesta encuesta = new Encuesta("¿Te gusta el té?", LocalDateTime.now().plusDays(2));
        Encuesta creada = surveyService.crearEncuesta(encuesta);

        Respuesta respuesta = surveyService.responderEncuesta(creada.getEncuestaId(), "empleado@empresa.com", true);

        assertNotNull(respuesta);
        assertEquals(creada.getEncuestaId(), respuesta.getEncuestaId());
        assertTrue(respuesta.isAfirmativa());
    }

    @Test
    public void testBuscarEncuesta() throws  FechaFinExpiradaException, InstanceNotFoundException {

        //Crear una encuesta para probar
        Encuesta encuesta = new Encuesta("¿Te gusta el café?", LocalDateTime.now().plusDays(5));
        Encuesta creada = surveyService.crearEncuesta(encuesta);

        //Buscar la encuesta por ID
        Encuesta encontrada = surveyService.buscarEncuestaPorId(creada.getEncuestaId());
        assertNotNull(encontrada);
        assertEquals(creada.getEncuestaId(), encontrada.getEncuestaId());
        assertEquals(creada.getPregunta(), encontrada.getPregunta());

        //Probar que lanza excepción si no existe
        Long idInexistente = -1L;
        assertThrows(InstanceNotFoundException.class, () -> {
            surveyService.buscarEncuestaPorId(idInexistente);
        });
    }

}

