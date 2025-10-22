package es.udc.ws.app.model.util.exceptions;

public class InstanceNotFoundException extends Exception {
    // ... (pegar el mismo código que te di antes)
    private Object key;
    private String className;

    public InstanceNotFoundException(Object key, String className) {
        super(className + " instance with key = " + key + " not found");
        this.key = key;
        this.className = className;
    }

    public Object getKey() { return key; }
    public String getClassName() { return className; }
}