package es.udc.ws.app.model.surveyservice;

import es.udc.ws.app.model.encuesta.Encuesta;
import es.udc.ws.app.model.respuesta.Respuesta;
import es.udc.ws.app.model.surveyservice.exceptions.EncuestaCanceladaException;
import es.udc.ws.app.model.surveyservice.exceptions.EncuestaFinalizadaException;
import es.udc.ws.app.model.surveyservice.exceptions.FechaFinExpiradaException;
import es.udc.ws.app.model.util.exceptions.InputValidationException;
import es.udc.ws.app.model.util.exceptions.InstanceNotFoundException;

import java.util.List;

public interface SurveyService {

    public Encuesta crearEncuesta(Encuesta encuesta)
            throws InputValidationException, FechaFinExpiradaException;


    public List<Encuesta> buscarEncuestas(String palabraClave, boolean soloNoFinalizadas);

    public Encuesta buscarEncuestaPorId(Long encuestaId)
            throws InstanceNotFoundException;

    public Respuesta responderEncuesta(Long encuestaId, String emailEmpleado, boolean afirmativa)
            throws InputValidationException, InstanceNotFoundException,
            EncuestaFinalizadaException, EncuestaCanceladaException;

    public Encuesta cancelarEncuesta(Long encuestaId)
            throws InstanceNotFoundException, EncuestaFinalizadaException,
            EncuestaCanceladaException;

    public List<Respuesta> obtenerRespuestas(Long encuestaId, boolean soloAfirmativas)
            throws InstanceNotFoundException, EncuestaCanceladaException;
}