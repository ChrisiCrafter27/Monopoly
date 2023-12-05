package monopol.screen;

import monopol.data.IPurchasable;
import monopol.data.Plant;
import monopol.data.Street;
import monopol.data.TrainStation;
import monopol.utils.JUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class BoardPane extends JLayeredPane {
    Consumer<IPurchasable> selectCard = ignored -> {};

    public BoardPane() {
        super();
        setBounds(0, 0, (int) JUtils.SCREEN_WIDTH, (int) JUtils.SCREEN_HEIGHT);
        //add(label, DEFAULT_LAYER);
        addButtons();
        reset();
    }

    public void reset() {
        setVisible(false);
    }

    public void init(Consumer<IPurchasable> selectCard) {
        this.selectCard = selectCard;
        setVisible(true);
    }
    
    public void addButtons() {
        //LEFT
        addStreetButton(Street.TIERGARTENSTRASSE, 0, 150, Direction.RIGHT);
        addStreetButton(Street.POSTSTRASSE, 0, 220, Direction.RIGHT);
        addPlantButton(Plant.GASWERK, 0, 290, Direction.RIGHT);
        addEreignisfeld(0, 360, Direction.RIGHT);
        addStreetButton(Street.ELISENSTRASSE, 0, 430, Direction.RIGHT);
        addStreetButton(Street.CHAUSSESTRASSE, 0, 500, Direction.RIGHT);
        addTrainStationButton(TrainStation.SUEDBAHNHOF, 0, 570, Direction.RIGHT);
        addSteuerfeld(0, 640, Direction.RIGHT);
        addStreetButton(Street.STADIONSTRASSE, 0, 710, Direction.RIGHT);
        addStreetButton(Street.TURMSTRASSE, 0, 780, Direction.RIGHT);
        addGemeinschaftsfeld(0, 850, Direction.RIGHT);
        addStreetButton(Street.BADSTRASSE, 0, 920, Direction.RIGHT);
        //UP
        addSpecialField(90, 60, Direction.DOWN);
        addStreetButton(Street.SEESTRASSE, 160, 60, Direction.DOWN);
        addStreetButton(Street.HAFENSTRASSE, 230, 60, Direction.DOWN);
        addPlantButton(Plant.ELEKTRIZITAETSWERK, 300, 60, Direction.DOWN);
        addStreetButton(Street.NEUESTRASSE, 370, 60, Direction.DOWN);
        addStreetButton(Street.MARKTPLATZ, 440, 60, Direction.DOWN);
        addTrainStationButton(TrainStation.WESTBAHNHOF, 510, 60, Direction.DOWN);
        addStreetButton(Street.MUENCHENERSTRASSE, 580, 60, Direction.DOWN);
        addGemeinschaftsfeld(650, 60, Direction.DOWN);
        addStreetButton(Street.WIENERSTRASSE, 720, 60, Direction.DOWN);
        addStreetButton(Street.BERLINERSTRASSE, 790, 60, Direction.DOWN);
        addStreetButton(Street.HAMBURGERSTRASSE, 860, 60, Direction.DOWN);
        //RIGHT
        addStreetButton(Street.THEATERSTRASSE, 930, 150, Direction.LEFT);
        addEreignisfeld(930, 220, Direction.LEFT);
        addStreetButton(Street.MUSEUMSTRASSE, 930, 290, Direction.LEFT);
        addStreetButton(Street.OPERNPLATZ, 930, 360, Direction.LEFT);
        addStreetButton(Street.KONZERTHAUSSTRASSE, 930, 430, Direction.LEFT);
        addSpecialField(930, 500, Direction.LEFT);
        addTrainStationButton(TrainStation.NORDBAHNHOF, 930, 570, Direction.LEFT);
        addStreetButton(Street.LESSINGSTRASSE, 930, 640, Direction.LEFT);
        addStreetButton(Street.SCHILLERSTRASSE, 930, 710, Direction.LEFT);
        addPlantButton(Plant.WASSERWERK, 930, 780, Direction.LEFT);
        addStreetButton(Street.GOETHESTRASSE, 930, 850, Direction.LEFT);
        addStreetButton(Street.RILKESTRASSE, 930, 920, Direction.LEFT);
        //DOWN
        addStreetButton(Street.SCHLOSSALLEE, 90, 990, Direction.UP);
        addGemeinschaftsfeld(160, 990, Direction.UP);
        addStreetButton(Street.PARKSTRASSE, 230, 990, Direction.UP);
        addStreetButton(Street.DOMPLATZ, 300, 990, Direction.UP);
        addSpecialField(370, 990, Direction.UP);
        addSteuerfeld(440, 990, Direction.UP);
        addTrainStationButton(TrainStation.HAUPTBAHNHOF, 510, 990, Direction.UP);
        addStreetButton(Street.BAHNHOFSTRASSE, 580, 990, Direction.UP);
        addEreignisfeld(650, 990, Direction.UP);
        addStreetButton(Street.BOERSENPLATZ, 720, 990, Direction.UP);
        addStreetButton(Street.HAUPSTRASSE, 790, 990, Direction.UP);
        addStreetButton(Street.RATHAUSPLATZ, 860, 990, Direction.UP);
        //CORNERS
        add(JUtils.addImage("images/felder/gefaengnis.png", 0, 60));
        add(JUtils.addImage("images/felder/los.png", 0, 990));
        add(JUtils.addImage("images/felder/freiparken.png", 930, 60));
        add(JUtils.addImage("images/felder/ins_gefaengnis.png", 930, 990));
    }

    private void addStreetButton(Street street, int x, int y, Direction direction) {
        JButton button;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, JUtils.getX(1920), JUtils.getY(1080));
        switch (direction) {
            case LEFT -> {
                button = JUtils.addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectCard.accept(street);
                });
                ImageIcon icon = new ImageIcon("images/felder/left_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(JUtils.addImage("images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+2, y+1), 2);
                pane.add(JUtils.addRotatedText(street.name, Font.BOLD, x-5, y+2,11, -90, 66), 1);
                pane.add(JUtils.addRotatedText(street.price + "€", Font.BOLD, x+45, y+2, 13, -90, 66), 1);
            }
            case UP -> {
                button = JUtils.addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectCard.accept(street);
                });
                ImageIcon icon = new ImageIcon("images/felder/up_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(JUtils.addImage("images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+1, y+2), 2);
                pane.add(JUtils.addRotatedText(street.name, Font.BOLD, x+2, y-5,11, 0, 66), 1);
                pane.add(JUtils.addRotatedText(street.price + "€", Font.BOLD, x+2, y+45, 13, 0, 66), 1);
            }
            case RIGHT -> {
                button = JUtils.addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectCard.accept(street);
                });
                ImageIcon icon = new ImageIcon("images/felder/right_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(JUtils.addImage("images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+70, y+1), 2);
                pane.add(JUtils.addRotatedText(street.name, Font.BOLD, x+28, y+2,11, 90, 66), 1);
                pane.add(JUtils.addRotatedText(street.price + "€", Font.BOLD, x-22, y+2, 13, 90, 66), 1);
            }
            case DOWN -> {
                button = JUtils.addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectCard.accept(street);
                });
                ImageIcon icon = new ImageIcon("images/felder/down_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(JUtils.addImage("images/felder/" + street.colorGroup.IMAGE + "_cardcolor.png", x+1, y+70), 2);
                pane.add(JUtils.addRotatedText(street.name, Font.BOLD, x+2, y+28,11, 180, 66), 1);
                pane.add(JUtils.addRotatedText(street.price + "€", Font.BOLD, x+2, y-22, 13, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(button, 3);
        add(pane);
    }

    private void addTrainStationButton(TrainStation station, int x, int y, Direction direction) {
        JButton button;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, 1920, 1080);
        switch (direction) {
            case LEFT -> {
                button = JUtils.addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectCard.accept(station);
                });
                ImageIcon icon = new ImageIcon("images/felder/wide_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(JUtils.addImage("images/felder/left_train.png", x+25, y+10), 2);
                pane.add(JUtils.addRotatedText(station.name, Font.BOLD, x-25, y+2,11, -90, 66), 1);
                pane.add(JUtils.addRotatedText(station.price + "€", Font.BOLD, x+45, y+2, 13, -90, 66), 1);
            }
            case UP -> {
                button = JUtils.addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectCard.accept(station);
                });
                ImageIcon icon = new ImageIcon("images/felder/high_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(JUtils.addImage("images/felder/up_train.png", x+10, y+25), 2);
                pane.add(JUtils.addRotatedText(station.name, Font.BOLD, x+2, y-25,11, 0, 66), 1);
                pane.add(JUtils.addRotatedText(station.price + "€", Font.BOLD, x+2, y+45, 13, 0, 66), 1);
            }
            case RIGHT -> {
                button = JUtils.addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectCard.accept(station);
                });
                ImageIcon icon = new ImageIcon("images/felder/wide_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(JUtils.addImage("images/felder/right_train.png", x+25, y+10), 2);
                pane.add(JUtils.addRotatedText(station.name, Font.BOLD, x+48, y+2,11, 90, 66), 1);
                pane.add(JUtils.addRotatedText(station.price + "€", Font.BOLD, x-22, y+2, 13, 90, 66), 1);
            }
            case DOWN -> {
                button = JUtils.addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectCard.accept(station);
                });
                ImageIcon icon = new ImageIcon("images/felder/high_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(JUtils.addImage("images/felder/down_train.png", x+10, y+27), 2);
                pane.add(JUtils.addRotatedText(station.name, Font.BOLD, x+2, y+48,11, 180, 66), 1);
                pane.add(JUtils.addRotatedText(station.price + "€", Font.BOLD, x+2, y-22, 13, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(button, 3);
        add(pane);
    }

    private void addPlantButton(Plant plant, int x, int y, Direction direction) {
        JButton button;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, 1920, 1080);
        switch (direction) {
            case LEFT -> {
                button = JUtils.addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectCard.accept(plant);
                });
                ImageIcon icon = new ImageIcon("images/felder/wide_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(JUtils.addImage("images/felder/water_icon.png", x+15, y+10), 2);
                pane.add(JUtils.addRotatedText(plant.name, Font.BOLD, x-25, y+2,11, -90, 66), 1);
                pane.add(JUtils.addRotatedText(plant.price + "€", Font.BOLD, x+45, y+2, 13, -90, 66), 1);
            }
            case UP -> {
                throw new RuntimeException();
            }
            case RIGHT -> {
                button = JUtils.addButton("", x, y, 90, 70, true, actionEvent -> {
                    selectCard.accept(plant);
                });
                ImageIcon icon = new ImageIcon("images/felder/wide_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(JUtils.addImage("images/felder/gas_icon.png", x+25, y+10), 2);
                pane.add(JUtils.addRotatedText(plant.name, Font.BOLD, x+48, y+2,11, 90, 66), 1);
                pane.add(JUtils.addRotatedText(plant.price + "€", Font.BOLD, x-22, y+2, 13, 90, 66), 1);
            }
            case DOWN -> {
                button = JUtils.addButton("", x, y, 70, 90, true, actionEvent -> {
                    selectCard.accept(plant);
                });
                ImageIcon icon = new ImageIcon("images/felder/high_background.png");
                button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH)));
                pane.add(JUtils.addImage("images/felder/power_icon.png", x+10, y+20), 2);
                pane.add(JUtils.addRotatedText(plant.name, Font.BOLD, x+2, y+48,11, 180, 66), 1);
                pane.add(JUtils.addRotatedText(plant.price + "€", Font.BOLD, x+2, y-22, 13, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(button, 3);
        add(pane);
    }

    private void addEreignisfeld(int x, int y, Direction direction) {
        JLabel label;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, 1920, 1080);
        switch (direction) {
            case LEFT -> {
                label = JUtils.addImage("images/felder/wide_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/right_ereignis.png", x+30, y+10), 2);
                pane.add(JUtils.addRotatedText("Ereignisfeld", Font.BOLD, x-25, y+2,11, -90, 66), 1);
            }
            case UP -> {
                label = JUtils.addImage("images/felder/high_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/down_ereignis.png", x+10, y+25), 2);
                pane.add(JUtils.addRotatedText("Ereignisfeld", Font.BOLD, x+2, y-25,11, 0, 66), 1);
            }
            case RIGHT -> {
                label = JUtils.addImage("images/felder/wide_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/left_ereignis.png", x+10, y+10), 2);
                pane.add(JUtils.addRotatedText("Ereignisfeld", Font.BOLD, x+48, y+2,11, 90, 66), 1);
            }
            case DOWN -> {
                label = JUtils.addImage("images/felder/high_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/up_ereignis.png", x+10, y+10), 2);
                pane.add(JUtils.addRotatedText("Ereignisfeld", Font.BOLD, x+2, y+48,11, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(label, 3);
        add(pane);
    }

    private void addGemeinschaftsfeld(int x, int y, Direction direction) {
        JLabel label;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, 1920, 1080);
        switch (direction) {
            case LEFT -> {
                label = JUtils.addImage("images/felder/wide_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/left_gemeinschaft.png", x+30, y+10), 2);
                pane.add(JUtils.addRotatedText("Gemeinschaftsfeld", Font.BOLD, x-25, y+2,11, -90, 66), 1);
            }
            case UP -> {
                label = JUtils.addImage("images/felder/high_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/up_gemeinschaft.png", x+10, y+25), 2);
                pane.add(JUtils.addRotatedText("Gemeinschaftsfeld", Font.BOLD, x+2, y-25,11, 0, 66), 1);
            }
            case RIGHT -> {
                label = JUtils.addImage("images/felder/wide_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/right_gemeinschaft.png", x+10, y+10), 2);
                pane.add(JUtils.addRotatedText("Gemeinschaftsfeld", Font.BOLD, x+48, y+2,11, 90, 66), 1);
            }
            case DOWN -> {
                label = JUtils.addImage("images/felder/high_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/down_gemeinschaft.png", x+10, y+10), 2);
                pane.add(JUtils.addRotatedText("Gemeinschaftsfeld", Font.BOLD, x+2, y+48,11, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(label, 3);
        add(pane);
    }

    private void addSteuerfeld(int x, int y, Direction direction) {
        JLabel label;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, 1920, 1080);
        switch (direction) {
            case UP -> {
                label = JUtils.addImage("images/felder/high_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/down_steuer.png", x+10, y+15), 3);
                pane.add(JUtils.addRotatedText("Zusatzsteuer", Font.BOLD, x+2, y-25,11, 0, 66), 2);
                pane.add(JUtils.addRotatedText("Zahle 75€", Font.BOLD, x+2, y+45, 11, 0, 66), 1);
            }
            case RIGHT -> {
                label = JUtils.addImage("images/felder/wide_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/left_steuer.png", x+25, y+10), 3);
                pane.add(JUtils.addRotatedText("Einkommenssteuer", Font.BOLD, x+48, y+2,11, 90, 66), 2);
                pane.add(JUtils.addRotatedText("Zahle 10%", Font.BOLD, x-15, y+2, 11, 90, 66), 1);
                pane.add(JUtils.addRotatedText("oder 200€", Font.BOLD, x-25, y+2, 11, 90, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(label, 4);
        add(pane);
    }

    private void addSpecialField(int x, int y, Direction direction) {
        JLabel label;
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0, 0, 1920, 1080);
        switch (direction) {
            case LEFT -> {
                label = JUtils.addImage("images/felder/wide_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/bus.png", x+30, y+10), 2);
                pane.add(JUtils.addRotatedText("Busfahrkarte", Font.BOLD, x-25, y+2,11, -90, 66), 1);
            }
            case UP -> {
                label = JUtils.addImage("images/felder/high_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/geschenk.png", x+10, y+25), 2);
                pane.add(JUtils.addRotatedText("Geschenk", Font.BOLD, x+2, y-25,11, 0, 66), 1);
            }
            case DOWN -> {
                label = JUtils.addImage("images/felder/high_background.png", x, y);
                pane.add(JUtils.addImage("images/felder/auktion.png", x+10, y+10), 2);
                pane.add(JUtils.addRotatedText("Auktion", Font.BOLD, x+2, y+48,11, 180, 66), 1);
            }
            default -> throw new RuntimeException();
        }
        pane.add(label, 3);
        add(pane);
    }
}
