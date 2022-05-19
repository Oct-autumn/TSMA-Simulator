package cn.octautumn.tsmasimulator.SceneController;

import cn.octautumn.tsmasimulator.CoreResource;
import cn.octautumn.tsmasimulator.SimulatorRun;
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
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable
{
    public Button btnToggleSimulation;
    public Label processorStatus1;
    public Label processorStatus2;
    public Text textMemStatus;
    //public HBox hBoxMemStatusBar;

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
    private final SimulatorRun simulator = new SimulatorRun();

    public void showSettingDialog()
    {
        CoreResource.configSceneController.initializeControls();
        CoreResource.configStage.show();
    }

    public void resetSimulator()
    {
        CoreResource.resetSimulator();
    }

    public void startSimulation()
    {
        simulator.start();

        btnToggleSimulation.setText("暂停模拟");
        btnToggleSimulation.setOnAction((actionEvent) -> stopSimulation());
    }

    public void stopSimulation()
    {
        simulator.stop();

        btnToggleSimulation.setText("开始模拟");
        btnToggleSimulation.setOnAction((actionEvent) -> startSimulation());
    }

    public void nextTimeStep()
    {
        simulator.nextTimeStep();
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
                    spinnerRequireRunTime.getValue(),
                    spinnerPriority.getValue(),
                    SimProcess.Property.SYNCHRONIZE_SUC,
                    assocPidList,
                    spinnerRequireMemSize.getValue()
            );
        } else
        {
            CoreResource.processService.createNewProcess(
                    tfThreadName.getText(),
                    spinnerRequireRunTime.getValue(),
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
        spinnerRequireMemSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, CoreResource.simulatorConfig.getTotalMemorySize(), 1));
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

        //tViewMemBlockTable.sc

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

    public void refreshTViewThreadTable()
    {
        tViewThreadTable.getItems().clear();
        visProcessItemList.clear();
        CoreResource.processService.getProcessMap().forEach((integer, simProcess) ->
                visProcessItemList.add(new VisProcessItem(simProcess)));
        tViewThreadTable.setItems(visProcessItemList);
    }

    public void refreshTViewMemBlockTable()
    {
        tViewMemBlockTable.getItems().clear();
        visMemoryBlockItemList.clear();
        for (SimMemoryBlock it : MemoryService.memoryBlockList)
            visMemoryBlockItemList.add(new VisMemoryBlockItem(it));
        tViewMemBlockTable.setItems(visMemoryBlockItemList);
    }
}
