package cn.octautumn.tsmasimulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ApplicationBoot extends Application
{
    @Override
    public void start(Stage stage) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationBoot.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 750);
        stage.setResizable(false);
        stage.setTitle("TSMA模拟器");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args)
    {
        launch();
        CoreResource.resetSimulator();
    }
}