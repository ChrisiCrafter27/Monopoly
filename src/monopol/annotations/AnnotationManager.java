package monopol.annotations;

import monopol.utils.AccessManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AnnotationManager {
    private static final AnnotationManager instance = new AnnotationManager();

    private AnnotationManager() {
        //Autostart
        try {
            for(Method method : AccessManager.getAllMethods()) {
                if(method.isAnnotationPresent(Autostart.class)) method.invoke(null, (Object) null);
            }
        } catch (IOException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Autostart
    public static void sayHi() {
        System.out.println("hi");
    }
}
