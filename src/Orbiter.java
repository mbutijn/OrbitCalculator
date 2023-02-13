import java.awt.*;

public class Orbiter {
    private int x, y; // pixel locations

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void draw(Graphics2D g2d) {
        g2d.drawRect(x-2, y-2, 4 ,4);
    }
}
