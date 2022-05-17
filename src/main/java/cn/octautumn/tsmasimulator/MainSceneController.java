package cn.octautumn.tsmasimulator;

import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable
{
    public Spinner<Integer> spinnerMaxThreadNum;
    public Label processor1Status;
    public Label processor2Status;
    public Text textMemStatus;
    public HBox hBoxMemStatusBar;
    public TableView tViewMemBlockTable;
    public TableView tViewThreadTable;
    public TextField tfThreadName;
    public Spinner<Integer> spinnerRequireRunTime;
    public Spinner<Integer> spinnerPriority;
    public RadioButton rbIndependent;
    public RadioButton rbSynchronizationPre;
    public RadioButton rbSynchronizationSuc;
    public ChoiceBox<Integer> cbAssociatedPID;
    public Spinner<Integer> spinnerRequireMemSize;

    public void showSettingDialog()
    {
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
        {
            spinnerMaxThreadNum.setEditable(true);
            spinnerMaxThreadNum.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 16, 1));
        }
    }
}