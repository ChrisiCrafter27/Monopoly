package monopol.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.nio.file.Files;

public class Json {
    private static final ObjectMapper OBJECT_MAPPER = getObjectMapper();

    private static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper = mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper = mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    public static JsonNode toJson(String value) throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(value);
    }

    public static JsonNode toJson(Object object) {
        return OBJECT_MAPPER.valueToTree(object);
    }

    public static String toString(JsonNode node, boolean readable) throws JsonProcessingException {
        ObjectWriter writer = OBJECT_MAPPER.writer();
        if(readable) {
            writer = writer.with(SerializationFeature.INDENT_OUTPUT);
        }
        return writer.writeValueAsString(node);
    }

    public static String toString(Object object, boolean readable) throws JsonProcessingException {
        return toString(toJson(object), readable);
    }

    public static String toString(File file, boolean readable) throws JsonProcessingException {
        return toString(toJson(file), readable);
    }

    public static <T> T toObject(JsonNode node, Class<T> clazz) throws JsonProcessingException {
        return OBJECT_MAPPER.treeToValue(node, clazz);
    }

    public static <T> T toObject(File file, Class<T> clazz) throws IOException {
        return toObject(toJson(Files.readString(file.toPath())), clazz);
    }

    public static <T> T toObject(String value, Class<T> clazz) throws JsonProcessingException {
        return OBJECT_MAPPER.treeToValue(toJson(value), clazz);
    }

    public static void saveToFile(JsonNode node, String directory, String fileName) throws IOException {
        new File(directory).mkdirs();
        FileWriter writer = new FileWriter(directory + "/" +  fileName);
        writer.write(toString(node, true));
        writer.close();
    }

    public static void saveToFile(Object object, String directory, String fileName) throws IOException {
        saveToFile(toJson(object), directory, fileName);
    }
}