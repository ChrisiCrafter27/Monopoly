package monopol.common.utils;

import monopol.common.data.IPurchasable;
import monopol.common.data.Plant;
import monopol.common.data.Street;
import monopol.common.data.TrainStation;

public class Coordinate {
    private int x;
    private int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate() {
        this(0, 0);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public static Coordinate of(IPurchasable purchasable){
        if(purchasable instanceof Street street) return switch (street){
            case BADSTRASSE -> new Coordinate(0, 920);
            case TURMSTRASSE -> new Coordinate(0, 780);
            case STADIONSTRASSE -> new Coordinate(0, 710);
            case CHAUSSESTRASSE ->  new Coordinate(0, 500);
            case ELISENSTRASSE ->  new Coordinate(0, 430);
            case POSTSTRASSE ->  new Coordinate(0, 220);
            case TIERGARTENSTRASSE ->  new Coordinate(0, 150);

            case SEESTRASSE ->  new Coordinate(160, 60);
            case HAFENSTRASSE ->  new Coordinate(230, 60);
            case NEUESTRASSE ->  new Coordinate(370, 60);
            case MARKTPLATZ ->  new Coordinate(440, 60);
            case MUENCHENERSTRASSE ->  new Coordinate(580, 60);
            case WIENERSTRASSE ->  new Coordinate(720, 60);
            case BERLINERSTRASSE ->  new Coordinate(790, 60);
            case HAMBURGERSTRASSE ->  new Coordinate(860, 60);

            case THEATERSTRASSE ->  new Coordinate(930, 150);
            case MUSEUMSTRASSE ->  new Coordinate(930, 290);
            case OPERNPLATZ ->  new Coordinate(930, 360);
            case KONZERTHAUSSTRASSE ->  new Coordinate(930, 430);
            case LESSINGSTRASSE ->  new Coordinate(930, 640);
            case SCHILLERSTRASSE ->  new Coordinate(930, 710);
            case GOETHESTRASSE ->  new Coordinate(930, 850);
            case RILKESTRASSE ->  new Coordinate(930, 920);

            case RATHAUSPLATZ ->  new Coordinate(860, 990);
            case HAUPSTRASSE ->  new Coordinate(790, 990);
            case BOERSENPLATZ ->  new Coordinate(720, 990);
            case BAHNHOFSTRASSE ->  new Coordinate(580, 990);
            case DOMPLATZ ->  new Coordinate(300, 990);
            case PARKSTRASSE ->  new Coordinate(230, 990);
            case SCHLOSSALLEE ->  new Coordinate(90, 990);
        };

        if(purchasable instanceof Plant plant) return switch (plant) {
            case GASWERK -> new Coordinate(0, 290);
            case ELEKTRIZITAETSWERK -> new Coordinate(300, 60);
            case WASSERWERK -> new Coordinate(930, 780);
        };

        if(purchasable instanceof TrainStation trainStation) return switch (trainStation) {
            case SUEDBAHNHOF -> new Coordinate(0, 570);
            case WESTBAHNHOF -> new Coordinate(510, 60);
            case NORDBAHNHOF -> new Coordinate(930, 570);
            case HAUPTBAHNHOF -> new Coordinate(510, 990);
        };
        return null;
    }
}
