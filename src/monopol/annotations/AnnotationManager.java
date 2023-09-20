package monopol.annotations;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class AnnotationManager {
    private static final AnnotationManager instance = new AnnotationManager();

    private AnnotationManager() {
        //LogOnExecution

    }

    public static List<String> getClassesInPackage(String packageName) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<String> classNames = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                File directory = new File(resource.getFile());
                if (directory.exists()) {
                    String[] files = directory.list();
                    if (files != null) {
                        for (String file : files) {
                            if (file.endsWith(".class")) {
                                String className = packageName + '.' + file.substring(0, file.length() - 6);
                                classNames.add(className);
                            }
                        }
                    }
                }
            }
        }

        List<Class<?>> classes = new ArrayList<>();
        for (String className : classNames) {
            Class<?> clazz = Class.forName(className);
            classes.add(clazz);
        }

        List<String> classNamesStr = new ArrayList<>();
        for (Class<?> clazz : classes) {
            classNamesStr.add(clazz.getName());
        }

        return classNamesStr;
    }

    public static void main(String[] args) {
        try {
            String packageName = "monopol"; // Ersetze durch das gewünschte Paket
            List<String> classNames = getClassesInPackage(packageName);

            System.out.println("Klassen im Paket " + packageName + ":");
            for (String className : classNames) {
                System.out.println(className);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }
}
