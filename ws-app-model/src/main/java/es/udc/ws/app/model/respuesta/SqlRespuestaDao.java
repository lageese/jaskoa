package es.udc.ws.app.model.respuesta;

import es.udc.ws.app.model.util.exceptions.InstanceNotFoundException;
import java.util.List;

public interface SqlRespuestaDao {

    public Respuesta create(Respuesta respuesta);

    public void update(Respuesta respuesta) throws InstanceNotFoundException;

    public Respuesta findByEmailAndEncuestaId(Long encuestaId, String emailEmpleado);

    public List<Respuesta> findByEncuestaId(Long encuestaId, boolean soloAfirmativas);
}