package monopol.common.data;

public enum TrainStation implements IPurchasable {
    SUEDBAHNHOF ("Südbahnhof"),
    WESTBAHNHOF ("Westbahnhof"),
    NORDBAHNHOF ("Nordbahnhof"),
    HAUPTBAHNHOF ("Hauptbahnhof");

    public final String name;
    public final int price = 200;
    public final int mortgage = 100;
    private boolean upgraded;
    public final int rentNormal = 25;
    private String owner = "";
    private boolean mortgaged;

    TrainStation(String name) {
        this.name = name;
    }

    public boolean isUpgraded() {
        return upgraded;
    }

    @Override
    public boolean upgrade() {
        if(upgraded) return false;
        upgraded = true;
        return true;
    }

    @Override
    public boolean downgrade() {
        if(!upgraded) return false;
        upgraded = false;
        return true;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public int price() {
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
    public int getLevel() {
        return isUpgraded() ? 1 : 0;
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
        int i = 0;
        for (TrainStation station : values()) {
            if (station.getOwner().equals(getOwner())) i++;
        }
        int toReturn = 25;
        while (i > 1) {
            toReturn *= 2;
            i--;
        }
        if (isUpgraded()) toReturn *= 2;
        return toReturn;
    }

    @Override
    public String keyText(int line) {
        return switch (line) {
            case 1 -> "Grundstückswert";
            case 3 -> "Miete 1 Bahnhof";
            case 4 -> "Miete 2 Bahnhöfe";
            case 5 -> "Miete 3 Bahnhöfe";
            case 6 -> "Miete 4 Bahnhöfe";
            case 9 -> "Die miete erhöht sich, je mehr";
            case 10 -> "Bahnhöfe du besitzt.";
            case 13 -> "Ein Zugdepot verdoppelt";
            case 14 -> "die Miete.";
            case 17 -> "1 Upgrade kostet";
            case 18 -> getOwner() == null || getOwner().equals("") ? "Zu Verkaufen" : "Besitzer: " + getOwner();
            default -> "";
        };
    }

    @Override
    public String valueText(int line) {
        return switch (line) {
            case 1 -> price() + "€";
            case 3 -> "25€";
            case 4 -> "50€";
            case 5 -> "100€";
            case 6 -> "200€";
            case 17 -> "100€";
            default -> "";
        };
    }

    @Override
    public String getName() {
        return name;
    }
}
