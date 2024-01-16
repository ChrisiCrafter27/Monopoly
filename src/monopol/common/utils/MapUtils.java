package monopol.common.utils;

import java.util.Map;

public class MapUtils {
    public static <K, V> K key(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) return entry.getKey();
        }
        return null;
    }
}
