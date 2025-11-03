package es.udc.ws.app.model.respuesta;

// Se importa la interfaz del DAO que esta factoría va a instanciar
import es.udc.ws.app.model.respuesta.RespuestaDao;

public class RespuestaDaoFactory {
    // El nombre del parámetro de configuración cambia para ser específico de esta factoría
    private final static String CLASS_NAME_PARAMETER = "RespuestaDaoFactory.className";
    private static RespuestaDao dao = null;

    private RespuestaDaoFactory() {
    }

    @SuppressWarnings("unchecked")
    private static RespuestaDao getInstance() {
        try {
            String daoClassName = System.getProperty(CLASS_NAME_PARAMETER);
            if (daoClassName == null) {
                // Nombre por defecto de la implementación SQL del DAO de Respuesta
                // Sigue el mismo patrón que EncuestaDaoFactory
                daoClassName = "es.udc.ws.app.model.respuesta.SqlRespuestaDao";
            }
            Class<?> daoClass = Class.forName(daoClassName);
            dao = (RespuestaDao) daoClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dao;
    }

    public synchronized static RespuestaDao getDao() {
        if (dao == null) {
            dao = getInstance();
        }
        return dao;
    }
}