package monopol.client.screen;

import monopol.common.data.IPurchasable;
import monopol.common.data.Street;
import monopol.common.data.TrainStation;
import monopol.common.utils.Coordinate;
import monopol.common.utils.Either;
import monopol.common.utils.JUtils;
import monopol.common.utils.MapUtils;

import javax.swing.*;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;

public class HousePane extends JLayeredPane {
    private static final int HOUSES_COUNT = 32;
    private static final int HOTELS_COUNT = 12;
    private static final int SKYSCRAPERS_COUNT = 8;
    private static final int DEPOTS_COUNT = 4;

    public HashMap<JLabel, Either<Street, Integer>> houses = new HashMap<>();
    public HashMap<JLabel, Either<Street, Integer>> hotels = new HashMap<>();
    public HashMap<JLabel, Either<Street, Integer>> skyscrapers = new HashMap<>();
    public HashMap<JLabel, Either<TrainStation, Integer>> depots = new HashMap<>();

    public HousePane(){
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(false);

        for(int i = 0; i< HOUSES_COUNT; i++){
            JLabel label = JUtils.addImage("images/Main_pictures/Haus.png",(i/8)*26+155,(i%8)*30+400, 20, (int) (17.5));
            add(label, DEFAULT_LAYER);
            houses.put(label, new Either<>(null, i));
        }

        for(int i = 0; i< HOTELS_COUNT; i++){
            JLabel label = JUtils.addImage("",(i/8)*26+155,(i%8)*30+400, 20, (int) (17.5));
            add(label, DEFAULT_LAYER);
            hotels.put(label, null);
        }

        for(int i = 0; i< SKYSCRAPERS_COUNT; i++){
            JLabel label = JUtils.addImage("",(i/8)*26+155,(i%8)*30+400, 20, (int) (17.5));
            add(label, DEFAULT_LAYER);
            skyscrapers.put(label, null);
        }

        for(int i = 0; i< DEPOTS_COUNT; i++){
            JLabel label = JUtils.addImage("",(i/8)*26+155,(i%8)*30+400, 20, (int) (17.5));
            add(label, DEFAULT_LAYER);
            depots.put(label, null);
        }
    }

    public void moveHouseToStreet(Street street){
        int x = Coordinate.of(street).getX();
        int y = Coordinate.of(street).getY();
        int seite = 0;
        if (x == 0) {seite = 1;}
        if (y == 60) {seite = 2;}
        if (x == 930) {seite = 3;}
        if (y == 990) {seite = 4;}
        if (seite == 1) {
            x = x + 70;
            y = 1 + 17 * matching(houses, street);
        }
        if (seite == 2) {
            y += 70;
            x += 1 + 17 * matching(houses, street);
        }
        if (seite == 3) {
            y = 1 + 17 * matching(houses, street);
        }
        if (seite == 4) {
            x = 1 + 17 * matching(houses, street);
        }
        JLabel label = getLabel(houses, storedHouses(houses) - 1);
        label.setBounds(x, y, label.getWidth(), label.getHeight());
    }
    public void moveHouseOfStreet(Street street){

    }


    private JLabel getLabel(HashMap<JLabel, Either<Street, Integer>> map, int pos) {
        return MapUtils.key(map, map.values().stream().filter(either -> either.getRight().isPresent() && (either.getRight().get() == pos)).findFirst().orElse(null)).orElse(null);
    }

    private int storedHouses(HashMap<JLabel, Either<Street, Integer>> map) {
        return map.values().stream().filter(either -> either.getRight().isPresent()).toList().size();
    }

    private int matching(HashMap<JLabel, Either<Street, Integer>> map, IPurchasable purchasable) {
        return map.values().stream().filter(either -> either.getLeft().isPresent() && either.getLeft().get().equals(purchasable)).toList().size();
    }

    public void reset() {
        setVisible(false);
    }
    public void init(){
        setVisible(true);
    }
}
