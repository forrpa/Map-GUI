import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.*;
import java.util.*;

public class Program extends Application {

    private Stage primaryStage;

    private Map<Position, Place> placeMap = new HashMap<>();
    private Map<String, Set<Place>> nameMap = new HashMap<>();
    private Set<Place> markedPlaces = new HashSet<>();
    private Map<String, Set<Place>> categoryMap = new HashMap<>();

    private boolean changed = false;
    private ListView<String> listView;
    private TextField searchField;
    private RadioButton describedBtn;
    private ImageView imageView = new ImageView();
    private Pane pane = new Pane();
    private Button newBtn;
    private NewPlaceHandler newHandler = new NewPlaceHandler();


    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        BorderPane root = new BorderPane();
        primaryStage.setTitle("Inlämningsuppgift 2");
        root.setStyle("-fx-font-size: 14");
        root.setStyle("-fx-font-weight: bold");

        FlowPane flowPane = new FlowPane();
        flowPane.setPadding(new Insets(8));
        flowPane.setVgap(8);
        flowPane.setHgap(8);
        flowPane.setAlignment(Pos.CENTER);
        flowPane.setPrefHeight(80);

        newBtn = new Button("New");
        newBtn.setOnAction(new NewHandler());
        flowPane.getChildren().addAll(newBtn);

        RadioButton namedBtn = new RadioButton("Named");
        describedBtn = new RadioButton("Described");
        ToggleGroup toggleGroup = new ToggleGroup();
        namedBtn.setToggleGroup(toggleGroup);
        describedBtn.setToggleGroup(toggleGroup);
        toggleGroup.selectToggle(namedBtn);

        VBox vButtons = new VBox();
        vButtons.setSpacing(8);
        vButtons.getChildren().addAll(namedBtn, describedBtn);
        flowPane.getChildren().add(vButtons);

        searchField = new TextField("Search");
        searchField.setMaxHeight(2);
        searchField.setMaxWidth(200);
        searchField.setEditable(true);
        searchField.setOnMouseClicked(e -> searchField.clear());
        flowPane.getChildren().add(searchField);

        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(new SearchHandler());
        Button hideBtn = new Button("Hide");
        hideBtn.setOnAction(new HideHandler());
        Button removeBtn = new Button("Remove");
        removeBtn.setOnAction(new RemoveHandler());
        Button coordBtn = new Button("Coordinates");
        coordBtn.setOnAction(new CoordinateHandler());
        flowPane.getChildren().addAll(searchBtn, hideBtn, removeBtn, coordBtn);

