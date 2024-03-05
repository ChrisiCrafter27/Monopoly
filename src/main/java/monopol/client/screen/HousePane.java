package monopol.client.screen;

import monopol.common.data.Street;
import monopol.common.data.TrainStation;
import monopol.common.utils.JUtils;

import javax.swing.*;
import java.util.HashMap;

public class HousePane extends JLayeredPane {
    private static final int HOUSES_COUNT = 32;
    private static final int HOTELS_COUNT = 12;
    private static final int SKYSCRAPERS_COUNT = 8;
    private static final int DEPOTS_COUNT = 4;

    public HashMap<JLabel, Street> houses = new HashMap<>();
    public HashMap<JLabel, Street> hotels = new HashMap<>();
    public HashMap<JLabel, Street> skyscrapers = new HashMap<>();
    public HashMap<JLabel, TrainStation> depots = new HashMap<>();

    public HousePane(){
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(false);

        for(int i = 0; i< HOUSES_COUNT; i++){
            JLabel label = JUtils.addImage("images/Main_pictures/Haus.png",(i/8)*30+150,(i%8)*35+500, 20, (int) (17.5));
            add(label, DEFAULT_LAYER);
            houses.put(label, null);
        }

        setVisible(true);
    }



}
