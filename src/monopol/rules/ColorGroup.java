package monopol.rules;

public enum ColorGroup {
    BROWN (50),
    CYAN (50),
    PINK (100),
    ORANGE (100),
    RED (150),
    YELLOW (150),
    GREEN (200),
    BLUE (200);

    private int upgradeCost;

    private ColorGroup(int upgradeCost) {
        this.upgradeCost = upgradeCost;
    }

    public int getUpgradeCost() {
        return upgradeCost;
    }
}
