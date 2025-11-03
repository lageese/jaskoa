package es.udc.ws.app.model.surveyservice;

import es.udc.ws.app.model.encuesta.Encuesta;
import es.udc.ws.app.model.encuesta.EncuestaDao;
import es.udc.ws.app.model.encuesta.EncuestaDaoFactory;
import es.udc.ws.app.model.respuesta.Respuesta;
// Imports para RespuestaDao
import es.udc.ws.app.model.respuesta.RespuestaDao;
import es.udc.ws.app.model.respuesta.RespuestaDaoFactory;
//
import es.udc.ws.app.model.surveyservice.exceptions.EncuestaCanceladaException;
import es.udc.ws.app.model.surveyservice.exceptions.EncuestaFinalizadaException;
import es.udc.ws.app.model.surveyservice.exceptions.FechaFinExpiradaException;// Asegúrate de que esta línea existe
import es.udc.ws.app.model.util.exceptions.InputValidationException;
import es.udc.ws.app.model.util.exceptions.InstanceNotFoundException;



import java.time.LocalDateTime;
import java.util.List;

public class SurveyServiceImpl implements SurveyService {

    private EncuestaDao encuestaDao = null;
    // Declaración del DAO de Respuesta
    private RespuestaDao respuestaDao = null;

    public SurveyServiceImpl() {
        this.encuestaDao = EncuestaDaoFactory.getDao();
        // Inicialización del DAO de Respuesta
        this.respuestaDao = RespuestaDaoFactory.getDao();
    }

    @Override
    public Encuesta crearEncuesta(Encuesta encuesta)
            throws InputValidationException, FechaFinExpiradaException {

        //Validar que la encuesta no sea nula
        if (encuesta == null) {
            throw new InputValidationException("La encuesta no puede ser nula");
        }

        //Validar que la pregunta no esté vacía o nula
        if (encuesta.getPregunta() == null || encuesta.getPregunta().trim().isEmpty()) {
            throw new InputValidationException("La pregunta de la encuesta no puede estar vacía");
        }

        //Validar que la fecha de fin exista
        if (encuesta.getFechaFin() == null) {
            throw new InputValidationException("La fecha de fin no puede ser nula");
        }

        //Validar que la fecha de fin no esté expirada
        if (encuesta.getFechaFin().isBefore(LocalDateTime.now())) {
            throw new FechaFinExpiradaException(encuesta.getFechaFin());
        }

        //Inicializar campos automáticos
        encuesta.setFechaCreacion(LocalDateTime.now().withNano(0));
        encuesta.setRespuestasPositivas(0);
        encuesta.setRespuestasNegativas(0);
        encuesta.setCancelada(false);

        //Guardar en la base de datos
        return encuestaDao.create(encuesta);
    }

    // MÉTODOS RESTANTES (A IMPLEMENTAR EN FUTURAS FASES)

    @Override
    public List<Encuesta> buscarEncuestas(String palabraClave, boolean soloNoFinalizadas) {
        throw new UnsupportedOperationException("Operación no implementada todavía");
    }

    // ==================================================================
    // CORRECCIÓN 1: Añadir "throws InstanceNotFoundException"
    // ==================================================================
    @Override
    public Encuesta buscarEncuestaPorId(Long encuestaId)
            throws InstanceNotFoundException { // <--- ESTO ES LO QUE FALTA

        // La lógica es simple: llamar al DAO y devolver lo que encuentre
        return encuestaDao.find(encuestaId); // <--- Esta es tu línea 65
    }

    // ==================================================================
    // CORRECCIÓN 2: Añadir "InstanceNotFoundException" a la lista de throws
    // ==================================================================
    @Override
    public Respuesta responderEncuesta(Long encuestaId, String emailEmpleado, boolean afirmativa)
            throws InputValidationException, InstanceNotFoundException, // <--- ESTO ES LO QUE FALTA
            EncuestaFinalizadaException, EncuestaCanceladaException {

        //Buscar la encuesta
        Encuesta encuesta = encuestaDao.find(encuestaId); // <--- Esta es tu línea 77

        //Validar que no esté cancelada
        if (encuesta.isCancelada()) {
            throw new EncuestaCanceladaException(encuestaId);
        }

        //Validar que no esté finalizada
        if (encuesta.getFechaFin().isBefore(LocalDateTime.now())) {
            throw new EncuestaFinalizadaException(encuestaId, encuesta.getFechaFin());
        }

        //Crear respuesta
        Respuesta respuesta = new Respuesta(encuestaId, emailEmpleado, afirmativa);

        // Actualizar contadores de la encuesta
        if (afirmativa) {
            encuesta.setRespuestasPositivas(encuesta.getRespuestasPositivas() + 1);
        } else {
            encuesta.setRespuestasNegativas(encuesta.getRespuestasNegativas() + 1);
        }

        //Guardar cambios
        encuestaDao.update(encuesta); // <--- Esta es tu línea 100
        respuestaDao.create(respuesta);

        //Devolver la respuesta creada
        return respuesta;
    }

    @Override
    public Encuesta cancelarEncuesta(Long encuestaId)
            throws InstanceNotFoundException, EncuestaFinalizadaException,
            EncuestaCanceladaException {
        throw new UnsupportedOperationException("Operación no implementada todavía");
    }

    @Override
    public List<Respuesta> obtenerRespuestas(Long encuestaId, boolean soloAfirmativas)
            throws InstanceNotFoundException, EncuestaCanceladaException {
        throw new UnsupportedOperationException("Operación no implementada todavía");
    }
}