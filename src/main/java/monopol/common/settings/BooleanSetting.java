package monopol.common.settings;

import javax.swing.*;

public class BooleanSetting extends Setting<JCheckBox, Boolean> {
    private final boolean defaultValue;

    public BooleanSetting(String name, boolean defaultValue) {
        this(name, null, defaultValue);
    }

    public BooleanSetting(String name, String tooltip, boolean defaultValue) {
        super(name, tooltip);
        this.defaultValue = defaultValue;
    }

    public boolean defaultValue() {
        return defaultValue;
    }

    @Override
    public Boolean getValue() {
        if(component == null) return defaultValue;
        else return component.isSelected();
    }
}