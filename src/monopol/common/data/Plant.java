package monopol.common.data;

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
        return switch (line) {
            case 1 -> "Grundstückswert";
            case 3 -> "Miete 1 Werk";
            case 4 -> "Miete 2 Werke";
            case 5 -> "Miete 3 Werke";
            case 8 -> "Die miete erhöht sich, je mehr";
            case 9 -> "Werke du besitzt.";
            case 12 -> "Die Miete ergibt sich aus der";
            case 13 -> "Würfelzahl der Person, die auf";
            case 14 -> "das Feld gekommen ist.";
            case 18 -> getOwner() == null || getOwner().equals("") ? "Zu Verkaufen" : "Besitzer: " + getOwner();
            default -> "";
        };
    }

    @Override
    public String valueText(int line) {
        return switch (line) {
            case 1 -> price() + "€";
            case 3 -> "4€ • Wurf";
            case 4 -> "10€ • Wurf";
            case 5 -> "20€ • Wurf";
            default -> "";
        };
    }

    @Override
    public String getName() {
        return name;
    }
}
