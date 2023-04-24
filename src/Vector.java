public class Vector {
    double x, y, abs;

    Vector(double x, double y){
        this.x = x;
        this.y = y;
    }

    Vector(double radius, double angle, boolean radialCoordinates){
        this.setVectorFromRadiusAndAngle(radius, angle);
    }

    public double getX(){
        return x;
    }

    public void setX(double x){
        this.x = x;
    }

    public double getY(){
        return y;
    }

    public void setY(double y){
        this.y = y;
    }

    public double getAbs(){
        return Math.sqrt(x*x + y*y);
    }

    public void setVectorFromRadiusAndAngle(double radius, double angle){
        setX(radius * Math.cos(angle));
        setY(radius * Math.sin(angle));
        this.abs = radius;
    }

    public double getAngle() {
        return Math.atan2(y, x);
    }

    public Vector add(Vector vector){
        return new Vector(this.getX()+vector.getX(), this.getY()+vector.getY());
    }

    public Vector add(double x, double y){
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector addFromRadialCoordinates(double radius, double angle){
        return add(radius * Math.cos(angle), radius * Math.sin(angle));
    }
}
