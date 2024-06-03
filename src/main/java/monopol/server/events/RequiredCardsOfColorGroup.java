package monopol.server.events;

import monopol.common.data.*;

public record RequiredCardsOfColorGroup(OwnedCardsOfColorGroup cardsRequiredForOneHouse, OwnedCardsOfColorGroup cardsRequiredForTwoHouses, OwnedCardsOfColorGroup cardsRequiredForThreeHouses, OwnedCardsOfColorGroup cardsRequiredForFourHouses, OwnedCardsOfColorGroup cardsRequiredForHotel, OwnedCardsOfColorGroup cardsRequiredForSkyscraper, boolean buildEquable, boolean megaBuildings) {

    public boolean valid(Player player, Street street) {
        return switch (street.getLevel()) {
            case 0 -> cardsRequiredForOneHouse.get(street.colorGroup) <= ownedCardsOfColorGroup(street.colorGroup, player);
            case 1 -> cardsRequiredForTwoHouses.get(street.colorGroup) <= ownedCardsOfColorGroup(street.colorGroup, player);
            case 2 -> cardsRequiredForThreeHouses.get(street.colorGroup) <= ownedCardsOfColorGroup(street.colorGroup, player);
            case 3 -> cardsRequiredForFourHouses.get(street.colorGroup) <= ownedCardsOfColorGroup(street.colorGroup, player);
            case 4 -> cardsRequiredForHotel.get(street.colorGroup) <= ownedCardsOfColorGroup(street.colorGroup, player);
            case 5 -> !megaBuildings && cardsRequiredForSkyscraper.get(street.colorGroup) <= ownedCardsOfColorGroup(street.colorGroup, player);
            default -> false;
        };
    }

    private int ownedCardsOfColorGroup(ColorGroup colorGroup, Player player) {
        return Field.purchasables().stream().filter(p -> p instanceof Street street && street.colorGroup == colorGroup && player.getName().equals(street.getOwner())).toList().size();
    }
}
