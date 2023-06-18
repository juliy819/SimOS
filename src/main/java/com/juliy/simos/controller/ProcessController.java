package com.juliy.simos.controller;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.juliy.simos.common.Operation;
import com.juliy.simos.system.process_manager.PCB;
import com.juliy.simos.system.process_manager.PStatus;
import com.juliy.simos.system.process_manager.ProcessManager;
import com.juliy.simos.system.process_manager.psa.ProcessSchedulingAlgorithm;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
public class ProcessController extends RootController {

    private static final Logger log = Logger.getLogger(ProcessController.class);

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
    private TableColumn<PCB, Integer> tcMemory;
    @FXML
    private TableColumn<PCB, List<Integer>> tcMaxR;
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
                        (ProcessSchedulingAlgorithm) Class.forName("com.juliy.simos.system.process_manager.psa." + nv)
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
        tcMemory.setCellValueFactory(new PropertyValueFactory<>("memorySize"));
        tcMaxR.setCellValueFactory(new PropertyValueFactory<>("maxR"));
        tcArrivalTime.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        tcServiceTime.setCellValueFactory(new PropertyValueFactory<>("serviceTime"));
        tcUsedTime.setCellValueFactory(new PropertyValueFactory<>("usedTime"));
        //设置进度条
        tcProgress.setCellFactory(ProgressBarTableCell.forTableColumn());

        tcMaxR.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(List<Integer> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    this.setText(null);
                } else {
                    this.setText(item.get(0) + "-" + item.get(1) + "-" + item.get(2));
                }
            }
        });
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

    @FXML
    void stopPSA() {
        pcsMgr.stopPSA();
    }

    @FXML
    void continuePSA() {
        pcsMgr.continuePSA();
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
        protected void updateItem(PCB item, boolean empty) {
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
