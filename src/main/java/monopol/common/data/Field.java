package monopol.common.data;

import java.util.ArrayList;
import java.util.List;

public enum Field implements IField {
    GEMEINSCHAFTSFELD,
    EREIGNISFELD,
    AUKTION,
    BUSFAHRKARTE,
    GESCHENK,
    EINKOMMENSSTEUER,
    ZUSATZSTEUER;

    private static final ArrayList<IField> FIELDS = new ArrayList<>(List.of(
            Corner.LOS,
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

            Corner.GEFAENGNIS,
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

            Corner.FREIPARKEN,
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

            Corner.INSGEFAENGNIS,
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

    public static List<IField> fields() {
        return new ArrayList<>(FIELDS);
    }

    public static List<IPurchasable> purchasables() {
        return fields().stream().filter(field -> field instanceof IPurchasable).map(field -> (IPurchasable) field).toList();
    }

    public static Corner nextCorner(int i) {
        do {
            i++;
        } while (!(get(i) instanceof Corner));
        return (Corner) get(i);
    }

    public static IField get(int i) {
        if(i < 0) throw new IllegalArgumentException();
        while (i >= 52) {
            i -= 52;
        }
        return FIELDS.get(i);
    }
}
