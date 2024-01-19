package monopol.client.screen;

import monopol.client.Client;
import monopol.common.utils.JUtils;

import javax.swing.*;
import java.util.function.Supplier;

public class FreeParkingPane extends JLayeredPane {
    private int money = 0;

    public FreeParkingPane() {
        super();
        setBounds(1080/2 - 175, 400, 300, 100);
        reset();
    }

    public void init() {
        setVisible(true);
    }

    public void reset() {
        setVisible(false);
    }

    public synchronized void update(int money) {
        if(this.money != money) {
            this.money = money;
            updateImages();
        }
    }

    private void updateImages() {
        removeAll();
        addFreeParkingMoney();
    }

    private void addFreeParkingMoney() {
        int y = 420+20;
        int x = 476+90+60-50;
        int rotation = 90;

        int noteWidth = 50;
        int noteHeight = 100;

        int amount = money;
        if(amount <= 0) return;
        int note1 = 0;
        int note5 = 0;
        int note10 = 0;
        int note20 = 0;
        int note50 = 0;
        int note100 = 0;
        int note500 = 0;
        int note1000 = 0;
        int noteAll = 0;
        int angle = 0;

        while (amount >= 1000) {
            amount -= 1000;
            note1000 += 1;
            noteAll += 1;
        }
        while (amount >= 500) {
            amount -= 500;
            note500 += 1;
            noteAll += 1;
        }
        while (amount >= 100) {
            amount -= 100;
            note100 += 1;
            noteAll += 1;
        }
        while (amount >= 50) {
            amount -= 50;
            note50 += 1;
            noteAll += 1;
        }
        while (amount >= 20) {
            amount -= 20;
            note20 += 1;
            noteAll += 1;
        }
        while (amount >= 10) {
            amount -= 10;
            note10 += 1;
            noteAll += 1;
        }
        while (amount >= 5) {
            amount -= 5;
            note5 += 1;
            noteAll += 1;
        }
        while (amount >= 1) {
            amount -= 1;
            note1 += 1;
            noteAll += 1;
        }

        if(noteAll == 0) return;

        x -= noteWidth / 2;
        y -= noteHeight / 2;
        angle -= ((noteAll - 1) * 5);
        angle += rotation;

        for(int i = 0; i < note1000; i++) {
            add(JUtils.addImage("images/banknotes/1000_vm.png", x, y, angle, 50, 25), DEFAULT_LAYER);
            angle += 10;
        }
        for(int i = 0; i < note500; i++) {
            add(JUtils.addImage("images/banknotes/500_vm.png", x, y, angle, 50, 25), DEFAULT_LAYER);
            angle += 10;
        }
        for(int i = 0; i < note100; i++) {
            add(JUtils.addImage("images/banknotes/100_vm.png", x, y, angle, 50, 25), DEFAULT_LAYER);
            angle += 10;
        }
        for(int i = 0; i < note50; i++) {
            add(JUtils.addImage("images/banknotes/50_vm.png", x, y, angle, 50,  25), DEFAULT_LAYER);
            angle += 10;
        }
        for(int i = 0; i < note20; i++) {
            add(JUtils.addImage("images/banknotes/20_vm.png", x, y, angle, 50, 25), DEFAULT_LAYER);
            angle += 10;
        }
        for(int i = 0; i < note10; i++) {
            add(JUtils.addImage("images/banknotes/10_vm.png", x, y, angle, 50, 25), DEFAULT_LAYER);
            angle += 10;
        }
        for(int i = 0; i < note5; i++) {
            add(JUtils.addImage("images/banknotes/5_vm.png", x, y, angle, 50, 25), DEFAULT_LAYER);
            angle += 10;
        }
        for(int i = 0; i < note1; i++) {
            add(JUtils.addImage("images/banknotes/1_vm.png", x, y, angle, 50, 25), DEFAULT_LAYER);
            angle += 10;
        }
    }
}
