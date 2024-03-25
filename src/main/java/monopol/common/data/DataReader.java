package monopol.common.data;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataReader {
    private final Iterator<Object> data;

    public DataReader(List<Object> data) {
        this.data = data.iterator();
    }

    public int readInt() {
        if(!data.hasNext()) throw new IllegalStateException("no data left!");
        Object obj = data.next();
        if(obj instanceof Integer i) return i;
        else throw new IllegalStateException("wrong data type!");
    }

    public double readDouble() {
        if(!data.hasNext()) throw new IllegalStateException("no data left!");
        Object obj = data.next();
        if(obj instanceof Double d) return d;
        else throw new IllegalStateException("wrong data type!");
    }

    public float readFloat() {
        if(!data.hasNext()) throw new IllegalStateException("no data left!");
        Object obj = data.next();
        if(obj instanceof Float f) return f;
        else throw new IllegalStateException("wrong data type!");
    }

    public long readLong() {
        if(!data.hasNext()) throw new IllegalStateException("no data left!");
        Object obj = data.next();
        if(obj instanceof Long l) return l;
        else throw new IllegalStateException("wrong data type!");
    }

    public boolean readBool() {
        if(!data.hasNext()) throw new IllegalStateException("no data left!");
        Object obj = data.next();
        if(obj instanceof Boolean b) return b;
        else throw new IllegalStateException("wrong data type!");
    }

    public char readChar() {
        if(!data.hasNext()) throw new IllegalStateException("no data left!");
        Object obj = data.next();
        if(obj instanceof Character c) return c;
        else throw new IllegalStateException("wrong data type!");
    }

    public String readString() {
        if(!data.hasNext()) throw new IllegalStateException("no data left!");
        Object obj = data.next();
        if(obj instanceof String s) return s;
        else return null;
    }

    public <E extends Enum<E>> E readEnum(Class<E> clazz) {
        return clazz.getEnumConstants()[readInt()];
    }

    public <T> List<T> readList(Supplier<? extends List<T>> listSup, Function<DataReader, T> reader) {
        int size = readInt();
        List<T> list = listSup.get();
        for(int i = 0; i < size; i++) {
            list.add(reader.apply(this));
        }
        return list;
    }

    public <K, V> Map<K, V> readMap(Supplier<? extends Map<K, V>> mapSup, Function<DataReader, K> keyReader, Function<DataReader, V> valueReader) {
        int size = readInt();
        Map<K, V> map = mapSup.get();
        for(int i = 0; i < size; i++) {
            map.put(keyReader.apply(this), valueReader.apply(this));
        }
        return map;
    }
}
