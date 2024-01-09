package monopol.rules;

public enum ColorGroup {
    BROWN (1, 50, "brown"),
    CYAN (2, 50, "cyan"),
    PINK (3, 100, "pink"),
    ORANGE (4, 100, "orange"),
    RED (5, 150, "red"),
    YELLOW (6, 150, "yellow"),
    GREEN (7, 200, "green"),
    BLUE (8, 200, "blue");

    public final int id;
    public final int upgradeCost;
    public final String image;

    ColorGroup(int id, int upgradeCost, String image) {
        this.id = id;
        this.upgradeCost = upgradeCost;
        this.image = image;
    }
}
