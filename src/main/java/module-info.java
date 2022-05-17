module cn.octautumn.cpumemsimulation {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires lombok;

    opens cn.octautumn.tsmasimulator to javafx.fxml;
    exports cn.octautumn.tsmasimulator;
    exports cn.octautumn.tsmasimulator.model;
}