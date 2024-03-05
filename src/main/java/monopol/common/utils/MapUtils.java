package monopol.common.utils;

import java.util.Map;
import java.util.Optional;

public class MapUtils {
    public static <K, V> Optional<K> key(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) return Optional.of(entry.getKey());
        }
        return Optional.empty();
    }
}
