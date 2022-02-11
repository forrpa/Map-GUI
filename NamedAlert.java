import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class NamedAlert extends Alert {

    private TextField nameField = new TextField();

    public NamedAlert(){
        super(AlertType.CONFIRMATION);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10));
        grid.setHgap(5);
        grid.setVgap(10);
        setHeaderText(null);
        setTitle("Add place");

        grid.addRow(0, new Label("Name:"), nameField);
        getDialogPane().setContent(grid);
    }

    public String getName(){
        return nameField.getText();
    }

}
