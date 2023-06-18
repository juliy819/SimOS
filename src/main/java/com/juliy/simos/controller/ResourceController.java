package com.juliy.simos.controller;

import com.jfoenix.controls.JFXListView;
import com.juliy.simos.system.resource_manager.Resource;
import com.juliy.simos.system.resource_manager.ResourceManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;

/**
 * 资源管理页面
 * @author JuLiy
 * @date 2022/12/19 0:37
 */
public class ResourceController extends RootController {

    private ResourceManager resMgr;

    @FXML
    private JFXListView<Resource> listA;
    @FXML
    private JFXListView<Resource> listB;
    @FXML
    private JFXListView<Resource> listC;
    @FXML
    private Text txtFreeA;
    @FXML
    private Text txtFreeB;
    @FXML
    private Text txtFreeC;
    @FXML
    private Text txtTotalA;
    @FXML
    private Text txtTotalB;
    @FXML
    private Text txtTotalC;

    @FXML
    void initialize() {
        resMgr = MainController.systemKernel.getResourceManager();

        initText();
        initList();
    }

    private void initText() {
        txtTotalA.setText(String.valueOf(resMgr.getTotalANum()));
        txtTotalB.setText(String.valueOf(resMgr.getTotalANum()));
        txtTotalC.setText(String.valueOf(resMgr.getTotalANum()));
    }

    private void initList() {
        listA.setItems(resMgr.getResourceA());
        listB.setItems(resMgr.getResourceB());
        listC.setItems(resMgr.getResourceC());

        Callback<ListView<Resource>, ListCell<Resource>> cell = new Callback<>() {
            @Override
            public ListCell<Resource> call(ListView<Resource> param) {
                return new ListCell<>() {
                    final Label status = new Label();
                    final Label id = new Label();
                    final HBox hbox = new HBox(status, id);

                    {
                        status.setFont(Font.font(16));
                        id.setFont(Font.font(16));
                        hbox.setSpacing(20);
                        hbox.setPadding(new Insets(1, 1, 1, 1));
                        hbox.setAlignment(Pos.CENTER);
                    }

                    @Override
                    protected void updateItem(Resource item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            this.setGraphic(null);
                            this.setText(null);
                        } else {
                            status.setText("status:" + item.getStatus());
                            id.setText("id:" + item.getId());
                            this.setGraphic(hbox);

                        }
                    }
                };
            }
        };
        listA.setCellFactory(cell);
        listB.setCellFactory(cell);
        listC.setCellFactory(cell);
    }

    /** 更新数据 */
    public void update(int freeA, int freeB, int freeC) {
        txtFreeA.setText(String.valueOf(freeA));
        txtFreeB.setText(String.valueOf(freeB));
        txtFreeC.setText(String.valueOf(freeC));

        listA.refresh();
        listB.refresh();
        listC.refresh();
    }
}
