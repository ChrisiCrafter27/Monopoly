package monopol.common.data;

public enum Street implements IPurchasable {
    BADSTRASSE ("Badstraße", 60, 0, 2, 10, 30, 90, 160, 250, 750, ColorGroup.BROWN, "", false),
    TURMSTRASSE ("Turmstraße", 60, 0, 4, 20, 60, 180, 320, 450, 950, ColorGroup.BROWN, "", false),
    STADIONSTRASSE ("Stadionstraße", 80, 0, 5, 30, 80, 240, 360, 500, 950, ColorGroup.BROWN, "", false),

    CHAUSSESTRASSE ("Chaussestraße", 100, 0, 6, 30, 90, 270, 400, 550, 1050, ColorGroup.CYAN, "", false),
    ELISENSTRASSE ("Elisenstraße", 100, 0, 6, 30, 90, 270, 400, 550, 1050, ColorGroup.CYAN, "", false),
    POSTSTRASSE ("Poststraße", 120, 0, 8, 40, 100, 300, 450, 600, 1100, ColorGroup.CYAN, "", false),
    TIERGARTENSTRASSE ("Tiergartenstraße", 120, 0, 8, 40, 100, 300, 450, 600, 1100, ColorGroup.CYAN, "", false),

    SEESTRASSE ("Seestraße", 140, 0, 10, 50, 150, 450, 625, 750, 1250, ColorGroup.PINK, "", false),
    HAFENSTRASSE ("Hafenstraße", 140, 0, 10, 50, 150, 450, 625, 750, 1250, ColorGroup.PINK, "", false),
    NEUESTRASSE ("Neuestraße", 160, 0, 12, 60, 180, 500, 700, 900, 1400, ColorGroup.PINK, "", false),
    MARKTPLATZ ("Marktplatz", 160, 0, 12, 60, 180, 500, 700, 900, 1400, ColorGroup.PINK, "", false),

    MUENCHENERSTRASSE ("Münchenerstraße", 180, 0, 14, 70, 200, 550, 750, 950, 1450, ColorGroup.ORANGE, "", false),
    WIENERSTRASSE ("Wienerstraße", 180, 0, 14, 70, 200, 550, 750, 950, 1450, ColorGroup.ORANGE, "", false),
    BERLINERSTRASSE ("Berlinerstraße", 200, 0, 16, 80, 220, 600, 800, 1000, 1500, ColorGroup.ORANGE, "", false),
    HAMBURGERSTRASSE ("Hamburgerstraße", 200, 0, 16, 80, 220, 600, 800, 1000, 1500, ColorGroup.ORANGE, "", false),

    THEATERSTRASSE ("Theaterstraße", 220, 0, 18, 90, 250, 700, 875, 1050, 2050, ColorGroup.RED, "", false),
    MUSEUMSTRASSE ("Museumsstraße", 220, 0, 18, 90, 250, 700, 875, 1050, 2050, ColorGroup.RED, "", false),
    OPERNPLATZ ("Opernplatz", 240, 0, 20, 100, 300, 750, 925, 1100, 2100, ColorGroup.RED, "", false),
    KONZERTHAUSSTRASSE ("Konzerthausstraße", 240, 0, 20, 100, 300, 750, 925, 1100, 2100, ColorGroup.RED, "", false),

    LESSINGSTRASSE ("Lessingsstraße", 260, 0, 22, 110, 330, 800, 975, 1150, 2150, ColorGroup.YELLOW, "", false),
    SCHILLERSTRASSE ("Schillerstraße", 260, 0, 22, 110, 330, 800, 975, 1150, 2150, ColorGroup.YELLOW, "", false),
    GOETHESTRASSE ("Göthestraße", 280, 0, 24, 120, 360, 850, 1025, 1200, 2200, ColorGroup.YELLOW, "", false),
    RILKESTRASSE ("Rilkestraße", 280, 0, 24, 120, 360, 850, 1025, 1200, 2200, ColorGroup.YELLOW, "", false),

    RATHAUSPLATZ ("Rathausplatz", 300, 0, 26, 130, 390, 900, 1100, 1275, 2275, ColorGroup.GREEN, "", false),
    HAUPSTRASSE ("Hauptsstraße", 300, 0, 26, 130, 390, 900, 1100, 1275, 2275, ColorGroup.GREEN, "", false),
    BOERSENPLATZ ("Börsenplatz", 320, 0, 28, 150, 450, 1000, 1200, 1400, 2400, ColorGroup.GREEN, "", false),
    BAHNHOFSTRASSE ("Bahnhofstraße", 320, 0, 28, 150, 450, 1000, 1200, 1400, 2400, ColorGroup.GREEN, "", false),

