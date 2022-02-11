import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class DescribedAlert extends Alert {

    private TextField nameField = new TextField();
    private TextField descriptionField = new TextField();

    public DescribedAlert(){

        super(AlertType.CONFIRMATION);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(10);
        gridPane.setHgap(5);
        setHeaderText(null);
        setTitle("Add place");

        gridPane.addRow(0, new Label("Name: "), nameField);
        gridPane.addRow(1, new Label("Description:"), descriptionField);

        getDialogPane().setContent(gridPane);

    }

    public String getName(){
        return nameField.getText();
    }

    public String getDescription(){
        return descriptionField.getText();
    }
}
