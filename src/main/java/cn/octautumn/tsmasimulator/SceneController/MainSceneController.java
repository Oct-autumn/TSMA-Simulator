package cn.octautumn.tsmasimulator.SceneController;

import cn.octautumn.tsmasimulator.CoreResource;
import cn.octautumn.tsmasimulator.model.SimMemoryBlock;
import cn.octautumn.tsmasimulator.model.SimProcess;
import cn.octautumn.tsmasimulator.model.VisMemoryBlockItem;
import cn.octautumn.tsmasimulator.model.VisProcessItem;
import cn.octautumn.tsmasimulator.service.MemoryService;
import cn.octautumn.tsmasimulator.service.ProcessService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable
{
    public Label processor1Status;
    public Label processor2Status;
    public Text textMemStatus;
    public HBox hBoxMemStatusBar;

    public TableView<VisMemoryBlockItem> tViewMemBlockTable;
    public TableColumn<VisMemoryBlockItem, String> tcMemBlockStartPos;
    public TableColumn<VisMemoryBlockItem, String> tcMemBlockEndPos;
    public TableColumn<VisMemoryBlockItem, String> tcMemBlockSize;
    public TableColumn<VisMemoryBlockItem, String> tcMemBlockStatus;

    public TableView<VisProcessItem> tViewThreadTable;
    public TableColumn<VisProcessItem, String> tcProcessNameAndID;
    public TableColumn<VisProcessItem, String> tcProcessRequireRunTime;
    public TableColumn<VisProcessItem, String> tcProcessPriority;
    public TableColumn<VisProcessItem, String> tcProcessStatus;
    public TableColumn<VisProcessItem, String> tcProcessProperty;
    public TableColumn<VisProcessItem, String> tcProcessMemSize;
    public TableColumn<VisProcessItem, String> tcProcessMemStartPos;
    public TableColumn<VisProcessItem, Button> tcToggleProcessHang;

    public TextField tfThreadName;
    public Spinner<Integer> spinnerRequireRunTime;
    public Spinner<Integer> spinnerPriority;
    public RadioButton rbIndependent;
    public RadioButton rbSynchronizationPre;
    public RadioButton rbSynchronizationSuc;
    public ChoiceBox<Integer> cbAssociatedPID;
    public Spinner<Integer> spinnerRequireMemSize;

    private final ObservableList<VisProcessItem> visProcessItemList = FXCollections.observableArrayList();
    private final ObservableList<VisMemoryBlockItem> visMemoryBlockItemList = FXCollections.observableArrayList();

    public void showSettingDialog()
    {
        CoreResource.configSceneController.initializeControls();
        CoreResource.configStage.show();
    }

    public void resetSimulator()
    {
        CoreResource.resetSimulator();
    }

    public void startSimulator()
    {
    }

    public void nextTimeSlice()
    {
    }

    public void addNewProcess()
    {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        spinnerRequireRunTime.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 256, 1));
        spinnerPriority.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 256, 0));
        spinnerRequireMemSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, CoreResource.simulatorConfig.getTotalMemorySize(), 1));
        rbIndependent.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) cbAssociatedPID.setDisable(true);
        });
        rbSynchronizationPre.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) cbAssociatedPID.setDisable(false);
        });
        rbSynchronizationSuc.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) cbAssociatedPID.setDisable(false);
        });

        tcMemBlockStartPos.setCellValueFactory(new PropertyValueFactory<>("startPos"));
        tcMemBlockEndPos.setCellValueFactory(new PropertyValueFactory<>("endPos"));
        tcMemBlockSize.setCellValueFactory(new PropertyValueFactory<>("totalSize"));
        tcMemBlockStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tcProcessNameAndID.setCellValueFactory(new PropertyValueFactory<>("nameAndPID"));
        tcProcessRequireRunTime.setCellValueFactory(new PropertyValueFactory<>("totalAndElapsedRunTime"));
        tcProcessPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        tcProcessStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tcProcessProperty.setCellValueFactory(new PropertyValueFactory<>("propertyAndAssocPID"));
        tcProcessMemSize.setCellValueFactory(new PropertyValueFactory<>("requireMemSize"));
        tcProcessMemStartPos.setCellValueFactory(new PropertyValueFactory<>("memStartPos"));
        tcToggleProcessHang.setCellFactory((col) -> new TableCell<>()
                {
                    @Override
                    protected void updateItem(Button item, boolean empty)
                    {
                        super.updateItem(item, empty);

                        //如果为空，则跳出
                        if (empty) return;

                        //获取模拟值
                        final SimProcess simProcess = ProcessService.processMap.get(Integer.parseInt(visProcessItemList.get(getIndex()).getNameAndPID().split("/")[1]));
                        if (simProcess == null)
                            return;

                        switch (Objects.requireNonNull(simProcess).getStatus())
                        {

                            case BACK, REVOKE, BLOCK, SYS_HANGUP ->
                            {
                            }
                            case READY ->
                            {
                                Button button = new Button("挂起");
                                button.setPrefHeight(20);
                                button.setOnAction(actionEvent -> {
                                    simProcess.setStatus(SimProcess.Status.USER_HANGUP);
                                    //回收内存
                                    CoreResource.memoryService.freeMemBlock(simProcess.getMemStartPos());
                                    simProcess.setMemStartPos(-1);

                                    //从就绪队列移出并加入到挂起队列
                                    ProcessService.readyPL.remove((Integer) simProcess.getPID());
                                    ProcessService.hangPL.add(simProcess.getPID());
                                    CoreResource.mainSceneController.refreshTViewThreadTable();
                                });
                                this.setGraphic(button);
                            }
                            case RUNNING, USER_HANGUP ->
                            {
                                Button button = new Button("解挂");
                                button.setPrefHeight(20);
                                button.setOnAction(actionEvent -> {
                                    if (ProcessService.readyPL.size() < CoreResource.simulatorConfig.getMaxReadyProcess())
                                    {
                                        int memBlockStartPos = CoreResource.memoryService.allocMemBlock(simProcess.getRequireMemSize(), SimMemoryBlock.Status.INUSE);
                                        if (memBlockStartPos != -1)
                                        {//成功分配内存，进入就绪队列
                                            simProcess.setStatus(SimProcess.Status.READY);
                                            simProcess.setMemStartPos(memBlockStartPos);
                                            ProcessService.hangPL.remove((Integer) simProcess.getPID());
                                            ProcessService.readyPL.add(simProcess.getPID());
                                            return;
                                        }
                                    }

                                    //内存分配失败，进入后备队列
                                    simProcess.setStatus(SimProcess.Status.USER_HANGUP);
                                    ProcessService.readyPL.remove(simProcess.getPID());
                                    ProcessService.hangPL.add(simProcess.getPID());
                                    CoreResource.mainSceneController.refreshTViewThreadTable();
                                });
                                this.setGraphic(button);
                            }
                        }


//                        if (empty)
//                        {
//                            //如果此列为空默认不添加元素
//                            setText(null);
//                            setGraphic(null);
//                        } else
//                        {
//                            this.setGraphic(button);
//                        }
                    }
                }
        );
    }

    public void refreshTViewThreadTable()
    {
        visProcessItemList.clear();
        ProcessService.processMap.forEach((integer, simProcess) ->
                visProcessItemList.add(new VisProcessItem(simProcess)));
        tViewThreadTable.setItems(visProcessItemList);
    }

    public void refreshTViewMemBlockTable()
    {
        visMemoryBlockItemList.clear();
        for (SimMemoryBlock it : MemoryService.memoryBlockList)
            visMemoryBlockItemList.add(new VisMemoryBlockItem(it));
        tViewMemBlockTable.setItems(visMemoryBlockItemList);
    }
}
