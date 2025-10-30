package es.udc.ws.app.model.surveyservice;

import es.udc.ws.app.model.encuesta.Encuesta;
import es.udc.ws.app.model.encuesta.EncuestaDao;
import es.udc.ws.app.model.encuesta.EncuestaDaoFactory;
import es.udc.ws.app.model.respuesta.Respuesta;
import es.udc.ws.app.model.surveyservice.exceptions.EncuestaCanceladaException;
import es.udc.ws.app.model.surveyservice.exceptions.EncuestaFinalizadaException;
import es.udc.ws.app.model.surveyservice.exceptions.FechaFinExpiradaException;
import es.udc.ws.util.exceptions.InputValidationException;
import es.udc.ws.util.exceptions.InstanceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

public class SurveyServiceImpl implements SurveyService {

    private EncuestaDao encuestaDao = null;

    public SurveyServiceImpl() {
        this.encuestaDao = EncuestaDaoFactory.getDao();
    }

    @Override
    public Encuesta crearEncuesta(Encuesta encuesta)
            throws InputValidationException, FechaFinExpiradaException {

        if (encuesta.getFechaFin().isBefore(LocalDateTime.now())) {
            throw new FechaFinExpiradaException(encuesta.getFechaFin());
        }

        encuesta.setFechaCreacion(LocalDateTime.now().withNano(0));
        encuesta.setRespuestasPositivas(0);
        encuesta.setRespuestasNegativas(0);
        encuesta.setCancelada(false);

        Encuesta encuestaGuardada = encuestaDao.create(encuesta);

        return encuestaGuardada;
    }

    // MÉTODOS RESTANTES (A IMPLEMENTAR EN FUTURAS FASES)

    @Override
    public List<Encuesta> buscarEncuestas(String palabraClave, boolean soloNoFinalizadas) {
        throw new UnsupportedOperationException("Operación no implementada todavía");
    }

    @Override
    public Encuesta buscarEncuestaPorId(Long encuestaId)
            throws InstanceNotFoundException {

        // La lógica es simple: llamar al DAO y devolver lo que encuentre
        return encuestaDao.find(encuestaId);
    }

    @Override
    public Respuesta responderEncuesta(Long encuestaId, String emailEmpleado, boolean afirmativa)
            throws InputValidationException, InstanceNotFoundException,
            EncuestaFinalizadaException, EncuestaCanceladaException {
        throw new UnsupportedOperationException("Operación no implementada todavía");
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