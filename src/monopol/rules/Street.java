package monopol.rules;

public enum Street implements ISelectedCard {
    BADSTRASSE ("Badstraße", 60, 0, 2, 10, 30, 90, 160, 250, 750, ColorGroup.BROWN, null, false),
    TURMSTRASSE ("Turmstraße", 60, 0, 4, 20, 60, 180, 320, 450, 950, ColorGroup.BROWN, null, false),
    STADIONSTRASSE ("Stadionstraße", 60, 0, 5, 30, 80, 240, 360, 500, 950, ColorGroup.BROWN, null, false),

    CHAUSSESTRASSE ("Chaussestraße", 60, 0, 6, 30, 90, 270, 400, 550, 1050, ColorGroup.CYAN, null, false),
    ELISENSTRASSE ("Elisenstraße", 60, 0, 6, 30, 90, 270, 400, 550, 1050, ColorGroup.CYAN, null, false),
    POSTSTRASSE ("Poststraße", 60, 0, 8, 40, 100, 300, 450, 600, 1100, ColorGroup.CYAN, null, false),
    TIERGARTENSTRASSE ("Tiergartenstraße", 60, 0, 8, 40, 100, 300, 450, 600, 1100, ColorGroup.CYAN, null, false),

    SEESTRASSE ("Seestraße", 60, 0, 10, 50, 150, 450, 625, 750, 1250, ColorGroup.PINK, null, false),
    HAFENSTRASSE ("Hafenstraße", 60, 0, 10, 50, 150, 450, 625, 750, 1250, ColorGroup.PINK, null, false),
    NEUESTRASSE ("Neuestraße", 60, 0, 12, 60, 180, 500, 700, 900, 1400, ColorGroup.PINK, null, false),
    MARKTPLATZ ("Marktplatz", 60, 0, 12, 60, 180, 500, 700, 900, 1400, ColorGroup.PINK, null, false),

    MUENCHENERSTRASSE ("Münchenerstraße", 60, 0, 14, 70, 200, 550, 750, 950, 1450, ColorGroup.ORANGE, null, false),
    WIENERSTRASSE ("Wienerstraße", 60, 0, 14, 70, 200, 550, 750, 950, 1450, ColorGroup.ORANGE, null, false),
    BERLINERSTRASSE ("Berlinerstraße", 60, 0, 16, 80, 220, 600, 800, 1000, 1500, ColorGroup.ORANGE, null, false),
    HAMBURGERSTRASSE ("Hamburgerstraße", 60, 0, 16, 80, 220, 600, 800, 1000, 1500, ColorGroup.ORANGE, null, false),

    THEATERSTRASSE ("Theaterstraße", 60, 0, 18, 90, 250, 700, 875, 1050, 2050, ColorGroup.RED, null, false),
    MUSEUMSTRASSE ("Museumsstra0e", 60, 0, 18, 90, 250, 700, 875, 1050, 2050, ColorGroup.RED, null, false),
    OPERNPLATZ ("Opernplatz", 60, 0, 20, 100, 300, 750, 925, 1100, 2100, ColorGroup.RED, null, false),
    KONZERTHAUSSTRASSE ("Konzerthausstraße", 60, 0, 20, 100, 300, 750, 925, 1100, 2100, ColorGroup.RED, null, false),

    LESSINGSTRASSE ("Lessingsstraße", 60, 0, 22, 110, 330, 800, 975, 1150, 2150, ColorGroup.YELLOW, null, false),
    SCHILLERSTRASSE ("Schillerstraße", 60, 0, 22, 110, 330, 800, 975, 1150, 2150, ColorGroup.YELLOW, null, false),
    GOETHESTRASSE ("Göthestraße", 60, 0, 24, 120, 360, 850, 1025, 1200, 2200, ColorGroup.YELLOW, null, false),
    RILKESTRASSE ("Rilkestraße", 60, 0, 24, 120, 360, 850, 1025, 1200, 2200, ColorGroup.YELLOW, null, false),

    RATHAUSPLATZ ("Rathausplatz", 60, 0, 26, 130, 390, 900, 1100, 1275, 2275, ColorGroup.GREEN, null, false),
    HAUPSTRASSE ("Hauptsstraße", 60, 0, 26, 130, 390, 900, 1100, 1275, 2275, ColorGroup.GREEN, null, false),
    BOERSENPLATZ ("Börsenplatz", 60, 0, 28, 150, 450, 1000, 1200, 1400, 2400, ColorGroup.GREEN, null, false),
    BAHNHOFSTRASSE ("Bahnhofstraße", 60, 0, 28, 150, 450, 1000, 1200, 1400, 2400, ColorGroup.GREEN, null, false),

    DOMPLATZ ("Domplatz", 60, 0, 35, 175, 500, 1100, 1300, 1500, 2500, ColorGroup.BLUE, null, false),
    PARKSTRASSE ("Parkstraße", 60, 0, 35, 175, 500, 1100, 1300, 1500, 2500, ColorGroup.BLUE, null, false),
    SCHLOSSALLEE ("Schlossallee", 60, 0, 50, 200, 600, 1400, 1700, 2000, 3000, ColorGroup.BLUE, null, false);


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

    private Street(String name, int price, int level, int rentNormal, int rentOneHouse, int rentTwoHouses, int rentThreeHouses, int rentFourHouses, int rentHotel, int rentSkyscraper, ColorGroup colorGroup, String owner, boolean mortgaged) {
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

    public int getLevel() {
        return level;
    }

    public boolean upgrade() {
        if(level >= 6) return false;
        level += 1;
        return true;
    }

    public boolean downgrade() {
        if(level <= 0) return false;
        level -= 1;
        return true;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isMortgaged() {
        return mortgaged;
    }

    public boolean mortgage() {
        if(mortgaged) return false;
        mortgaged = true;
        return true;
    }

    public boolean unmortgage() {
        if(!mortgaged) return false;
        mortgaged = false;
        return true;
    }

    public int getRent() {
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
}
