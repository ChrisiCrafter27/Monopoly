package monopol.server.events;

import monopol.common.data.Field;
import monopol.common.data.IPurchasable;
import monopol.common.data.Player;
import monopol.common.data.Street;

import java.util.List;
import java.util.function.Function;

public enum BuildRule implements Function<Player, List<IPurchasable>> {
    ANYWHERE,
    CURRENT_COLOR_GROUP,
    CURRENT_POS;

    @Override
    public List<IPurchasable> apply(Player player) {
        return switch(this) {
            case ANYWHERE -> Field.purchasables();
            case CURRENT_COLOR_GROUP -> Field.get(player.getPosition()) instanceof IPurchasable purchasable ? Field.purchasables().stream().filter(p -> p.equals(purchasable) || (purchasable instanceof Street s1 && p instanceof Street s2 && s1.colorGroup.equals(s2.colorGroup))).toList() : List.of();
            case CURRENT_POS -> Field.get(player.getPosition()) instanceof IPurchasable purchasable ? List.of(purchasable) : List.of();
        };
    }

    public boolean valid(Player player, IPurchasable purchasable) {
        return apply(player).contains(purchasable);
    }

    public static boolean buildingEquable(Player player) {
        return Field.purchasables().stream().filter(p -> player.getName().equals(p.getOwner()) && p instanceof Street).map(p -> (Street) p).allMatch(s -> buildingEquable(s) != EqualBuildingResult.ILLEGAL);
    }

    public static EqualBuildingResult buildingEquable(Street street) {
        if(street.getOwner() == null) return EqualBuildingResult.OKAY;
        List<Street> streets = Field.purchasables().stream().filter(p -> p instanceof Street).map(p -> (Street) p).filter(s -> s.colorGroup == street.colorGroup && street.getOwner().equals(s.getOwner())).toList();
        if(streets.stream().anyMatch(s -> s.getLevel() < street.getLevel() - 1)) return EqualBuildingResult.ILLEGAL;
        if(streets.stream().anyMatch(s -> s.getLevel() > street.getLevel() + 1)) return EqualBuildingResult.ILLEGAL;
        if(streets.stream().allMatch(s -> s.getLevel() == street.getLevel())) return EqualBuildingResult.OPTIONAL;
        return EqualBuildingResult.OKAY;
    }

    public enum EqualBuildingResult {
        OKAY,
        OPTIONAL,
        ILLEGAL;
    }
}
