package com.juliy.simos;

import com.juliy.simos.common.Operation;
import javafx.application.Application;
import javafx.stage.Stage;

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
        primaryStage.show();
    }
}
