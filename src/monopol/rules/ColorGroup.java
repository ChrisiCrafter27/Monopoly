package monopol.rules;

public enum ColorGroup {
    BROWN (1, 50),
    CYAN (2, 50),
    PINK (3, 100),
    ORANGE (4, 100),
    RED (5, 150),
    YELLOW (6, 150),
    GREEN (7, 200),
    BLUE (8, 200);

    public final int id;
    public final int upgradeCost;

    ColorGroup(int id, int upgradeCost) {
        this.id = id;
        this.upgradeCost = upgradeCost;
    }
}
