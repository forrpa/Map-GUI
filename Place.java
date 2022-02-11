import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public abstract class Place extends Polygon {

    private String name;
    private Position position;
    private boolean marked = false;
    private String category;
    protected String type;
    protected String description;

    public Place(String name, String category, Position position){
        super(position.getX() - 15, position.getY() - 30, position.getX() + 15, position.getY() - 30, position.getX(), position.getY());
        this.name = name;
        this.category = category;
        this.position = position;
        color();
    }

    public void color(){
        switch (category) {
            case "Bus":
                setFill(Color.RED);
                setStroke(Color.RED);
                break;
            case "Underground":
                setFill(Color.BLUE);
                setStroke(Color.BLUE);
                break;
            case "Train":
                setFill(Color.GREEN);
                setStroke(Color.GREEN);
                break;
            default:
                setFill(Color.BLACK);
                break;
        }
    }

    public String getName(){
        return name;
    }

    public double getX() {return position.getX();}

    public String getType() {return type;}

    public double getY() {return position.getY();}

    public String getCategory(){
        return category;
    }

    public Position getPosition(){
        return position;
    }

    public String getDescription(){
        return description;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean on){
        marked = on;
    }

    @Override
    public String toString(){
        return getName() + "[" + getPosition() + "]";
    }
}
