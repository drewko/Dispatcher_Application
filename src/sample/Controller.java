package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller {

    ExecutorService threadPool = Executors.newWorkStealingPool();


    public ListView listView;
    public Button button;
    public TextField text;
    public ImageView loader;

    @FXML
    public void initialize() {
        loader.setVisible(false);
    }


    public static final String ID = "bB3VUSqETrjqixgsfp6l";
    public static final String CODE = "ajOsRiWYp44kvThQz0YjoQ";

    List<Ambulance> freeAmbulances = new ArrayList<>();
    String selectedId;
    Ambulance selectedAmbulance;

    static double[] coordinates = new double[2];

    public void updateAmbulanceList(double x1, double x2) {
        freeAmbulances.clear();
        freeAmbulances.add(new Ambulance("0", 50.077797, 19.924892));
        freeAmbulances.add(new Ambulance("1", 50.244883, 23.130384));
        freeAmbulances.add(new Ambulance("2", 52.406376, 16.925167));
        freeAmbulances.add(new Ambulance("3", 54.352024, 18.646639));
        freeAmbulances.add(new Ambulance("4", 52.229675, 21.012230));

        for (Ambulance car : freeAmbulances) {
            double[] result;
            try {
                result = calculateDistanceAndTime(car.getLatitude(), car.getLongtitude(), x1, x2);
                System.out.println(result[0] + " " + result[1]);

                car.setDistance(result[0]);
                car.setTime((int) result[1]);
                System.out.println("Distance set");
            } catch (IOException e) {
                System.out.println("Wrong adress");
            }
        }
        freeAmbulances.sort(new Comparator<Ambulance>() {
            @Override
            public int compare(Ambulance o1, Ambulance o2) {
                return (int) (o1.getTime() - o2.getTime());
            }
        });
    }

    public void updateList() {

        ObservableList<Ambulance> items = FXCollections.observableArrayList(freeAmbulances);
        listView.setItems(items);

    }


    public void onClickListView() {
        try {
            selectedId = listView.getSelectionModel().getSelectedItems().get(0).toString().split(" ")[1];
        } catch (Exception e) {
            System.out.println("Taking prev index");
        }


        for (Ambulance car : freeAmbulances) {
            if (car.getId().equals(selectedId))
                selectedAmbulance = car;
        }

        System.out.println("Selected ambulance id: " + selectedId);
    }

    public void onClickButton() throws IOException {
        Thread thread = new Thread(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String adress = text.getText();

                try {
                    coordinates = parse(adress);
                } catch (JSONException e) {
                    System.out.println("JSON ERROR");
                }

                System.out.println("Ambulance nr " + selectedId + " ahead " + adress + " " + coordinates[0] + " " + coordinates[1]);

                for (int i = 0; i < freeAmbulances.size(); i++) {
                    if (freeAmbulances.get(i).getId().equals(selectedId)) {
                        freeAmbulances.remove(i);
                    }
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        updateList();
                        listView.setItems(null);

                    }
                });
                return null;
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void onClickFindButton() {
        coordinates[0] = 0.0;
        coordinates[1] = 0.0;
        loader.setVisible(true);
        listView.setItems(null);


        Thread thread = new Thread(new Task<Void>() {
            @Override
            public Void call() throws IOException {
                String adress = text.getText();
                try {
                    coordinates = parse(adress);
                } catch (JSONException e) {
                    System.out.println("JSON ERROR");
                    coordinates[0] = 0.0;
                    coordinates[1] = 0.0;
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        listView.setItems(null);
                        freeAmbulances.clear();
                        if (!(coordinates[0] == 0.0) && !(coordinates[1] == 0.0)) {
                            updateAmbulanceList(coordinates[0], coordinates[1]);
                        }
                        updateList();
                        System.out.println("List updated");
                        loader.setVisible(false);

                    }
                });

                return null;
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void showAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Wrong adress");
        alert.setContentText("Please check if entered adress is correct.");
        alert.showAndWait();
    }


    public static double[] parse(String adress) throws IOException, JSONException {
        double[] result = new double[2];
        adress = adress.replace(' ', '+');


        URL url = new URL("https://geocoder.api.here.com/6.2/geocode.json?" + "app_id=" + ID + "&app_code=" + CODE + "&searchtext=" + "polska" + "+" + adress);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        JSONObject obj = new JSONObject(content.toString());
        Double x1 = obj.getJSONObject("Response").getJSONArray("View").getJSONObject(0).getJSONArray("Result").getJSONObject(0).getJSONObject("Location").getJSONArray("NavigationPosition").getJSONObject(0).getDouble("Latitude");
        Double x2 = obj.getJSONObject("Response").getJSONArray("View").getJSONObject(0).getJSONArray("Result").getJSONObject(0).getJSONObject("Location").getJSONArray("NavigationPosition").getJSONObject(0).getDouble("Longitude");
        System.out.println(x1 + " " + x2);
        result[0] = x1;
        result[1] = x2;
        return result;
    }

    public double[] calculateDistanceAndTime(double a1, double a2, double b1, double b2) throws IOException, JSONException {
        //50.077797,19.924892
        //50.081727, 19.915362

        double[] result = new double[2];
        double distance = 0.0;
        int time = 0;
        URL url = new URL("https://route.api.here.com/routing/7.2/calculateroute.json?&app_id=" + ID + "&app_code=" + CODE + "&waypoint0=geo!" + a1 + "," + a2 + "&waypoint1=geo!" + b1 + "," + b2 + "&mode=fastest;car;traffic:enabled");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        connection.disconnect();
        JSONObject obj = new JSONObject(content.toString());
        distance = obj.getJSONObject("response").getJSONArray("route").getJSONObject(0).getJSONObject("summary").getDouble("distance");
        time = obj.getJSONObject("response").getJSONArray("route").getJSONObject(0).getJSONObject("summary").getInt("trafficTime");
        result[0] = distance / 1000;
        result[1] = time;
        return result;


    }

    public static Popup createPopup(final String message) {
        final Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        Label label = new Label(message);
        label.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                popup.hide();
            }
        });
        label.getStylesheets().add("/css/styles.css");
        label.getStyleClass().add("popup");
        popup.getContent().add(label);
        return popup;
    }

    public static void showPopupMessage(final String message, final Stage stage) {
        final Popup popup = createPopup(message);
        popup.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                popup.setX(stage.getX() + stage.getWidth() / 2 - popup.getWidth() / 2);
                popup.setY(stage.getY() + stage.getHeight() / 2 - popup.getHeight() / 2);
            }
        });
        popup.show(stage);
    }
}
