package monopol.data;

import monopol.rules.ColorGroup;

public enum Plant implements IPurchasable {
    GASWERK ("Gaswerk"),
    ELEKTRIZITAETSWERK ("Elektrizitätswerk"),
    WASSERWERK ("Wasserwerk");

    public final String name;
    public final int price = 150;
    public final int mortgage = 75;
    private String owner = "";
    private boolean mortgaged;

    Plant(String name) {
        this.name = name;
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
        return 0;
    }

    @Override
    public boolean upgrade() {
        return false;
    }

    @Override
    public boolean downgrade() {
        return false;
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
        for (Plant plant : values()) {
            if (plant.getOwner().equals(getOwner())) i++;
        }
        int toReturn = switch (i) {
            case 1 -> 4;
            case 2 -> 10;
            case 3 -> 20;
            default -> 0;
        };
        return toReturn * diceResult;
    }

    @Override
    public String keyText(int line) {
        return "";
    }

    @Override
    public String valueText(int line) {
        return "";
    }
}
