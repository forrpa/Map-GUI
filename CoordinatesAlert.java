import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class CoordinatesAlert extends Alert {

    private TextField xField = new TextField();
    private TextField yField = new TextField();

    public CoordinatesAlert() {

        super(AlertType.CONFIRMATION);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        gridPane.addRow(0, new Label("x: "), xField);
        gridPane.addRow(1, new Label("y: "), yField);

        getDialogPane().setContent(gridPane);
        setHeaderText(null);
        setTitle("Search place");
    }

    public double getXCoordinate(){
        return Double.parseDouble(xField.getText());
    }

    public double getYCoordinate(){
        return Double.parseDouble(yField.getText());
    }
}
