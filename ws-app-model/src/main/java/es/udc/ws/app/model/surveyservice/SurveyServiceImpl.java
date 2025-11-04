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
import es.udc.ws.util.exceptions.InputValidationException;
import es.udc.ws.util.exceptions.InstanceNotFoundException;




import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class SurveyServiceImpl implements SurveyService {

    private EncuestaDao encuestaDao = null;
    private RespuestaDao respuestaDao = null;

    public SurveyServiceImpl() {
        this.encuestaDao = EncuestaDaoFactory.getDao();
        this.respuestaDao = RespuestaDaoFactory.getDao();
    }

    @Override
    public Encuesta crearEncuesta(Encuesta encuesta)
            throws InputValidationException, FechaFinExpiradaException {

        if (encuesta == null) {
            throw new InputValidationException("La encuesta no puede ser nula");
        }

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


    @Override
    public List<Encuesta> buscarEncuestas(String palabraClave, boolean soloNoFinalizadas) {

        // Validamos la entrada (la práctica dice que la palabra clave no puede ser nula)
        Objects.requireNonNull(palabraClave, "La palabra clave no puede ser nula");

        return encuestaDao.findByKeywords(palabraClave, soloNoFinalizadas);
    }


    @Override
    public Encuesta buscarEncuestaPorId(Long encuestaId)
            throws InstanceNotFoundException {

        // La lógica es simple: llamar al DAO y devolver lo que encuentre
        return encuestaDao.find(encuestaId); // <--- Esta es tu línea 65
    }


    @Override
    public Respuesta responderEncuesta(Long encuestaId, String emailEmpleado, boolean afirmativa)
            throws InputValidationException, InstanceNotFoundException,
            EncuestaFinalizadaException, EncuestaCanceladaException {

        // 1. Validar email (simple)
        if (emailEmpleado == null || emailEmpleado.trim().isEmpty()) {
            throw new InputValidationException("El email del empleado no puede ser nulo o vacío");
        }

        Encuesta encuesta = encuestaDao.find(encuestaId);

        // 3. Validar estado de la encuesta (requisitos de [FUNC-4])
        if (encuesta.isCancelada()) {
            throw new EncuestaCanceladaException(encuestaId);
        }
        if (encuesta.getFechaFin().isBefore(LocalDateTime.now())) {
            throw new EncuestaFinalizadaException(encuestaId, encuesta.getFechaFin());
        }

        // 4. Lógica de "Crear" o "Actualizar" respuesta
        // Buscamos si el empleado ya ha respondido
        Respuesta respuestaExistente = respuestaDao.findByEmailAndEncuestaId(encuestaId, emailEmpleado);

        LocalDateTime ahora = LocalDateTime.now().withNano(0);

        if (respuestaExistente == null) {
            // 4.A. CASO NUEVO: El empleado responde por primera vez

            // Creamos la nueva respuesta
            Respuesta nuevaRespuesta = new Respuesta(encuestaId, emailEmpleado, afirmativa);
            nuevaRespuesta.setFechaRespuesta(ahora);

            // Guardamos la respuesta en la BD
            Respuesta respuestaGuardada = respuestaDao.create(nuevaRespuesta);

            // Actualizamos el contador de la encuesta
            if (afirmativa) {
                encuesta.setRespuestasPositivas(encuesta.getRespuestasPositivas() + 1);
            } else {
                encuesta.setRespuestasNegativas(encuesta.getRespuestasNegativas() + 1);
            }
            encuestaDao.update(encuesta); // Guardamos la encuesta actualizada (¡requiere Tarea 2 de Angie!)

            return respuestaGuardada;

        } else {

            boolean respuestaAntigua = respuestaExistente.isAfirmativa();

            // Actualizamos el objeto respuesta
            respuestaExistente.setAfirmativa(afirmativa);
            respuestaExistente.setFechaRespuesta(ahora);

            // Guardamos la respuesta actualizada en la BD
            respuestaDao.update(respuestaExistente);

            // Actualizamos contadores SI la respuesta ha cambiado
            if (respuestaAntigua != afirmativa) {
                if (afirmativa) {
                    // Voto cambió a SÍ: +1 pos, -1 neg
                    encuesta.setRespuestasPositivas(encuesta.getRespuestasPositivas() + 1);
                    encuesta.setRespuestasNegativas(encuesta.getRespuestasNegativas() - 1);
                } else {
                    // Voto cambió a NO: -1 pos, +1 neg
                    encuesta.setRespuestasPositivas(encuesta.getRespuestasPositivas() - 1);
                    encuesta.setRespuestasNegativas(encuesta.getRespuestasNegativas() + 1);
                }
                encuestaDao.update(encuesta); // Guardamos la encuesta actualizada (¡requiere Tarea 2 de Angie!)
            }

            return respuestaExistente;
        }
    }
    @Override
    public Encuesta cancelarEncuesta(Long encuestaId)
            throws InstanceNotFoundException, EncuestaFinalizadaException,
            EncuestaCanceladaException {

        // 1. Validar la encuesta (reutilizamos [FUNC-3])
        // Esto lanza InstanceNotFoundException si no existe
        Encuesta encuesta = encuestaDao.find(encuestaId);

        // 2. Validar estado: no se puede cancelar si ya ha finalizado
        // (requisito de [FUNC-5])
        if (encuesta.getFechaFin().isBefore(LocalDateTime.now())) {
            throw new EncuestaFinalizadaException(encuestaId, encuesta.getFechaFin());
        }

        // 3. Validar estado: no se puede cancelar si ya está cancelada
        // (requisito de [FUNC-5])
        if (encuesta.isCancelada()) {
            throw new EncuestaCanceladaException(encuestaId);
        }

        // 4. Realizar la cancelación
        encuesta.setCancelada(true);

        // 5. Guardar el cambio en la BD (reutilizamos el 'update' de [FUNC-4])
        encuestaDao.update(encuesta);

        // 6. Devolver la encuesta actualizada
        return encuesta;
    }


    @Override
    public List<Respuesta> obtenerRespuestas(Long encuestaId, boolean soloAfirmativas)
            throws InstanceNotFoundException, EncuestaCanceladaException {

        Encuesta encuesta = encuestaDao.find(encuestaId);


        if (encuesta.isCancelada()) {
            throw new EncuestaCanceladaException(encuestaId);
        }

        return respuestaDao.findByEncuestaId(encuestaId, soloAfirmativas);
    }
}