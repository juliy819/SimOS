package com.juliy.simos;

import com.juliy.simos.common.Operation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author JuLiy
 * @date 2022/10/21 8:17
 */
public class App extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage = Operation.createStage("Main", "模拟系统", false);
        //primaryStage.setMaximized(true);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }
}
