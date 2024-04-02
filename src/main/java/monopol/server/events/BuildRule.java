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
}