        MenuBar menuBar = new MenuBar();
        VBox top = new VBox();
        top.getChildren().add(menuBar);
        Menu menu = new Menu("File");
        MenuItem loadMap = new MenuItem("Load Map");
        loadMap.setOnAction(new ImageHandler());
        MenuItem loadPlaces = new MenuItem("Load Places");
        loadPlaces.setOnAction(new LoadHandler());
        MenuItem save = new MenuItem("Save");
        save.setOnAction(new SaveHandler());
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST)));
        menu.getItems().addAll(loadMap, loadPlaces, save, exit);
        menuBar.getMenus().add(menu);
        top.getChildren().add(flowPane);
        root.setTop(top);

        VBox right = new VBox();
        right.setAlignment(Pos.CENTER);
        right.setSpacing(2);
        right.setPrefWidth(200);
        root.setRight(right);

        Label label = new Label("Categories");
        listView = new ListView<>();
        listView.setPadding(new Insets(2));
        listView.setPrefHeight(100);
        listView.getItems().addAll("Bus", "Underground", "Train");
        listView.getSelectionModel().selectedItemProperty().addListener(new ListHandler());

        Button hideAllBtn = new Button("Hide Category");
        hideAllBtn.setOnAction(new CategoryHandler());
        right.getChildren().addAll(label, listView, hideAllBtn);

        root.setCenter(new ScrollPane(pane));
        pane.setPrefSize(600, 400);
        Scene scene = new Scene(root);
        primaryStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new ExitHandler());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    class ImageHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {

            if (saveAlert()) {
                return;
            }
            removePlaces();
            changed = false;

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load map");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image files", "*.jpg", "*.png"),
                    new FileChooser.ExtensionFilter("All files", "*.*")
            );
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                String fileName = file.getAbsolutePath();
                Image image = new Image("file:" + fileName);
                double height = image.getHeight();
                double width = image.getWidth();
                pane.setPrefSize(width, height);
                imageView.setImage(image);
                pane.getChildren().add(imageView);
                primaryStage.sizeToScene();
            }
        }
    }

    class LoadHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {

            if (saveAlert()) {
                return;
            }
            removePlaces();
            changed = false;

            try {
                if (pane.getChildren().contains(imageView)) {
                    String fileName = newFileChooser();
                    if (fileName != null) {
                        FileReader inFile = new FileReader(fileName);
                        BufferedReader in = new BufferedReader(inFile);
                        String line;
                        while ((line = in.readLine()) != null) {
                            String[] tokens = line.split(",");
                            String type = tokens[0];
                            String category = tokens[1];
                            double x = Double.parseDouble(tokens[2]);
                            double y = Double.parseDouble(tokens[3]);
                            Position position = new Position(x, y);
                            String name = tokens[4].trim();
                            if (type.equals("Named")) {
                                Place place = new NamedPlace(name, category, position);
                                addPlace(position, place, name, category);
                            } else {
                                String description = tokens[5].trim();
                                Place place = new DescribedPlace(name, description, category, position);
                                addPlace(position, place, name, category);
                            }
                        }
                        in.close();
                        inFile.close();
                    }
                } else {
                    newAlert("Error: A map must be loaded before loading places!");
                }
            } catch (FileNotFoundException e) {
                newAlert(e.getMessage());
            } catch (IOException e) {
                newAlert(e.getMessage());
            }
        }
    }

    class SaveHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            try {
                String fileName = newFileChooser();
                if (fileName != null) {
                    FileWriter outfile = new FileWriter(fileName);
                    PrintWriter out = new PrintWriter(outfile);
                    for (Place p : placeMap.values()) {
                        if (p.getType().equals("Named")) {
                            out.println(p.getType() + "," + p.getCategory() + "," + p.getX() + "," + p.getY() + "," + p.getName());
                        } else {
                            out.println(p.getType() + "," + p.getCategory() + "," + p.getX() + "," + p.getY() + "," + p.getName() + "," + p.getDescription());
                        }
                    }
                    out.close();
                    outfile.close();
                    changed = false;
                }
            } catch (FileNotFoundException e) {
                newAlert(e.getMessage());
            } catch (IOException e) {
                newAlert(e.getMessage());
            }
        }
    }

    class NewHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (pane.getChildren().contains(imageView)) {
                pane.addEventHandler(MouseEvent.MOUSE_CLICKED, newHandler);
                pane.setCursor(Cursor.CROSSHAIR);
                newBtn.setDisable(true);
            }
        }
    }

    class NewPlaceHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {

            double x = event.getX();
            double y = event.getY();
            Position position = new Position(x, y);
            if (placeMap.keySet().contains(position)) {
                newAlert("Error: There is already a place in this location!");
            } else {
                if (describedBtn.isSelected()) {
                    DescribedAlert dialog = new DescribedAlert();
                    Optional<ButtonType> result = dialog.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        String name = dialog.getName().trim();
                        if (name.trim().isEmpty()) {
                            newAlert("Error: Name can't be empty!");
                        } else {
                            String description = dialog.getDescription().trim();
                            if (description.trim().isEmpty()) {
                                newAlert("Error: Description can't be empty!");
                            } else {
                                String category = "None";
                                if (listView.getSelectionModel().getSelectedItem() != null) {
                                    category = listView.getSelectionModel().getSelectedItem();
                                }
                                DescribedPlace describedPlace = new DescribedPlace(name, description, category, position);
                                addPlace(position, describedPlace, name, category);
                            }
                        }
                    }
                } else {
                    NamedAlert dialog = new NamedAlert();
                    Optional<ButtonType> result = dialog.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        String name = dialog.getName().trim();
                        if (name.trim().isEmpty()) {
                            newAlert("Error: Name can't be empty!");
                        } else {
                            String category = "None";
                            if (listView.getSelectionModel().getSelectedItem() != null) {
                                category = listView.getSelectionModel().getSelectedItem();
                            }
                            NamedPlace namedPlace = new NamedPlace(name, category, position);
                            addPlace(position, namedPlace, name, category);
                        }
                    }
                }
            }
            changed = true;
            listView.getSelectionModel().clearSelection();
            pane.removeEventHandler(MouseEvent.MOUSE_CLICKED, newHandler);
            pane.setCursor(Cursor.DEFAULT);
            newBtn.setDisable(false);
        }
    }

    class SearchHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            String name = searchField.getText().trim();
            if (!name.trim().isEmpty()) {
                unmarkPlace();
                searchField.clear();
                Set<Place> sameName = nameMap.get(name);
                if (sameName != null) {
                    for (Place p : sameName) {
                        markPlace(p);
                    }
                }
            }
        }
    }

    class HideHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            Iterator<Place> iterator = markedPlaces.iterator();
            while (iterator.hasNext()) {
                Place p = iterator.next();
                p.setMarked(false);
                p.setVisible(false);
                p.color();
                iterator.remove();
            }
        }
    }

    class RemoveHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {

            Iterator<Place> marked = markedPlaces.iterator();
            while (marked.hasNext()) {
                Place p = marked.next();
                Collection<Place> places = placeMap.values();
                places.removeIf(Place::isMarked);
                String name = p.getName();
                String category = p.getCategory();
                removeFromMap(category, categoryMap);
                removeFromMap(name, nameMap);
                pane.getChildren().remove(p);
                marked.remove();
            }
            changed = true;
        }

        private void removeFromMap(String name, Map<String, Set<Place>> nameMap) {
            Set<Place> names = nameMap.get(name);
            if (names != null) {
                names.removeIf(Place::isMarked);
                if (names.isEmpty()) {
                    nameMap.remove(name);
                }
            }
        }
    }

    class CoordinateHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            try {
                CoordinatesAlert dialog = new CoordinatesAlert();
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    double x = dialog.getXCoordinate();
                    double y = dialog.getYCoordinate();
                    Position position = new Position(x, y);
                    if (placeMap.keySet().contains(position)) {
                        Place place = placeMap.get(position);
                        unmarkPlace();
                        if (!place.isMarked()) {
                            markPlace(place);
                        }
                    } else {
                        newAlert("Error: There is no place with these coordinates!");
                    }
                }
            } catch (NumberFormatException e) {
                newAlert("Error: Enter numbers only!");
            }
        }
    }

    class ListHandler implements ChangeListener<String> {
        @Override
        public void changed(ObservableValue obs, String old, String nev) {
            Set<Place> places = categoryMap.get(nev);
            if (places != null) {
                for (Place p : places) {
                    p.setVisible(true);
                }
            }
        }
    }

    class ClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {

            Place place = (Place) event.getSource();
            if (event.getButton() == MouseButton.PRIMARY) {
                if (!place.isMarked()) {
                    markPlace(place);
                } else {
                    place.setMarked(false);
                    place.color();
                    markedPlaces.remove(place);
                }
            } else if (event.getButton() == MouseButton.SECONDARY) {
                printInfo(place);
            }
        }

        private void printInfo(Place place) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            if (place.getType().equals("Named")) {
                alert.setHeaderText(null);
                alert.setContentText(place.toString());
            } else {
                alert.setHeaderText(place.toString());
                alert.setContentText(place.getDescription());
            }
            alert.showAndWait();
        }
    }

    class CategoryHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            String category = listView.getSelectionModel().getSelectedItem();
            Set<Place> places = categoryMap.get(category);
            if (places != null) {
                for (Place place : places) {
                    place.setVisible(false);
                    place.setMarked(false);
                }
                listView.getSelectionModel().clearSelection();
            }
        }
    }

    class ExitHandler implements EventHandler<WindowEvent> {
        @Override
        public void handle(WindowEvent event) {
            if (saveAlert()) {
                event.consume();
            }
        }
    }

    private boolean saveAlert() {
        if (changed) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Do you want to exit without saving?");
            alert.setHeaderText(null);
            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.CANCEL;
        }
        return false;
    }

    private String newFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text files", "*.txt"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file == null)
            return null;
        else {
            String fileName = file.getAbsolutePath();
            return fileName;
        }
    }

    private void markPlace(Place p) {
        p.setMarked(true);
        p.setVisible(true);
        p.setFill(Color.PINK);
        p.setStroke(Color.BLACK);
        markedPlaces.add(p);
    }

    private void addPlace(Position position, Place place, String name, String category) {
        placeMap.put(position, place);
        Set<Place> namePlaces = nameMap.computeIfAbsent(name, k -> new HashSet<>());
        namePlaces.add(place);
        Set<Place> categoryPlaces = categoryMap.computeIfAbsent(category, k -> new HashSet<>());
        categoryPlaces.add(place);
        place.setOnMouseClicked(new ClickHandler());
        pane.getChildren().add(place);

    }

    private void unmarkPlace() {
        Iterator<Place> iterator = markedPlaces.iterator();
        while (iterator.hasNext()) {
            Place p = iterator.next();
            p.setMarked(false);
            p.color();
            iterator.remove();
        }
    }

    private void newAlert(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setTitle("Error");
        alert.setContentText(text);
        alert.showAndWait();
    }

    private void removePlaces() {
        Collection<Place> places = placeMap.values();
        Iterator<Place> iter = places.iterator();
        while (iter.hasNext()) {
            Place place = iter.next();
            pane.getChildren().remove(place);
            removeAll(nameMap);
            removeAll(categoryMap);
            Iterator<Place> marked = markedPlaces.iterator();
            while (marked.hasNext()) {
                Place p = marked.next();
                marked.remove();
            }
            iter.remove();
        }
    }

    private void removeAll(Map<String, Set<Place>> nameMap) {
        Collection<Set<Place>> names = nameMap.values();
        Iterator<Set<Place>> iterator = names.iterator();
        while (iterator.hasNext()) {
            Set<Place> place = iterator.next();
            iterator.remove();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}

