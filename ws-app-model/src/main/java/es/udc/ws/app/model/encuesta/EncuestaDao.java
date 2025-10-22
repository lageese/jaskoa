package es.udc.ws.app.model.encuesta;

import es.udc.ws.app.model.util.exceptions.InstanceNotFoundException;
import java.util.List;

public interface EncuestaDao {

    public Encuesta create(Encuesta encuesta);

    public void update(Encuesta encuesta) throws InstanceNotFoundException;

    public Encuesta find(Long encuestaId) throws InstanceNotFoundException;

    public List<Encuesta> findByKeywords(String keywords, boolean soloNoFinalizadas);
}