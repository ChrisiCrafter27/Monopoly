package monopol.common.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class DataWriter {
    private final List<Object> data;

    public DataWriter() {
        this.data = new ArrayList<>();
    }

    public void writeInt(int i) {
        data.add(i);
    }

    public void writeDouble(double d) {
        data.add(d);
    }

    public void writeFloat(float f) {
        data.add(f);
    }

    public void writeLong(long l) {
        data.add(l);
    }

    public void writeBool(boolean b) {
        data.add(b);
    }

    public void writeChar(char c) {
        data.add(c);
    }

    public void writeString(String s) {
        data.add(s);
    }

    public <E extends Enum<E>> void writeEnum(E e) {
        writeInt(e.ordinal());
    }

    public <T> void writeList(List<T> list, BiConsumer<DataWriter, T> writer) {
        writeInt(list.size());
        for(T obj : list) writer.accept(this, obj);
    }

    public <K, V> void writeMap(Map<K, V> map, BiConsumer<DataWriter, K> keyWriter, BiConsumer<DataWriter, V> valueWriter) {
        writeInt(map.size());
        for(Map.Entry<K, V> entry : map.entrySet()) {
            keyWriter.accept(this, entry.getKey());
            valueWriter.accept(this, entry.getValue());
        }
    }

    public List<Object> getData() {
        return new ArrayList<>(data);
    }
}
