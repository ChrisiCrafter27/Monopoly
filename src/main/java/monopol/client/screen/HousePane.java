package monopol.client.screen;

import monopol.common.data.IPurchasable;
import monopol.common.data.Street;
import monopol.common.data.TrainStation;
import monopol.common.utils.JUtils;

import javax.swing.*;
import java.util.HashMap;

public class HousePane extends JLayeredPane {
    public HashMap<JLabel, Street> houses = new HashMap<>();
    public HashMap<JLabel, Street> hotels = new HashMap<>();
    public HashMap<JLabel, Street> wolkenkratzer = new HashMap<>();
    public HashMap<JLabel, TrainStation> bahnhof = new HashMap<>();

    int Heuser = 32;
    int Hotels = 12;
    int Wolkenkratzer = 8;
    int Zugdepot = 4;
    public HousePane(){
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        setVisible(false);

        for(int i = 0;i<Heuser;i++){
            JLabel label = JUtils.addImage("images/Main_pictures/Haus.png",(i/8)*30+150,(i%8)*35+500, 20, (int) (17.5));
            add(label, DEFAULT_LAYER);
            houses.put(label, null);
        }

        setVisible(true);
    }



}
