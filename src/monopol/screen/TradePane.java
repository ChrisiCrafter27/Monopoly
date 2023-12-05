package monopol.screen;

import monopol.client.Client;
import monopol.client.TradeState;
import monopol.data.*;
import monopol.utils.JUtils;

import javax.swing.*;
import java.util.ArrayList;

public class TradePane extends JLayeredPane {

    public TradePane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        //add(label, DEFAULT_LAYER);
        
        
        
        reset();
    }

    public void reset() {
        setVisible(false);
    }

    public boolean isOwner(IPurchasable card) {
        return false;
    }

    private void addTradeButtons(int x, int y) {
        JButton button;
        button = new JButton();
        button.setSelectedIcon(new ImageIcon("images/kleine_karten/" + "brown" + "_filled.png"));
        button.setDisabledIcon(new ImageIcon("images/kleine_karten/disabled.png"));
        add(JUtils.addButton(button, "", "images/kleine_karten/" + "brown" + ".png", x+15, y, 20, 40, Street.BADSTRASSE.getOwner().equals(name), client.tradeData.offerCards.contains(Street.BADSTRASSE), actionEvent -> {
            if(client.tradeData.offerCards.contains(Street.BADSTRASSE)) client.tradeData.offerCards.remove(Street.BADSTRASSE); else client.tradeData.offerCards.add(Street.BADSTRASSE);
            client.tradeData.tradeState = TradeState.SEND_OFFER;
        }), 0);

        for(IPurchasable card : Field.getAll().stream().filter(card -> card instanceof IPurchasable).map(card -> ((IPurchasable) card)).toList()) {

        }
    }

    private String getImage(int id) {
        if(id <= 3) {
            return "images/kleine_karten/" + "brown" + ".png";
        } else if(id <= 7) {
            return "images/kleine_karten/" + "cyan" + ".png";
        } else if(id <= 11) {
            return "images/kleine_karten/" + "pink" + ".png";
        } else if(id <= 15) {
            return "images/kleine_karten/" + "orange" + ".png";
        } else if(id <= 19) {
            return "images/kleine_karten/" + "red" + ".png";
        } else if(id <= 23) {
            return "images/kleine_karten/" + "yellow" + ".png";
        } else if(id <= 27) {
            return "images/kleine_karten/" + "green" + ".png";
        } else if(id <= 30) {
            return "images/kleine_karten/" + "blue" + ".png";
        } else if(id <= 34) {
            return "images/kleine_karten/" + "train" + ".png";
        } else if(id == 35) {
            return "images/kleine_karten/" + "gas" + ".png";
        } else if(id == 36) {
            return "images/kleine_karten/" + "elec" + ".png";
        } else if(id == 37) {
            return "images/kleine_karten/" + "water" + ".png";
        } else return "";
    }

    private int getX(int id) {
        int toReturn = 30 * id;
        if(id <= 3) {
            return toReturn - 15;
        } else if(id <= 7) {
            return toReturn - 15 + 10;
        } else if(id <= 11) {
            return toReturn - 15 + 20;
        } else if(id <= 15) {
            return toReturn;
        } else if(id <= 19) {
            return toReturn + 10;
        } else if(id <= 23) {
            return toReturn + 20;
        } else if(id <= 27) {
            return toReturn + 80;
        } else if(id <= 30) {
            return toReturn + 80 + 10;
        } else if(id <= 34) {
            return toReturn + 80;
        } else if(id <= 37) {
            return toReturn + 80 + 10;
        } else return 0;
    }

    private int getY(int id) {
        if(id <= 11) {
            return 0;
        } else if(id <= 23) {
            return 50;
        } else if(id <= 30) {
            return 100;
        } else if(id <= 37) {
            return 150;
        } else return 0;
    }

    private void addTradeInfo(PrototypeJUtils JUtils, ArrayList<IPurchasable> tradeItems, String name, int x, int y) {
        add(JUtils.addImage(Street.BADSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.BADSTRASSE) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png" : "images/kleine_karten/disabled.png", x+15, y), 0);
        add(JUtils.addImage(Street.TURMSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.TURMSTRASSE) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png" : "images/kleine_karten/disabled.png", x+45, y), 0);
        add(JUtils.addImage(Street.STADIONSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.STADIONSTRASSE) ? "images/kleine_karten/brown_filled.png" : "images/kleine_karten/brown.png" : "images/kleine_karten/disabled.png", x+75, y), 0);
        add(JUtils.addImage(Street.CHAUSSESTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.CHAUSSESTRASSE) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png" : "images/kleine_karten/disabled.png", x+115, y), 0);
        add(JUtils.addImage(Street.ELISENSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.ELISENSTRASSE) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png" : "images/kleine_karten/disabled.png", x+145, y), 0);
        add(JUtils.addImage(Street.POSTSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.POSTSTRASSE) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png" : "images/kleine_karten/disabled.png", x+175, y), 0);
        add(JUtils.addImage(Street.TIERGARTENSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.TIERGARTENSTRASSE) ? "images/kleine_karten/cyan_filled.png" : "images/kleine_karten/cyan.png" : "images/kleine_karten/disabled.png", x+205, y), 0);
        add(JUtils.addImage(Street.SEESTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.SEESTRASSE) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png" : "images/kleine_karten/disabled.png", x+245, y), 0);
        add(JUtils.addImage(Street.HAFENSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.HAFENSTRASSE) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png" : "images/kleine_karten/disabled.png", x+275, y), 0);
        add(JUtils.addImage(Street.NEUESTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.NEUESTRASSE) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png" : "images/kleine_karten/disabled.png", x+305, y), 0);
        add(JUtils.addImage(Street.MARKTPLATZ.getOwner().equals(name) ? tradeItems.contains(Street.MARKTPLATZ) ? "images/kleine_karten/pink_filled.png" : "images/kleine_karten/pink.png" : "images/kleine_karten/disabled.png", x+335, y), 0);

        add(JUtils.addImage(Street.MUENCHENERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.MUENCHENERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x, y+50), 0);
        add(JUtils.addImage(Street.WIENERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.WIENERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x+30, y+50), 0);
        add(JUtils.addImage(Street.BERLINERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.BERLINERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x+60, y+50), 0);
        add(JUtils.addImage(Street.HAMBURGERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.HAMBURGERSTRASSE) ? "images/kleine_karten/orange_filled.png" : "images/kleine_karten/orange.png" : "images/kleine_karten/disabled.png", x+90, y+50), 0);
        add(JUtils.addImage(Street.THEATERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.THEATERSTRASSE) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+130, y+50), 0);
        add(JUtils.addImage(Street.MUSEUMSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.MUSEUMSTRASSE) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+160, y+50), 0);
        add(JUtils.addImage(Street.OPERNPLATZ.getOwner().equals(name) ? tradeItems.contains(Street.OPERNPLATZ) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+190, y+50), 0);
        add(JUtils.addImage(Street.KONZERTHAUSSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.KONZERTHAUSSTRASSE) ? "images/kleine_karten/red_filled.png" : "images/kleine_karten/red.png" : "images/kleine_karten/disabled.png", x+220, y+50), 0);
        add(JUtils.addImage(Street.LESSINGSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.LESSINGSTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+260, y+50), 0);
        add(JUtils.addImage(Street.SCHILLERSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.SCHILLERSTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+290, y+50), 0);
        add(JUtils.addImage(Street.GOETHESTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.GOETHESTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+320, y+50), 0);
        add(JUtils.addImage(Street.RILKESTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.RILKESTRASSE) ? "images/kleine_karten/yellow_filled.png" : "images/kleine_karten/yellow.png" : "images/kleine_karten/disabled.png", x+350, y+50), 0);

        add(JUtils.addImage(Street.RATHAUSPLATZ.getOwner().equals(name) ? tradeItems.contains(Street.RATHAUSPLATZ) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png" : "images/kleine_karten/disabled.png", x+80, y+100), 0);
        add(JUtils.addImage(Street.HAUPSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.HAUPSTRASSE) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png" : "images/kleine_karten/disabled.png", x+110, y+100), 0);
        add(JUtils.addImage(Street.BOERSENPLATZ.getOwner().equals(name) ? tradeItems.contains(Street.BOERSENPLATZ) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png" : "images/kleine_karten/disabled.png", x+140, y+100), 0);
        add(JUtils.addImage(Street.BAHNHOFSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.BAHNHOFSTRASSE) ? "images/kleine_karten/green_filled.png" : "images/kleine_karten/green.png" : "images/kleine_karten/disabled.png", x+170, y+100), 0);
        add(JUtils.addImage(Street.DOMPLATZ.getOwner().equals(name) ? tradeItems.contains(Street.DOMPLATZ) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png" : "images/kleine_karten/disabled.png", x+210, y+100), 0);
        add(JUtils.addImage(Street.PARKSTRASSE.getOwner().equals(name) ? tradeItems.contains(Street.PARKSTRASSE) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png" : "images/kleine_karten/disabled.png", x+240, y+100), 0);
        add(JUtils.addImage(Street.SCHLOSSALLEE.getOwner().equals(name) ? tradeItems.contains(Street.SCHLOSSALLEE) ? "images/kleine_karten/blue_filled.png" : "images/kleine_karten/blue.png" : "images/kleine_karten/disabled.png", x+270, y+100), 0);

        add(JUtils.addImage(TrainStation.SUEDBAHNHOF.getOwner().equals(name) ? tradeItems.contains(TrainStation.SUEDBAHNHOF) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png" : "images/kleine_karten/disabled.png", x+80, y+150), 0);
        add(JUtils.addImage(TrainStation.WESTBAHNHOF.getOwner().equals(name) ? tradeItems.contains(TrainStation.WESTBAHNHOF) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png" : "images/kleine_karten/disabled.png", x+110, y+150), 0);
        add(JUtils.addImage(TrainStation.NORDBAHNHOF.getOwner().equals(name) ? tradeItems.contains(TrainStation.NORDBAHNHOF) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png" : "images/kleine_karten/disabled.png", x+140, y+150), 0);
        add(JUtils.addImage(TrainStation.HAUPTBAHNHOF.getOwner().equals(name) ? tradeItems.contains(TrainStation.HAUPTBAHNHOF) ? "images/kleine_karten/train_filled.png" : "images/kleine_karten/train.png" : "images/kleine_karten/disabled.png", x+170, y+150), 0);
        add(JUtils.addImage(Plant.GASWERK.getOwner().equals(name) ? tradeItems.contains(Plant.GASWERK) ? "images/kleine_karten/gas_filled.png" : "images/kleine_karten/gas.png" : "images/kleine_karten/disabled.png", x+210, y+150), 0);
        add(JUtils.addImage(Plant.ELEKTRIZITAETSWERK.getOwner().equals(name) ? tradeItems.contains(Plant.ELEKTRIZITAETSWERK) ? "images/kleine_karten/elec_filled.png" : "images/kleine_karten/elec.png" : "images/kleine_karten/disabled.png", x+240, y+150), 0);
        add(JUtils.addImage(Plant.WASSERWERK.getOwner().equals(name) ? tradeItems.contains(Plant.WASSERWERK) ? "images/kleine_karten/water_filled.png" : "images/kleine_karten/water.png" : "images/kleine_karten/disabled.png", x+270, y+150), 0);
    }
}
