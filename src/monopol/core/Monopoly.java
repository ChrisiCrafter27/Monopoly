package monopol.core;

public class Monopoly {
    public static final Monopoly INSTANCE = new Monopoly();

    GameState state;

    public static void main(String[] args) {
        INSTANCE.state = GameState.MAIN_MENU;
        Thread menuThread = new Thread() {
            @Override
            public void run() {
                while(!interrupted()) {
                    //Check for buttons
                    //Manage inputs, checkboxes, etc.
                    //finally interrupt this thread and start client- or server-thread
                    try {
                        sleep(10);
                    } catch (InterruptedException ignored) {}
                }
            }
        };
        menuThread.start();
    }
}