    DOMPLATZ ("Domplatz", 350, 0, 35, 175, 500, 1100, 1300, 1500, 2500, ColorGroup.BLUE, "", false),
    PARKSTRASSE ("Parkstraße", 350, 0, 35, 175, 500, 1100, 1300, 1500, 2500, ColorGroup.BLUE, "", false),
    SCHLOSSALLEE ("Schlossallee", 400, 0, 50, 200, 600, 1400, 1700, 2000, 3000, ColorGroup.BLUE, "", false);


    public final String name;
    public final int price;
    public final int mortgage;
    private int level;
    public final int rentNormal;
    public final int rentOneHouse;
    public final int rentTwoHouses;
    public final int rentThreeHouses;
    public final int rentFourHouses;
    public final int rentHotel;
    public final int rentSkyscraper;
    public final ColorGroup colorGroup;
    private String owner;
    private boolean mortgaged;

    Street(String name, int price, int level, int rentNormal, int rentOneHouse, int rentTwoHouses, int rentThreeHouses, int rentFourHouses, int rentHotel, int rentSkyscraper, ColorGroup colorGroup, String owner, boolean mortgaged) {
        this.name = name;
        this.price = price;
        this.level = level;
        this.mortgage = price / 2;
        this.rentNormal = rentNormal;
        this.rentOneHouse = rentOneHouse;
        this.rentTwoHouses = rentTwoHouses;
        this.rentThreeHouses = rentThreeHouses;
        this.rentFourHouses = rentFourHouses;
        this.rentHotel = rentHotel;
        this.rentSkyscraper = rentSkyscraper;
        this.colorGroup = colorGroup;
        this.owner = owner;
        this.mortgaged = mortgaged;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getMaxLevel() {
        return 6;
    }

    @Override
    public int getUpgradeCost() {
        return switch (colorGroup) {
            case BROWN, CYAN -> 50;
            case PINK, ORANGE -> 100;
            case RED, YELLOW -> 150;
            case GREEN, BLUE -> 200;
        };
    }

    @Override
    public boolean upgrade() {
        if(level >= 6) return false;
        level += 1;
        return true;
    }

    @Override
    public boolean downgrade() {
        if(level <= 0) return false;
        level -= 1;
        return true;
    }

    @Override
    public String getOwner() {
        return owner == null ? "" : owner;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public int getMortgage() {
        return mortgage;
    }

    @Override
    public boolean mortgaged() {
        return mortgaged;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public boolean isMortgaged() {
        return mortgaged;
    }

    @Override
    public boolean mortgage() {
        if(mortgaged) return false;
        mortgaged = true;
        return true;
    }

    @Override
    public boolean unmortgage() {
        if(!mortgaged) return false;
        mortgaged = false;
        return true;
    }

    @Override
    public int getRent(int diceResult) {
        return switch (level) {
            case 0 -> rentNormal;
            case 1 -> rentOneHouse;
            case 2 -> rentTwoHouses;
            case 3 -> rentThreeHouses;
            case 4 -> rentFourHouses;
            case 5 -> rentHotel;
            case 6 -> rentSkyscraper;
            default -> -1;
        };
    }

    @Override
    public String keyText(int line) {
        return switch (line) {
            case 0 -> "Grundstückswert";
            case 1 -> "Miete Grundstück allein";
            case 2 -> "Miete 1 Haus";
            case 3 -> "Miete 2 Häuser";
            case 4 -> "Miete 3 Häuser";
            case 5 -> "Miete 4 Häuser";
            case 6 -> "Miete Hotel";
            case 7 -> "Miete Wolkenkratzer"; //TODO disable when game rule is false
            case 9 -> "Verdoppelung der Miete, wenn die Straße";
            case 10 -> "nicht bebaut ist und du alle bis auf";
            case 11 -> "eine Straße der Farbgruppe besitzt.";
            case 13 -> "Verdreifachung der Miete, wenn die Straße";
            case 14 -> "nicht bebaut ist und du alle Straßen der";
            case 15 -> "Farbgruppe besitzt.";
            case 17 -> "1 Upgrade kostet";
            case 18 -> getOwner() == null || getOwner().equals("") ? "Zu Verkaufen" : "Besitzer: " + getOwner();
            default -> "";
        };
    }

    @Override
    public String valueText(int line) {
        return switch (line) {
            case 0 -> getPrice() + "€";
            case 1 -> rentNormal + "€";
            case 2 -> rentOneHouse + "€";
            case 3 -> rentTwoHouses + "€";
            case 4 -> rentThreeHouses + "€";
            case 5 -> rentFourHouses + "€";
            case 6 -> rentHotel + "€";
            case 7 -> rentSkyscraper + "€"; //TODO disable when game rule is false
            case 17 -> colorGroup.upgradeCost + "€";
            default -> "";
        };
    }

    public String getName() {
        return name;
    }
}
