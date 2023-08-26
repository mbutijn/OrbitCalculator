import java.awt.*;

public class Star extends CelestialBody{
    public Star (double radius, double SOI, double mu, Color color){
        super(radius, SOI, mu, color, "sun");
        this.color = color;
    }

    public void updateMiddle(int x, int y){
        this.x_int = x;
        this.y_int = y;
    }

}
