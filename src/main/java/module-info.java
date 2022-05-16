module cn.octautumn.cpumemsimulation {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens cn.octautumn.cpumemsimulation to javafx.fxml;
    exports cn.octautumn.cpumemsimulation;
}