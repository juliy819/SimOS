package com.juliy.simos.controller;

import com.juliy.simos.common.Context;
import com.juliy.simos.common.Operation;
import com.juliy.simos.system.SystemKernel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

/**
 * @author JuLiy
 * @date 2022/10/21 8:17
 */
public class MainController {
    public static SystemKernel systemKernel = new SystemKernel();

    @FXML
    AnchorPane paneMain;
    @FXML
    AnchorPane paneWelcome;

    @FXML
    void initialize() {
        systemKernel.start();
        Operation.createStage("Process", "进程管理器", false)
                .initOwner(Context.stageMap.get("Main"));
        Operation.createStage("Resource", "资源监视", false)
                .initOwner(Context.stageMap.get("Main"));
        Operation.createStage("Memory", "内存监视", false)
                .initOwner(Context.stageMap.get("Main"));
    }

    @FXML
    void showProcess() {
        Context.stageMap.get("Process").show();
    }

    @FXML
    void showResource() {
        Context.stageMap.get("Resource").show();
    }

    @FXML
    void showMemory() {
        Context.stageMap.get("Memory").show();
    }

    @FXML
    void shutdown() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    void start() {
        paneMain.getChildren().remove(paneWelcome);
    }

    @FXML
    void minimize() {
        Context.stageMap.get("Main").setIconified(true);
    }

}
