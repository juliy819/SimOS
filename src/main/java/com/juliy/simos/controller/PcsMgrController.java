package com.juliy.simos.controller;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.juliy.simos.common.Operation;
import com.juliy.simos.entity.PCB;
import com.juliy.simos.entity.PStatus;
import com.juliy.simos.system.process_manager.ProcessManager;
import com.juliy.simos.system.process_manager.sa.ProcessSchedulingAlgorithm;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author JuLiy
 * @date 2022/12/8 21:43
 */
public class PcsMgrController extends RootController {

    private static final Logger log = Logger.getLogger(PcsMgrController.class);

    private ProcessManager pcsMgr;

    @FXML
    private JFXComboBox<String> cbbSa;
    @FXML
    private JFXListView<PCB> listBlock;
    @FXML
    private JFXListView<PCB> listReady;
    @FXML
    private TableView<PCB> tableProcess;
    @FXML
    private TableColumn<PCB, Integer> tcArrivalTime;
    @FXML
    private TableColumn<PCB, Integer> tcPId;
    @FXML
    private TableColumn<PCB, Integer> tcPriority;
    @FXML
    private TableColumn<PCB, Double> tcProgress;
    @FXML
    private TableColumn<PCB, Integer> tcServiceTime;
    @FXML
    private TableColumn<PCB, PStatus> tcStatus;
    @FXML
    private TableColumn<PCB, String> tcUId;
    @FXML
    private TableColumn<PCB, Integer> tcUsedTime;
    @FXML
    private Text txtCpuTime;

    @FXML
    void initialize() {
        pcsMgr = MainController.systemKernel.getProcessManager();
        txtCpuTime.textProperty().bind(pcsMgr.cpuTimeProperty());

        initTable();
        initCbb();
        initListView();
    }

    private void initListView() {
        listReady.setItems(pcsMgr.getReadyQueue());
        listBlock.setItems(pcsMgr.getBlockQueue());

        listReady.setCellFactory(param -> new ProcessCell());
        listBlock.setCellFactory(param -> new ProcessCell());
    }

    private void initCbb() {
        cbbSa.getItems().addAll("FCFS", "SJF", "PJF", "RR");
        cbbSa.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            try {
                ProcessSchedulingAlgorithm psa =
                        (ProcessSchedulingAlgorithm) Class.forName("com.juliy.simos.system.process_manager.sa." + nv)
                                .getConstructor(List.class)
                                .newInstance(pcsMgr.getReadyQueue());
                pcsMgr.setPsa(psa);
            } catch (ClassNotFoundException | NoSuchMethodException |
                     InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        cbbSa.getSelectionModel().select(0);
    }

    private void initTable() {
        //设置数据
        tcPId.setCellValueFactory(new PropertyValueFactory<>("pid"));
        tcUId.setCellValueFactory(new PropertyValueFactory<>("uid"));
        tcStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tcPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        tcProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        tcArrivalTime.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        tcServiceTime.setCellValueFactory(new PropertyValueFactory<>("serviceTime"));
        tcUsedTime.setCellValueFactory(new PropertyValueFactory<>("usedTime"));
        //设置进度条
        tcProgress.setCellFactory(ProgressBarTableCell.forTableColumn());
    }

    @FXML
    void createNewProcess() {
        PCB pcb = pcsMgr.create();
        tableProcess.getItems().add(pcb);
    }

    @FXML
    void destroyProcess() {
        PCB pcb = getPCB();
        if (pcb != null) {
            pcsMgr.destroy(pcb.getPid());
        }
    }

    @FXML
    void suspendProcess() {
        PCB pcb = getPCB();
        if (pcb != null) {
            pcsMgr.suspend(pcb.getPid());
        }
    }

    @FXML
    void activeProcess() {
        PCB pcb = getPCB();
        if (pcb != null) {
            pcsMgr.active(pcb.getPid());
        }
    }

    /** 获取选中进程的PCB */
    private PCB getPCB() {
        PCB pcb = null;
        if (tableProcess.getSelectionModel().isEmpty()) {
            Operation.showErrorAlert("请先选中一个进程！");
        } else {
            pcb = tableProcess.getSelectionModel().getSelectedItem();
        }
        return pcb;
    }

    /** 自定义列表单元格 */
    static class ProcessCell extends ListCell<PCB> {
        final Label uid = new Label();
        final Label status = new Label();
        final AnchorPane pane = new AnchorPane(uid, status);

        public ProcessCell() {
            AnchorPane.setLeftAnchor(uid, 0.0);
            AnchorPane.setLeftAnchor(status, 60.0);
        }

        @Override
        protected void updateItem(com.juliy.simos.entity.PCB item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                this.setGraphic(null);
            } else {
                uid.setText("pid : " + item.getPid());
                status.setText("status : " + item.getStatus());
                this.setGraphic(pane);
            }
        }
    }
}
