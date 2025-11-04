package es.udc.ws.app.model.respuesta;

import java.util.List;

import es.udc.ws.util.exceptions.InstanceNotFoundException;

public interface SqlRespuestaDao {

    public Respuesta create(Respuesta respuesta);

    public void update(Respuesta respuesta) throws InstanceNotFoundException;

    public Respuesta findByEmailAndEncuestaId(Long encuestaId, String emailEmpleado);

    public List<Respuesta> findByEncuestaId(Long encuestaId, boolean soloAfirmativas);
}