package monopol.rules;

public enum Street {
    BADSTRASSE ("Badstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    TURMSTRASSE ("Turmstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    STADIONSTRASSE ("Stadionstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),

    CHAUSSESTRASSE ("Chaussestraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    ELISENSTRASSE ("Elisenstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    POSTSTRASSE ("Poststraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    TIERGARTENSTRASSE ("Tiergartenstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),

    SEESTRASSE ("Seestraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    HAFENSTRASSE ("Hafenstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    NEUESTRASSE ("Neuestraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    MARKTPLATZ ("Marktplatz", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),

    MÜNCHENERSTRASSE ("Münchenerstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    WIENERSTRASSE ("Wienerstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    BERLINERSTRASSE ("Berlinerstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    HAMBURGERSTRASSE ("Hamburgerstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),

    THEATERSTRASSE ("Theaterstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    MUSEUMSTRASSE ("Museumsstra0e", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    OPERNPLATZ ("Opernplatz", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    KONZERTHAUSSTRASSE ("Konzerthausstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),

    LESSINGSTRASSE ("Lessingsstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    SCHILLERSTRASSE ("Schillerstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    GÖTHESTRASSE ("Göthestraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    RILKESTRASSE ("Rilkestraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),

    RATHAUSPLATZ ("Rathausplatz", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    HAUPSTRASSE ("Hauptsstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    BÖRSENPLATZ ("Börsenplatz", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    BAHNHOFSTRASSE ("Bahnhofstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),

    DOMPLATZ ("Domplatz", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    PARKSTRASSE ("Parkstraße", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false),
    SCHLOSSALLEE ("Schlossallee", 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, ColorGroup.BROWN, null, false);


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

    Street(String name, int price, int level, int mortgage, int rentNormal, int rentOneHouse, int rentTwoHouses, int rentThreeHouses, int rentFourHouses, int rentHotel, int rentSkyscraper, ColorGroup colorGroup, String owner, boolean mortgaged) {
        this.name = name;
        this.price = price;
        this.level = level;
        this.mortgage = mortgage;
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


}
