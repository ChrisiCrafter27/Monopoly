package monopol.rules;

public enum Plant implements ISelectedCard {
    GASWERK ("Gaswerk"),
    ELEKTRIZITAETSWERK ("Elektrizitätswerk"),
    WASSERWERK ("Wasserwerk");

    public final String name;
    public final int price = 150;
    public final int mortgage = 75;
    private String owner;
    private boolean mortgaged;

    Plant(String name) {
        this.name = name;
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
}
