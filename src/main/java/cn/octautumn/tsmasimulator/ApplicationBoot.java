package cn.octautumn.tsmasimulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ApplicationBoot extends Application
{
    @Override
    public void start(Stage stage) throws IOException
    {
        CoreResource.mainStage = stage;
        CoreResource.mainStage.getIcons().setAll(new Image(
                Objects.requireNonNull(ApplicationBoot.class.getResourceAsStream("img/icon-main.png"))));
        CoreResource.mainStage.setResizable(false);
        CoreResource.mainStage.setTitle("TSMA模拟器");
        FXMLLoader mainSceneLoader = new FXMLLoader(ApplicationBoot.class.getResource("main-view.fxml"));
        Scene mainScene = new Scene(mainSceneLoader.load(), 800, 750);
        CoreResource.mainStage.setScene(mainScene);
        CoreResource.mainSceneController = mainSceneLoader.getController();

        CoreResource.mainStage.setOnCloseRequest(windowEvent -> {
            System.exit(0);
        });

        CoreResource.configStage = new Stage();
        CoreResource.configStage.getIcons().setAll(new Image(
                Objects.requireNonNull(ApplicationBoot.class.getResourceAsStream("img/icon-main.png"))));
        CoreResource.configStage.setResizable(false);
        CoreResource.configStage.setTitle("模拟器设置");
        FXMLLoader configSceneLoader = new FXMLLoader(ApplicationBoot.class.getResource("config-view.fxml"));
        Scene configScene = new Scene(configSceneLoader.load(), 350, 270);
        CoreResource.configStage.setScene(configScene);
        CoreResource.configStage.initOwner(stage);
        CoreResource.configStage.initModality(Modality.WINDOW_MODAL);
        CoreResource.configSceneController = configSceneLoader.getController();

        CoreResource.mainStage.show();
        CoreResource.resetSimulator();
    }

    public static void main(String[] args)
    {
        launch();
    }
}