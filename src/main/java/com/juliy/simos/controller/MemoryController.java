package com.juliy.simos.controller;

import com.jfoenix.controls.JFXListView;
import com.juliy.simos.system.memory_manager.MemoryBlock;
import com.juliy.simos.system.memory_manager.MemoryManager;
import com.juliy.simos.system.memory_manager.dpaa.MemoryAllocationAlgorithm;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 内存页面控制器
 * @author JuLiy
 * @date 2022/12/20 23:55
 */
public class MemoryController extends RootController {

    private static final Logger log = Logger.getLogger(MemoryController.class);

    private MemoryManager memMgr;

    @FXML
    private ComboBox<String> cbbMAA;
    @FXML
    private JFXListView<MemoryBlock> listMemory;
    @FXML
    private Text txtMaxSize;
    @FXML
    private Text txtUsedSize;

    @FXML
    void initialize() {
        memMgr = MainController.systemKernel.getMemoryManager();
        txtMaxSize.setText(String.valueOf(memMgr.getMaxSize()));

        initList();
        initCbb();
    }

    private void initCbb() {
        cbbMAA.getItems().addAll("FF", "NF", "BF", "WF");
        cbbMAA.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            try {
                MemoryAllocationAlgorithm maa =
                        (MemoryAllocationAlgorithm) Class.forName("com.juliy.simos.system.memory_manager.dpaa." + nv)
                                .getConstructor(List.class)
                                .newInstance(memMgr.getMemoryList());
                memMgr.setMAA(maa);
                log.info("当前内存调度算法：" + maa.getClass().getSimpleName());
            } catch (ClassNotFoundException | NoSuchMethodException |
                     InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        cbbMAA.getSelectionModel().select(0);
    }

    private void initList() {
        listMemory.setItems(memMgr.getMemoryList());
        listMemory.setCellFactory(param -> new ListCell<>() {
            final Label startAddress = new Label();
            final Label size = new Label();
            final Label status = new Label();
            final AnchorPane pane = new AnchorPane(startAddress, size, status);

            {
                startAddress.setFont(Font.font(16));
                size.setFont(Font.font(16));
                status.setFont(Font.font(16));
                pane.setPadding(new Insets(1, 1, 1, 1));
                AnchorPane.setLeftAnchor(startAddress, 80.0);
                AnchorPane.setLeftAnchor(size, 200.0);
                AnchorPane.setLeftAnchor(status, 320.0);
            }

            @Override
            protected void updateItem(MemoryBlock item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    this.setGraphic(null);
                    this.setText(null);
                } else {
                    startAddress.setText("起址:" + item.getStartAddress());
                    size.setText("大小:" + item.getSize());
                    status.setText("状态:" + item.getStatus());
                    this.setGraphic(pane);

                }
            }
        });
    }

    public void update() {
        txtUsedSize.setText(String.valueOf(memMgr.getUsedSize()));
        listMemory.refresh();
    }

}
