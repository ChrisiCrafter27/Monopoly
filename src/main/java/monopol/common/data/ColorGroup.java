package monopol.common.data;

import monopol.common.utils.Direction;

import java.io.Serializable;

public enum ColorGroup implements Serializable {
    BROWN (1, 50, "brown", Direction.LEFT),
    CYAN (2, 50, "cyan", Direction.LEFT),
    PINK (3, 100, "pink", Direction.UP),
    ORANGE (4, 100, "orange", Direction.UP),
    RED (5, 150, "red", Direction.RIGHT),
    YELLOW (6, 150, "yellow", Direction.RIGHT),
    GREEN (7, 200, "green", Direction.DOWN),
    BLUE (8, 200, "blue", Direction.DOWN);

    public final int id;
    public final int upgradeCost;
    public final String image;
    public final Direction side;

    ColorGroup(int id, int upgradeCost, String image, Direction side) {
        this.id = id;
        this.upgradeCost = upgradeCost;
        this.image = image;
        this.side = side;
    }
}
