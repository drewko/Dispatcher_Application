package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Controller {

    public ListView listView;
    public Button button;
    public TextField text;


    @FXML
    public void initialize()
    {
//        Timer timer = new Timer();
//        TimerTask task = new TimerTask()
//        {
//            public void run()
//            {
//
//            }
//
//        };
//        timer.scheduleAtFixedRate(task,0,2000l);

    }

    public static final String ID="bB3VUSqETrjqixgsfp6l";
    public static final String CODE="ajOsRiWYp44kvThQz0YjoQ";

    List<Ambulance> freeAmbulances=new ArrayList<>();
    String selectedId;
    Ambulance selectedAmbulance;

    public void updateAmbulanceList(double x1,double x2)
    {
        freeAmbulances.clear();
        freeAmbulances.add(new Ambulance("0",50.077797,19.924892));
        freeAmbulances.add(new Ambulance("1",50.244883,23.130384));
        freeAmbulances.add(new Ambulance("2",52.406376,16.925167));
        freeAmbulances.add(new Ambulance("3",54.352024,18.646639));
        freeAmbulances.add(new Ambulance("4",52.229675,21.012230));

        for(Ambulance car:freeAmbulances)
        {
            double [] result;
            try {
                result=calculateDistanceAndTime(car.getLatitude(),car.getLongtitude(),x1,x2);
                System.out.println(result[0]+" "+result[1]);

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
                return (int) (o1.getDistance() - o2.getDistance());
            }
        });
    }

    public  void updateList() {
               ObservableList<Ambulance> items= FXCollections.observableArrayList(freeAmbulances);
        listView.setItems(items);
    }


    public void onClickListView()
    {
        selectedId=listView.getSelectionModel().getSelectedItems().get(0).toString().split(" ")[1];

        for(Ambulance car:freeAmbulances)
        {
            if(car.getId().equals(selectedId))
                selectedAmbulance=car;
        }

        System.out.println("Selected ambulance id: "+selectedId);
    }

    public void onClickButton() {
        String adress=text.getText();
        double [] coordinates=new double[2];
        try {
            coordinates=parse(adress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            System.out.println("Wrong adress");
        }
        for(int i=0; i<freeAmbulances.size();i++)
        {
            if(freeAmbulances.get(i).getId().equals(selectedId))
            {
                freeAmbulances.remove(i);
            }
        }
        updateList();


        System.out.println("Ambulance nr "+selectedId+" ahead "+adress+" "+coordinates[0]+" "+coordinates[1]);
    }
    public void onClickFindButton()
    {
        String adress=text.getText();
        double [] coordinates=new double[2];
        try {
            coordinates=parse(adress);
        } catch (IOException e) {
            System.out.println("IO EXEPTION");
        }
        catch (JSONException e)
        {
            System.out.println("Wrong adress");
        }



            updateAmbulanceList(coordinates[0],coordinates[1]);
            System.out.println("Updating list");
            updateList();
            System.out.println("List updated");


    }
    public static double [] parse (String adress) throws IOException,JSONException {
        double [] result=new double[2];
        adress=adress.replace(' ','+');


        URL url = new URL("https://geocoder.api.here.com/6.2/geocode.json?"+"app_id="+ID+"&app_code="+CODE+"&searchtext="+"polska"+"+"+adress);

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
        Double x1=obj.getJSONObject("Response").getJSONArray("View").getJSONObject(0).getJSONArray("Result").getJSONObject(0).getJSONObject("Location").getJSONArray("NavigationPosition").getJSONObject(0).getDouble("Latitude");
        Double x2=obj.getJSONObject("Response").getJSONArray("View").getJSONObject(0).getJSONArray("Result").getJSONObject(0).getJSONObject("Location").getJSONArray("NavigationPosition").getJSONObject(0).getDouble("Longitude");
        System.out.println(x1+" "+x2);
        result[0]=x1;
        result[1]=x2;
        return result;
    }

    public double [] calculateDistanceAndTime(double a1, double a2,double b1,double b2) throws IOException,JSONException {
        //50.077797,19.924892
        //50.081727, 19.915362

        double [] result=new double[2];

        double distance=0.0;
        int time=0;
        URL url = new URL("https://route.api.here.com/routing/7.2/calculateroute.json?&app_id="+ID+"&app_code="+CODE+"&waypoint0=geo!"+a1+","+a2+"&waypoint1=geo!"+b1+","+b2+"&mode=fastest;car;traffic:enabled");
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
        JSONObject obj=new JSONObject(content.toString());
        distance=obj.getJSONObject("response").getJSONArray("route").getJSONObject(0).getJSONObject("summary").getDouble("distance");
        time=obj.getJSONObject("response").getJSONArray("route").getJSONObject(0).getJSONObject("summary").getInt("trafficTime");
        result[0]=distance/1000;
        result[1]=time;
        return result;

    }


}
