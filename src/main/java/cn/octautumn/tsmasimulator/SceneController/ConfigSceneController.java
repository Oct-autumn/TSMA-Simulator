package cn.octautumn.tsmasimulator.SceneController;

import cn.octautumn.tsmasimulator.CoreResource;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConfigSceneController implements Initializable
{
    public Spinner<Integer> spinnerProcessorCount;
    public Spinner<Integer> spinnerMaxReadyProcess;
    public Spinner<Integer> spinnerTotalMemorySize;
    public Spinner<Integer> spinnerSystemReservedMemSize;
    public Spinner<Integer> spinnerTimeSliceLength;

    public void saveConfig()
    {
        CoreResource.simulatorConfig.setProcessorCount(spinnerProcessorCount.getValue());
        CoreResource.simulatorConfig.setMaxReadyProcess(spinnerMaxReadyProcess.getValue());
        CoreResource.simulatorConfig.setTotalMemorySize(spinnerTotalMemorySize.getValue());
        CoreResource.simulatorConfig.setSystemReservedMemSize(spinnerSystemReservedMemSize.getValue());
        CoreResource.simulatorConfig.setTimeSliceLength(spinnerTimeSliceLength.getValue());

        CoreResource.configStage.hide();
    }

    public void cancelEdit()
    {
        /*
        if (CoreResource.simulatorConfig.getProcessorCount() != spinnerProcessorCount.getValue()
                || CoreResource.simulatorConfig.getMaxReadyProcess() != spinnerMaxReadyProcess.getValue()
                || CoreResource.simulatorConfig.getTotalMemorySize() != spinnerTotalMemorySize.getValue()
                || CoreResource.simulatorConfig.getSystemReservedMemSize() != spinnerSystemReservedMemSize.getValue())
        {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("警告");
            alert.setContentText("有尚未保存的更改，确定关闭窗口？");
            Optional<ButtonType> choice = alert.showAndWait();
            if (choice.isEmpty() || !choice.get().equals(ButtonType.OK))
                return;
        }
        */
        CoreResource.configStage.hide();
    }

    public void initializeControls()
    {
        spinnerProcessorCount.getValueFactory().setValue(CoreResource.simulatorConfig.getProcessorCount());
        spinnerMaxReadyProcess.getValueFactory().setValue(CoreResource.simulatorConfig.getMaxReadyProcess());
        spinnerTotalMemorySize.getValueFactory().setValue(CoreResource.simulatorConfig.getTotalMemorySize());
        spinnerSystemReservedMemSize.getValueFactory().setValue(CoreResource.simulatorConfig.getSystemReservedMemSize());
        spinnerTimeSliceLength.getValueFactory().setValue(CoreResource.simulatorConfig.getTimeSliceLength());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        spinnerProcessorCount.setDisable(true);
        spinnerProcessorCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 16, 2));
        spinnerMaxReadyProcess.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 16, 0));
        spinnerTotalMemorySize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65536, 2048));
        spinnerSystemReservedMemSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65536, 128));
        spinnerTimeSliceLength.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 5));

        spinnerTotalMemorySize.valueProperty().addListener((observableValue, integer, t1) ->
                ((SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerSystemReservedMemSize.getValueFactory()).setMax(t1));
    }
}
