package com.juliy.simos.controller;

import com.juliy.simos.common.Context;
import com.juliy.simos.common.Operation;
import com.juliy.simos.system.SystemKernel;
import javafx.fxml.FXML;

/**
 * @author JuLiy
 * @date 2022/10/21 8:17
 */
public class MainController {
    public static SystemKernel systemKernel = new SystemKernel();

    @FXML
    void initialize() {
        systemKernel.start();
        Operation.createStage("ProcessManager", "进程管理器", false);
    }

    @FXML
    void showPcsMgr() {
        Context.stageMap.get("ProcessManager").show();
    }

}
