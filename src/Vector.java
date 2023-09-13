public class Vector {
    private double x, y;
    public double abs;

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
        return new Vector(this.getX() + vector.getX(), this.getY() + vector.getY());
    }

    public Vector add(double x, double y){
        return new Vector(this.x += x, this.y += y);
    }

    public Vector subtract(Vector vector){
        return new Vector(this.getX() - vector.getX(), this.getY() - vector.getY());
    }

    public void addFromRadialCoordinates(double radius, double angle){
        this.x += radius * Math.cos(angle);
        this.y += radius * Math.sin(angle);
    }

    public double dotMultiplication(Vector vector) {
        return getX() * vector.getX() + getY() * vector.getY();
    }

    public double crossMultiplication(Vector vector){
        return getX() * vector.getY() - getY() * vector.getX();
    }
}
