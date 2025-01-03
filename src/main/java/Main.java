import dataHandler.EmployeeLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import view.CommuteTimerView;

import java.util.List;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        EmployeeLoader employeeLoader = new EmployeeLoader();
        List<String> employeeNames = employeeLoader.loadEmployeeNames();

        TabPane tabPane = new TabPane();

        for (String employeeName : employeeNames) {
            CommuteTimerView timerView = new CommuteTimerView(employeeName);
            VBox timerRoot = timerView.createTimerView();

            Tab tab = new Tab(employeeName, timerRoot);
            tabPane.getTabs().add(tab);
        }

        Scene scene = new Scene(tabPane, 600, 500);
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/icon/icon.png")));
        stage.setTitle("출퇴근 기록 프로그램");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
