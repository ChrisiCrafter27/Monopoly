package monopol.common.settings;

import java.awt.*;

public abstract class Setting<C extends Component, R> {
    private final String name, tooltip;
    protected C component;

    protected Setting(String name, String tooltip) {
        this.name = name;
        this.tooltip = tooltip;
    }

    public String name() {
        return name;
    }

    public String tooltip() {
        return tooltip;
    }

    public void setComponent(C component) {
        this.component = component;
    }

    public abstract R getValue();
}