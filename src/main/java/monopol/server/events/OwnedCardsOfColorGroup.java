package monopol.server.events;

import monopol.common.data.ColorGroup;
import monopol.common.data.Field;
import monopol.common.data.Street;

public enum OwnedCardsOfColorGroup {
    ONE,
    TWO,
    THREE,
    ALL,
    ALL_BUT_ONE,
    ALL_BUT_TWO;

    public int get(ColorGroup colorGroup) {
        return switch (this) {
            case ONE -> 1;
            case TWO -> 2;
            case THREE -> 3;
            case ALL -> cardsOfColorGroup(colorGroup);
            case ALL_BUT_ONE -> cardsOfColorGroup(colorGroup) - 1;
            case ALL_BUT_TWO -> cardsOfColorGroup(colorGroup) - 2;
        };
    }

    private int cardsOfColorGroup(ColorGroup colorGroup) {
        return Field.purchasables().stream().filter(p -> p instanceof Street street && street.colorGroup == colorGroup).toList().size();
    }
}
