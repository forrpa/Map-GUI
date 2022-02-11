public class DescribedPlace extends Place {

    public DescribedPlace(String name, String description, String category, Position position){
        super(name, category, position);
        this.description = description;
        type = "Described";
    }
}
