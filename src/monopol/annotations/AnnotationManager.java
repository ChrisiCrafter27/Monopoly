package monopol.annotations;

import monopol.utils.AccessManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AnnotationManager {
    private static AnnotationManager instance;

    private AnnotationManager() {
        //Autostart
        try {
            for(Method method : AccessManager.getAllMethods()) {
                if(method.isAnnotationPresent(Autostart.class) && Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 0) method.invoke(null);
            }
        } catch (IOException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setup() {
        if(instance == null) instance = new AnnotationManager();
    }
}
