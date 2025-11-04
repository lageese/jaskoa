package es.udc.ws.app.model.encuesta;

public class EncuestaDaoFactory {

    private final static String CLASS_NAME_PARAMETER = "EncuestaDaoFactory.className";
    private static SqlEncuestaDao dao = null;

    private EncuestaDaoFactory() {
    }

    @SuppressWarnings("unchecked")
    private static SqlEncuestaDao getInstance() {
        try {
            String daoClassName = System.getProperty(CLASS_NAME_PARAMETER);

            if (daoClassName == null) {
                // Clase por defecto si no se define la propiedad del sistema
                daoClassName = "es.udc.ws.app.model.encuesta.Jdbc3CcSqlEncuestaDao";
            }

            Class<?> daoClass = Class.forName(daoClassName);
            dao = (SqlEncuestaDao) daoClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dao;
    }

    public synchronized static SqlEncuestaDao getDao() {
        if (dao == null) {
            dao = getInstance();
        }
        return dao;
    }
}
