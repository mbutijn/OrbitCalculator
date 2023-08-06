import java.awt.*;

public class Star extends CelestialBody{
    public Star (int x, int y, double radius, double SOI, double mu, Color color){
        super(radius, SOI, mu, color, "sun");
        this.x_int = x;
        this.y_int = y;
        this.color = color;
    }

    public void updateMiddle(int x, int y){
        this.x_int = x;
        this.y_int = y;
    }

}
