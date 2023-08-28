package monopol.rules;

public enum TrainStation implements ISelectedCard {
    SUEDBAHNHOF ("Südbahnhof"),
    WESTBAHNHOF ("Westbahnhof"),
    NORDBAHNHOF ("Nordbahnhof"),
    HAUPTBAHNHOF ("Hauptbahnhof");

    public final String name;
    public final int price = 200;
    public final int mortgage = 100;
    private boolean upgraded;
    public final int rentNormal = 25;
    private String owner;
    private boolean mortgaged;

    TrainStation(String name) {
        this.name = name;
    }

    public boolean isUpgraded() {
        return upgraded;
    }

    public boolean upgrade() {
        if(upgraded) return false;
        upgraded = true;
        return true;
    }

    public boolean downgrade() {
        if(!upgraded) return false;
        upgraded = false;
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
        //TODO: Return rent
        return -1;
    }
}
