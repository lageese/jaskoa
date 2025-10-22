package es.udc.ws.app.model.surveyservice;

public class SurveyServiceFactory {

    private final static String CLASS_NAME_PARAMETER = "SurveyServiceFactory.className";
    private static SurveyService service = null;

    private SurveyServiceFactory() {
    }

    @SuppressWarnings("unchecked")
    private static SurveyService getInstance() {
        try {
            String serviceClassName = System.getProperty(CLASS_NAME_PARAMETER);
            if (serviceClassName == null) {
                serviceClassName = "es.udc.ws.app.model.surveyservice.SurveyServiceImpl";
            }
            Class<?> serviceClass = Class.forName(serviceClassName);
            service = (SurveyService) serviceClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return service;
    }

    public synchronized static SurveyService getService() {
        if (service == null) {
            service = getInstance();
        }
        return service;
    }
}