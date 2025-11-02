package es.udc.ws.app.model.encuesta;

public class EncuestaDaoFactory {
    private final static String CLASS_NAME_PARAMETER = "EncuestaDaoFactory.className";
    private static EncuestaDao dao = null;

    private EncuestaDaoFactory() {
    }

    @SuppressWarnings("unchecked")
    private static EncuestaDao getInstance() {
        try {
            String daoClassName = System.getProperty(CLASS_NAME_PARAMETER);
            if (daoClassName == null) {
                // Nombre por defecto de la implementación SQL del DAO
                daoClassName = "es.udc.ws.app.model.encuesta.SqlEncuestaDao";
            }
            Class<?> daoClass = Class.forName(daoClassName);
            dao = (EncuestaDao) daoClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dao;
    }

    public synchronized static EncuestaDao getDao() {
        if (dao == null) {
            dao = getInstance();
        }
        return dao;
    }
}
