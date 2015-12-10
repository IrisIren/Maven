package ecg;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main extends Application {

    private String comPortName;
    private static BlockingQueue<Double> queue = new LinkedBlockingQueue<>();
    private static ComPortHandler comPortHandler = new ComPortHandler(queue);
    private static SignalHandler signalHandler = new SignalHandler(queue);

    private Thread signalHandlerThread = new Thread(signalHandler);

    private ListView<String> getPortList() {
        ListView<String> list = new ListView<String>();
        String[] portNames = ComPortHandler.getPortNames();
        if (portNames.length == 0) {
            portNames = new String[]{"No COM ports found"};
            list.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            list.getSelectionModel().select(-1);
                        }
                    });
                }
            });
        } else {
            list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (comPortName == null) {
                        comPortName = newValue;
                        ComPortHandler.setSerialPort(comPortName);
                        System.out.println(comPortName);
                        comPortHandler.start();
                    } else {
                        comPortHandler.finish();
                        comPortName = newValue;
                        ComPortHandler.setSerialPort(comPortName);
                        comPortHandler.start();
                    }
                }
            });
        }
        ObservableList<String> items = FXCollections.observableArrayList(portNames);
        list.setItems(items);
        return list;
    }

    private void init(Stage primaryStage) {
//        TO USE WITH FXML
//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
//        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.setTitle("JavaFX Welcome");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Scene scene = new Scene(grid, 300, 275);
        primaryStage.setScene(scene);

        grid.add(getPortList(), 3, 4);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                signalHandler.terminate();
                Platform.exit();
                System.exit(0);
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        init(primaryStage);

        signalHandlerThread.start();

        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}