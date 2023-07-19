public class Star extends CelestialBody{

    public Star (int x, int y, double radius, double SOI, double mu){
        super(radius, SOI, mu);
        this.x_int = x;
        this.y_int = y;
    }

}
