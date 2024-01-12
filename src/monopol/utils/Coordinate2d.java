package monopol.utils;

public class Coordinate2d {
    private double x;
    private double y;

    public Coordinate2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate2d() {
        this(0, 0);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
