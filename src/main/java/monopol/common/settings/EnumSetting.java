package monopol.common.settings;

import javax.swing.*;
import java.util.Arrays;

public class EnumSetting<E extends Enum<E>> extends Setting<JComboBox<E>, E> {
    private final E[] values;
    private final E defaultValue;

    public EnumSetting(String name, E[] values, E defaultValue) {
        this(name, null, values, defaultValue);
    }

    public EnumSetting(String name, String tooltip, E[] values, E defaultValue) {
        super(name, tooltip);
        this.values = values;
        this.defaultValue = defaultValue;
        if (!Arrays.asList(values).contains(defaultValue)) throw new IllegalArgumentException("default must be a value");
    }

    public E[] values() {
        return values;
    }

    public E defaultValue() {
        return defaultValue;
    }

    @Override
    public E getValue() {
        if(component == null) return defaultValue;
        else return component.getItemAt(component.getSelectedIndex());
    }
}
