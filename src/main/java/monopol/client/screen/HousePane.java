package monopol.client.screen;

import monopol.common.data.Field;
import monopol.common.data.IPurchasable;
import monopol.common.data.Street;
import monopol.common.data.TrainStation;
import monopol.common.utils.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class HousePane extends JLayeredPane {
    private static final int HOUSES_COUNT = 32;
    private static final int HOTELS_COUNT = 12;
    private static final int SKYSCRAPERS_COUNT = 0;
    private static final int DEPOTS_COUNT = 0;

    public HashMap<JLabel, Pair<Street, Integer>> houses = new HashMap<>();
    public HashMap<JLabel, Pair<Street, Integer>> hotels = new HashMap<>();
    public HashMap<JLabel, Pair<Street, Integer>> skyscrapers = new HashMap<>();
    public HashMap<JLabel, Pair<TrainStation, Integer>> depots = new HashMap<>();

    public HousePane(){
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(false);

        for(int i = 0; i < HOUSES_COUNT; i++){
            JLabel label = JUtils.addImage("images/Main_pictures/Haus.png",(i%4)*26+158,(i/4)*30+395, 20, 18);
            add(label, DEFAULT_LAYER);
            houses.put(label, new Pair<>(null, i));
        }

        for(int i = 0; i < HOTELS_COUNT; i++){
            JLabel label = JUtils.addImage("images/Main_pictures/Hotel.png",(i%3)*36+767,(i/3)*40+395, 24, 30);
            add(label, DEFAULT_LAYER);
            hotels.put(label, new Pair<>(null, i));
        }

        for(int i = 0; i< SKYSCRAPERS_COUNT; i++){
            JLabel label = JUtils.addImage("images/Main_pictures/WOK_2.png",(i%3)*36+767+(i>5?18:0),(i/3)*60+565, 20, 40);
            add(label, DEFAULT_LAYER);
            skyscrapers.put(label, new Pair<>(null, i));
        }

        for(int i = 0; i< DEPOTS_COUNT; i++){
            JLabel label = JUtils.addImage("",(i/2)*26+155,(i%8)*30+400, 20, (int) (17.5));
            add(label, DEFAULT_LAYER);
            depots.put(label, new Pair<>(null, i));
        }
    }

    public synchronized void update() {
        for(IPurchasable purchasable : Field.purchasables()) {
            if(purchasable instanceof Street street) {
                int level = street.getLevel();
                if(level == 6) {
                    while(buildingsOn(houses, street) > 0) {
                        moveHouseOffStreet(street);
                    }
                    if(buildingsOn(hotels, street) == 1) moveHotelOffStreet(street);
                    if(buildingsOn(skyscrapers, street) == 0) moveSkyscraperToStreet(street);
                } else if(level == 5) {
                    while(buildingsOn(houses, street) > 0) {
                        moveHouseOffStreet(street);
                    }
                    if(buildingsOn(hotels, street) == 0) moveHotelToStreet(street);
                    if(buildingsOn(skyscrapers, street) == 1) moveSkyscraperOffStreet(street);
                } else if(level > 0) {
                    while(buildingsOn(houses, street) > level) {
                        moveHouseOffStreet(street);
                    }
                    while(buildingsOn(houses, street) < level) {
                        moveHouseToStreet(street);
                    }
                    if(buildingsOn(hotels, street) == 1) moveHotelOffStreet(street);
                    if(buildingsOn(skyscrapers, street) == 1) moveSkyscraperOffStreet(street);
                } else {
                    while(buildingsOn(houses, street) > 0) {
                        moveHouseOffStreet(street);
                    }
                    if(buildingsOn(hotels, street) == 1) moveHotelOffStreet(street);
                    if(buildingsOn(skyscrapers, street) == 1) moveSkyscraperOffStreet(street);
                }
            }
            if(purchasable instanceof TrainStation trainStation) {
                boolean upgraded = trainStation.isUpgraded();
                if(upgraded && buildingsOn(depots, trainStation) == 0) ; //add depot
                if(!upgraded && buildingsOn(depots, trainStation) == 1) ; //remove depot
            }
        }
    }

    private void moveHouseToStreet(Street street) {
        int x = Coordinate.of(street).getX();
        int y = Coordinate.of(street).getY();
        int side = 0;
        if (x == 0) side = 1;
        if (y == 60) side = 2;
        if (x == 930) side = 3;
        if (y == 990) side = 4;
        int buildingsOn = buildingsOn(houses, street);
        if (side == 1) {
            x += 69;
            y += (int) (17.25 * buildingsOn + 1);
        }
        if (side == 2) {
            x += (int) (17.5 * buildingsOn - 1);
            y += 70;
        }
        if (side == 3) {
            x += 1;
            y += (int) (17.25 * buildingsOn + 1);
        }
        if (side == 4) {
            x += (int) (17.5 * buildingsOn - 1);
            y += 2;
        }
        JLabel label = getLabel(houses, storedBuildings(houses) - 1);
        label.setBounds(JUtils.getX(x), JUtils.getY(y), label.getWidth(), label.getHeight());
        houses.replace(label, new Pair<>(street, buildingsOn));
    }

    private void moveHouseOffStreet(Street street) {
        int storedHouses = storedBuildings(houses);
        int x = (storedHouses%4)*26+155;
        int y = (storedHouses/4)*30+400;
        JLabel label = getLabel(houses, street, buildingsOn(houses, street) - 1);
        label.setBounds(JUtils.getX(x), JUtils.getY(y), label.getWidth(), label.getHeight());
        houses.replace(label, new Pair<>(null, storedHouses));
    }

    public void moveHotelToStreet(Street street) {
        int x = Coordinate.of(street).getX();
        int y = Coordinate.of(street).getY();
        int side = 0;
        if (x == 0) side = 1;
        if (y == 60) side = 2;
        if (x == 930) side = 3;
        if (y == 990) side = 4;
        int buildingsOn = buildingsOn(hotels, street);
        if (side == 1) {
            x += 67;
            y += 20;
        }
        if (side == 2) {
            x += 23;
            y += 60;
        }
        if (side == 3) {
            x -= 1;
            y += 20;
        }
        if (side == 4) {
            x += 23;
            y -= 8;
        }
        JLabel label = getLabel(hotels, storedBuildings(hotels) - 1);
        label.setBounds(JUtils.getX(x), JUtils.getY(y), label.getWidth(), label.getHeight());
        hotels.replace(label, new Pair<>(street, buildingsOn));
    }

    private void moveHotelOffStreet(Street street) {
        int storedHotels = storedBuildings(hotels);
        int x = (storedHotels%4)*26+155;
        int y = (storedHotels/4)*30+400;
        JLabel label = getLabel(hotels, street, buildingsOn(hotels, street) - 1);
        label.setBounds(JUtils.getX(x), JUtils.getY(y), label.getWidth(), label.getHeight());
        hotels.replace(label, new Pair<>(null, storedHotels));
    }

    public void moveSkyscraperToStreet(Street street) {
        int x = Coordinate.of(street).getX();
        int y = Coordinate.of(street).getY();
        int side = 0;
        if (x == 0) side = 1;
        if (y == 60) side = 2;
        if (x == 930) side = 3;
        if (y == 990) side = 4;
        int buildingsOn = buildingsOn(skyscrapers, street);
        if (side == 1) {
            x += 67;
            y += 20;
        }
        if (side == 2) {
            x += 23;
            y += 60;
        }
        if (side == 3) {
            x -= 1;
            y += 20;
        }
        if (side == 4) {
            x += 23;
            y -= 8;
        }
        JLabel label = getLabel(skyscrapers, storedBuildings(skyscrapers) - 1);
        boolean topOrBottom = street.colorGroup.side == Direction.UP || street.colorGroup.side == Direction.DOWN;
        label.setIcon(new ImageIcon(JUtils.imageIcon("images/Main_pictures/WOK_" + (topOrBottom ? "1" : "2") + ".png").getImage().getScaledInstance(20, topOrBottom ? 32 : 40, Image.SCALE_SMOOTH)));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), label.getIcon().getIconWidth(), label.getIcon().getIconHeight());
        skyscrapers.replace(label, new Pair<>(street, buildingsOn));
    }

    private void moveSkyscraperOffStreet(Street street) {
        int storedSkyscraper = storedBuildings(skyscrapers);
        int x = (storedSkyscraper%4)*26+155;
        int y = (storedSkyscraper/4)*30+400;
        JLabel label = getLabel(skyscrapers, street, buildingsOn(skyscrapers, street) - 1);
        label.setIcon(JUtils.imageIcon("images/Main_pictures/WOK_2.png"));
        label.setBounds(JUtils.getX(x), JUtils.getY(y), label.getWidth(), label.getHeight());
        skyscrapers.replace(label, new Pair<>(null, storedSkyscraper));
    }

    private JLabel getLabel(HashMap<JLabel, Pair<Street, Integer>> map, int pos) {
        return MapUtils.key(map, map.values().stream().filter(either -> either.getLeft() == null && (either.getRight() == pos)).findFirst().orElse(null)).orElse(null);
    }

    private int storedBuildings(HashMap<JLabel, Pair<Street, Integer>> map) {
        return map.values().stream().filter(either -> either.getLeft() == null).toList().size();
    }

    private JLabel getLabel(HashMap<JLabel, Pair<Street, Integer>> map, IPurchasable purchasable, int pos) {
        return MapUtils.key(map, map.values().stream().filter(either -> either.getLeft() != null && either.getLeft().equals(purchasable) && (either.getRight() == pos)).findFirst().orElse(null)).orElse(null);
    }

    private <T extends IPurchasable> int buildingsOn(HashMap<JLabel, Pair<T, Integer>> map, IPurchasable purchasable) {
        return map.values().stream().filter(either -> either.getLeft() != null && either.getLeft().equals(purchasable)).toList().size();
    }

    public void reset() {
        setVisible(false);
    }
    public void init(){
        setVisible(true);
    }
}
