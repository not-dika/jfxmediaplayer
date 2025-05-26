/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.jfxmediaplayer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author ASUS
 */
public class MediaPlayerMain extends Application {
    
    private MediaPlayerController controller;
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("JFXMediaPlayer.fxml"));
        Parent root = loader.load();
        
        controller = loader.getController();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        
        stage.setTitle("JFX Media Player");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
        
        stage.setOnCloseRequest(e -> {
            if (controller != null) {
                controller.shutdown();
            }
            Platform.exit();
        });
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}