package monopol.utils;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ProjectStructure {

    public static boolean isAnnotated(Method method, Class<? extends Annotation> annotationClass) {
        if(method.isAnnotationPresent(annotationClass)) return true;
        while (method.getDeclaringClass().getSuperclass() != null) {
            try {
                method = method.getDeclaringClass().getSuperclass().getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                return false;
            }
            if(method.isAnnotationPresent(annotationClass)) return true;
        }
        return false;
    }

    public static ArrayList<Class> getClassesInPackage(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        ArrayList<Class> list = reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .distinct().collect(Collectors.toCollection(ArrayList::new));
        list.removeIf((clazz) -> clazz.getName().contains("$"));
        return list;
    }

    private static Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    public static ArrayList<String> getSubpackages(String basePackage) throws IOException {
        ArrayList<String> subpackages = new ArrayList<>();

        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);

        for (String classpathEntry : classpathEntries) {
            File entryFile = new File(classpathEntry);
            if (entryFile.isDirectory()) {
                String basePackagePath = basePackage.replace('.', File.separatorChar);
                File packageDirectory = new File(entryFile, basePackagePath);

                if (packageDirectory.exists() && packageDirectory.isDirectory()) {
                    File[] subpackageDirectories = packageDirectory.listFiles(File::isDirectory);
                    if (subpackageDirectories != null) {
                        for (File subpackageDirectory : subpackageDirectories) {
                            String subpackage = basePackage + "." + subpackageDirectory.getName();
                            subpackages.add(subpackage);
                        }
                    }
                }
            }
        }

        return subpackages;
    }

    public static ArrayList<Method> getMethodsOfClass(Class clazz) {
        ArrayList<Method> list = new ArrayList<>(Arrays.stream(clazz.getMethods()).toList());
        list.removeIf((method) -> {
            return !method.getDeclaringClass().getName().equals(clazz.getName());
        });
        return list;
    }

    public static ArrayList<Field> getFieldsOfClass(Class clazz) {
        ArrayList<Field> list = new ArrayList<>(Arrays.stream(clazz.getFields()).toList());
        list.removeIf((field) -> {
            return !field.getDeclaringClass().getName().equals(clazz.getName());
        });
        return list;
    }

    public static ArrayList<String> getAllPackages() throws IOException {
        ArrayList<String> list = new ArrayList<>();
        list.add("monopol");
        list.addAll(getSubpackages("monopol"));
        return list;
    }

    public static ArrayList<Class> getAllClasses() throws IOException {
        ArrayList<Class> list = new ArrayList<>();
        for(String packageName : getAllPackages()) {
            list.addAll(getClassesInPackage(packageName));
        }
        return list;
    }

    public static ArrayList<Method> getAllMethods() throws IOException {
        ArrayList<Method> list = new ArrayList<>();
        for(Class clazz : getAllClasses()) {
            list.addAll(getMethodsOfClass(clazz));
        }
        return list;
    }

    public static ArrayList<Field> getAllFields() throws IOException {
        ArrayList<Field> list = new ArrayList<>();
        for(Class clazz : getAllClasses()) {
            list.addAll(getFieldsOfClass(clazz));
        }
        return list;
    }

    public static void printProjectStructure() {
        try {
            for(String packageName : getAllPackages()) {
                System.out.println("Package detected: " + packageName);
            }
            for(Class clazz : getAllClasses()) {
                System.out.println("Class detected: " + clazz.getName());
            }
            for(Method method : getAllMethods()) {
                String parameterString = "";
                for(Parameter parameter : method.getParameters()) {
                    parameterString += ", ";
                    parameterString += parameter.getType().getSimpleName();
                    parameterString += " ";
                    parameterString += parameter.getName();
                }
                if(parameterString.length() >= 2) parameterString = parameterString.substring(2);
                System.out.println("Method detected: " + method.getDeclaringClass().getName() + "." + method.getName() + "(" + parameterString + ")");
            }
            for(Field field : getAllFields()) {
                System.out.println("Field detected: " + field.getType().getName() + " " + field.getDeclaringClass().getName() + ":" + field.getName());
            }
        } catch (IOException e) {
            System.out.println("Failed to print project-structure: " + Arrays.toString(e.getStackTrace()));
        }
    }

    public static void printProjectStructureAsTree(boolean detailed) {
        System.out.println("*********************************************************************************************************************************************************************************************************************************");
        System.out.println("Indexing project...");
        System.out.println("*********************************************************************************************************************************************************************************************************************************");
        if(detailed) {
            try {
                for (String packageName : getAllPackages()) {
                    System.out.println("Package detected: " + packageName);
                    for (Class clazz : getClassesInPackage(packageName)) {
                        System.out.println("    Class detected: " + clazz.getName());
                        for (Field field : getFieldsOfClass(clazz)) {
                            System.out.println("        Field detected: " + field.getType().getName() + " " + clazz.getName() + "." + field.getName());
                        }
                        for (Method method : getMethodsOfClass(clazz)) {
                            String parameterString = "";
                            for (Parameter parameter : method.getParameters()) {
                                parameterString += ", ";
                                parameterString += parameter.getType().getSimpleName();
                                parameterString += " ";
                                parameterString += parameter.getName();
                            }
                            if (parameterString.length() >= 2) parameterString = parameterString.substring(2);
                            System.out.println("        Method detected: " + method.getDeclaringClass().getName() + "." + method.getName() + "(" + parameterString + ")");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Failed to print project-structure: " + Arrays.toString(e.getStackTrace()));
            }
        } else {
            try {
                for (String packageName : getAllPackages()) {
                    System.out.println("Package detected: " + packageName);
                    for (Class clazz : getClassesInPackage(packageName)) {
                        System.out.println("    Class detected: " + clazz.getSimpleName());
                        for (Field field : getFieldsOfClass(clazz)) {
                            System.out.println("        Field detected: " + field.getType().getSimpleName() + " " + field.getName());
                        }
                        for (Method method : getMethodsOfClass(clazz)) {
                            String parameterString = "";
                            for (Parameter parameter : method.getParameters()) {
                                parameterString += ", ";
                                parameterString += parameter.getType().getSimpleName();
                                parameterString += " ";
                                parameterString += parameter.getName();
                            }
                            if (parameterString.length() >= 2) parameterString = parameterString.substring(2);
                            System.out.println("        Method detected: " + method.getName() + "(" + parameterString + ")");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Failed to print project-structure: " + Arrays.toString(e.getStackTrace()));
            }
        }
        System.out.println("*********************************************************************************************************************************************************************************************************************************");
    }
}