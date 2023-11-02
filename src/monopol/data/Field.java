package monopol.data;

import java.util.ArrayList;
import java.util.Arrays;

public enum Field implements IField {
    GEMEINSCHAFTSFELD,
    EREIGNISFELD,
    AUKTION,
    BUSFAHRKARTE,
    GESCHENK,
    LOS,
    ZUBESUCH,
    FREIPARKEN,
    INSGEFAENGNIS,
    EINKOMMENSSTEUER,
    ZUSATZSTEUER;

    private static final ArrayList<IField> fields = new ArrayList<>(Arrays.asList(
            Field.LOS,

            Street.BADSTRASSE,
            Field.GEMEINSCHAFTSFELD,
            Street.TURMSTRASSE,
            Street.STADIONSTRASSE,
            Field.EINKOMMENSSTEUER,
            TrainStation.SUEDBAHNHOF,
            Street.CHAUSSESTRASSE,
            Street.ELISENSTRASSE,
            Field.EREIGNISFELD,
            Plant.GASWERK,
            Street.POSTSTRASSE,
            Street.TIERGARTENSTRASSE,

            Field.ZUBESUCH,
            Field.AUKTION,
            Street.SEESTRASSE,
            Street.HAFENSTRASSE,
            Plant.ELEKTRIZITAETSWERK,
            Street.NEUESTRASSE,
            Street.MARKTPLATZ,
            TrainStation.WESTBAHNHOF,
            Street.MUENCHENERSTRASSE,
            Field.GEMEINSCHAFTSFELD,
            Street.WIENERSTRASSE,
            Street.BERLINERSTRASSE,
            Street.HAMBURGERSTRASSE,

            Field.FREIPARKEN,
            Street.THEATERSTRASSE,
            Field.EREIGNISFELD,
            Street.MUSEUMSTRASSE,
            Street.OPERNPLATZ,
            Street.KONZERTHAUSSTRASSE,
            Field.BUSFAHRKARTE,
            TrainStation.NORDBAHNHOF,
            Street.LESSINGSTRASSE,
            Street.SCHILLERSTRASSE,
            Plant.WASSERWERK,
            Street.GOETHESTRASSE,
            Street.RILKESTRASSE,

            Field.INSGEFAENGNIS,
            Street.RATHAUSPLATZ,
            Street.HAUPSTRASSE,
            Street.BOERSENPLATZ,
            Field.GEMEINSCHAFTSFELD,
            Street.BAHNHOFSTRASSE,
            TrainStation.HAUPTBAHNHOF,
            Field.EREIGNISFELD,
            Field.GESCHENK,
            Street.DOMPLATZ,
            Street.PARKSTRASSE,
            Field.ZUSATZSTEUER,
            Street.SCHLOSSALLEE
    ));

    public static IField getField(int i) {
        if(i < 1) throw new IllegalArgumentException();
        while (i > 52) {
            i -= 52;
        }
        return fields.get(i-1);
    }
}
