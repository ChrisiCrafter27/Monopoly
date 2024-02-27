package monopol.common.settings;

import javax.swing.*;

public class IntSetting extends Setting<JTextField, Integer> {
    public final int min, max, defaultValue;

    public IntSetting(String name, int min, int max, int defaultValue) {
        this(name, null, min, max, defaultValue);
    }

    public IntSetting(String name, String tooltip, int min, int max, int defaultValue) {
        super(name, tooltip);
        this.min = min;
        this.defaultValue = defaultValue;
        this.max = max;
        if (defaultValue < min || defaultValue > max) throw new IllegalArgumentException("default value must be in range");
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public int defaultValue() {
        return defaultValue;
    }

    @Override
    public Integer getValue() {
        if(component == null) return defaultValue;
        else try {
            return Integer.parseInt(component.getText());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }
}
