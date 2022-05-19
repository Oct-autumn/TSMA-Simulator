package cn.octautumn.tsmasimulator.SceneController;

import cn.octautumn.tsmasimulator.CoreResource;
import cn.octautumn.tsmasimulator.model.Sim.SimMemoryBlock;
import cn.octautumn.tsmasimulator.model.Sim.SimProcess;
import cn.octautumn.tsmasimulator.model.Vis.VisMemoryBlockItem;
import cn.octautumn.tsmasimulator.model.Vis.VisProcessItem;
import cn.octautumn.tsmasimulator.model.Vis.VisProcessorItem;
import cn.octautumn.tsmasimulator.service.MemoryService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable
{
    public Button btnToggleSimulation;
    public Label processorStatus1;
    public ProgressBar pbTimeSliceProgress1;
    public ProgressBar pbProcessProgress1;
    public Label lbTimeSliceProgress1;
    public Label lbProcessProgress1;
    public Label processorStatus2;
    public ProgressBar pbTimeSliceProgress2;
    public ProgressBar pbProcessProgress2;
    public Label lbTimeSliceProgress2;
    public Label lbProcessProgress2;

    public Text textMemStatus;
    public Rectangle rectReservedMem;
    public Rectangle rectActiveMem;
    public Rectangle rectInactiveMem;
    public Rectangle rectIdleMem;
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
    public RadioButton rbSynchronizationSuc;
    public ChoiceBox<Integer> cbAssociatedPID;
    public Spinner<Integer> spinnerRequireMemSize;


    private final ObservableList<VisProcessItem> visProcessItemList = FXCollections.observableArrayList();
    private final ObservableList<VisMemoryBlockItem> visMemoryBlockItemList = FXCollections.observableArrayList();

    public void showSettingDialog()
    {
        if (btnToggleSimulation.getText().equals("暂停模拟"))
        {
            stopSimulation();
        }
        CoreResource.configSceneController.initializeControls();
        CoreResource.configStage.show();
    }

    public void resetSimulator()
    {
        if (btnToggleSimulation.getText().equals("暂停模拟"))
        {
            stopSimulation();
        }
        CoreResource.resetSimulator();
        refreshProcessorVis();
        refreshMemBlockVis();
        refreshTViewThreadTable();
    }

    public void startSimulation()
    {
        CoreResource.processorService.start();

        btnToggleSimulation.setText("暂停模拟");
        btnToggleSimulation.setOnAction((actionEvent) -> stopSimulation());
    }

    public void stopSimulation()
    {
        CoreResource.processorService.stop();

        btnToggleSimulation.setText("开始模拟");
        btnToggleSimulation.setOnAction((actionEvent) -> startSimulation());
    }

    public void nextTimeStep()
    {
        //CoreResource.processorService.nextTimeStep();
    }

    public void addNewProcess()
    {
        if (spinnerRequireRunTime.getValue() == null)
            return;
        if (spinnerPriority.getValue() == null)
            return;
        if (spinnerRequireMemSize.getValue() == null)
            return;

        if (rbSynchronizationSuc.isSelected())
        {
            if (cbAssociatedPID.getValue() == null)
                return;

            ArrayList<Integer> assocPidList = new ArrayList<>();
            assocPidList.add(cbAssociatedPID.getValue());

            CoreResource.processService.createNewProcess(
                    tfThreadName.getText(),
                    spinnerRequireRunTime.getValue() * 1000,
                    spinnerPriority.getValue(),
                    SimProcess.Property.SYNCHRONIZE_SUC,
                    assocPidList,
                    spinnerRequireMemSize.getValue()
            );
        } else
        {
            CoreResource.processService.createNewProcess(
                    tfThreadName.getText(),
                    spinnerRequireRunTime.getValue() * 1000,
                    spinnerPriority.getValue(),
                    SimProcess.Property.INDEPENDENT,
                    null,
                    spinnerRequireMemSize.getValue()
            );
        }

        tfThreadName.setText("");
        spinnerRequireRunTime.getValueFactory().setValue(1);
        spinnerPriority.getValueFactory().setValue(0);
        rbIndependent.setSelected(true);
        spinnerRequireMemSize.getValueFactory().setValue(1);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        spinnerRequireRunTime.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 256, 1));
        spinnerPriority.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 256, 0));
        spinnerRequireMemSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,
                CoreResource.simulatorConfig.getTotalMemorySize() - CoreResource.simulatorConfig.getSystemReservedMemSize(), 1));
        rbIndependent.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1)
            {
                cbAssociatedPID.setDisable(true);
                cbAssociatedPID.getItems().clear();
            }
        });
        rbSynchronizationSuc.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1)
            {
                cbAssociatedPID.setDisable(false);
                ObservableList<Integer> optionalPidList = CoreResource.processService.getOptionalPidList();
                cbAssociatedPID.setItems(optionalPidList);
            }
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
        tcToggleProcessHang.setCellValueFactory(new PropertyValueFactory<>("toggleHangButton"));
    }

    public void refreshProcessorVis()
    {
        Platform.runLater(() -> {
            VisProcessorItem vis1 = new VisProcessorItem(CoreResource.processorService.processorMap.get(0));
            processorStatus1.setText(vis1.getStatusInfo());
            pbTimeSliceProgress1.setProgress(vis1.getTimeSlicePercentage());
            pbProcessProgress1.setProgress(vis1.getProcessRunTimePercentage());
            if (vis1.getTimeSlicePercentage() == 0)
                lbTimeSliceProgress1.setText("--");
            else
                lbTimeSliceProgress1.setText(String.format("%.2f%%", vis1.getTimeSlicePercentage() * 100));
            if (vis1.getProcessRunTimePercentage() == 0)
                lbProcessProgress1.setText("--");
            else
                lbProcessProgress1.setText(String.format("%.2f%%", vis1.getProcessRunTimePercentage() * 100));

            VisProcessorItem vis2 = new VisProcessorItem(CoreResource.processorService.processorMap.get(1));
            processorStatus2.setText(vis2.getStatusInfo());
            pbTimeSliceProgress2.setProgress(vis2.getTimeSlicePercentage());
            pbProcessProgress2.setProgress(vis2.getProcessRunTimePercentage());
            if (vis2.getTimeSlicePercentage() == 0)
                lbTimeSliceProgress2.setText("--");
            else
                lbTimeSliceProgress2.setText(String.format("%.2f%%", vis2.getTimeSlicePercentage() * 100));
            if (vis2.getProcessRunTimePercentage() == 0)
                lbProcessProgress2.setText("--");
            else
                lbProcessProgress2.setText(String.format("%.2f%%", vis2.getProcessRunTimePercentage() * 100));
        });
    }

    public void refreshMemBlockVis()
    {
        Platform.runLater(() -> {
            int totalMemSize = CoreResource.simulatorConfig.getTotalMemorySize();
            int idleMemSize = 0;
            int activeMemSize = 0;
            int inactiveMemSize = 0;
            int reservedMemSize = 0;
            for (SimMemoryBlock it : MemoryService.memoryBlockList)
            {
                switch (it.getStatus())
                {
                    case IDLE -> idleMemSize += it.getTotalSize();
                    case ACTIVE -> activeMemSize += it.getTotalSize();
                    case INACTIVE -> inactiveMemSize += it.getTotalSize();
                    case RESERVED -> reservedMemSize += it.getTotalSize();
                }
            }

            textMemStatus.setText(String.format("%dByte / %dByte", totalMemSize - idleMemSize, totalMemSize));

            rectReservedMem.setWidth(((double) reservedMemSize / totalMemSize) * 700);
            rectActiveMem.setWidth(((double) activeMemSize / totalMemSize) * 700);
            rectInactiveMem.setWidth(((double) inactiveMemSize / totalMemSize) * 700);
            rectIdleMem.setWidth(((double) idleMemSize / totalMemSize) * 700);

            tViewMemBlockTable.getItems().clear();
            visMemoryBlockItemList.clear();
            for (SimMemoryBlock it : MemoryService.memoryBlockList)
                visMemoryBlockItemList.add(new VisMemoryBlockItem(it));
            tViewMemBlockTable.setItems(visMemoryBlockItemList);
        });
    }

    public void refreshTViewThreadTable()
    {
        Platform.runLater(() -> {
            tViewThreadTable.getItems().clear();
            visProcessItemList.clear();
            CoreResource.processService.getProcessMap().forEach((integer, simProcess) ->
                    visProcessItemList.add(new VisProcessItem(simProcess)));
            tViewThreadTable.setItems(visProcessItemList);
        });
    }
}
