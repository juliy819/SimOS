package com.juliy.simos.common;

import com.juliy.simos.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * 常用操作类
 * @author JuLiy
 * @date 2022/12/9 13:50
 */
public class Operation {

    /**
     * 创建新窗口
     * @param fxmlName     要加载的fxml文件名(无需后缀)
     * @param title        窗口标题
     * @param isResizeable 窗口是否可缩放
     * @return 创建好的窗口对象
     */
    public static Stage createStage(String fxmlName, String title, boolean isResizeable) {
        Parent root;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("view/" + fxmlName + ".fxml")));
        } catch (IOException e) {
            throw new FxmlLoadError(e);
        }
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.setResizable(isResizeable);
        Context.stageMap.put(fxmlName, stage);
        return stage;
    }

    public static void showErrorAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setContentText(msg);
        alert.showAndWait();
    }

}
