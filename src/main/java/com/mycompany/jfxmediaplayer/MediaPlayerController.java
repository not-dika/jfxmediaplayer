/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.jfxmediaplayer;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

/**
 *
 * @author ASUS
 */
public class MediaPlayerController implements Initializable {

    @FXML
    private BorderPane mainPane;

    @FXML
    private MediaView mediaView;

    @FXML
    private Button playButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button prevButton;

    @FXML
    private Slider timeSlider;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Label timeLabel;

    @FXML
    private Label volumeLabel;

    @FXML
    private Button openButton;

    @FXML
    private ListView<String> playlistView;

    @FXML
    private Label titleLabel;

    @FXML
    private StackPane mediaPane;

    @FXML
    private HBox controlBox;

    private MediaPlayer mediaPlayer;
    private Media media;
    private String filePath;

    private ObservableList<String> playlist = FXCollections.observableArrayList();
    private ObservableList<String> playlistPaths = FXCollections.observableArrayList();
    private int currentMediaIndex = 0;

    private boolean isPlaying = false;
    private boolean isMuted = false;
    private double volumeBeforeMute = 0.5;
    private boolean isEndOfMedia = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        timeSlider.setValue(0);
        volumeSlider.setValue(50);
        volumeLabel.setText("50%");

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                double volume = newValue.doubleValue() / 100.0;
                mediaPlayer.setVolume(volume);
                volumeLabel.setText(String.format("%.0f%%", newValue.doubleValue()));
                volumeBeforeMute = volume;
            }
        });

        playlistView.setItems(playlist);
        playlistView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int selectedIndex = playlistView.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < playlistPaths.size()) {
                    currentMediaIndex = selectedIndex;
                    playMedia(playlistPaths.get(currentMediaIndex));
                }
            }
        });

        setupButtonIcons();
    }

    private void setupButtonIcons() {
        try {
            ImageView playIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/play.png")));
            playIcon.setFitHeight(16);
            playIcon.setFitWidth(16);
            playButton.setGraphic(playIcon);

            ImageView pauseIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/pause.png")));
            pauseIcon.setFitHeight(16);
            pauseIcon.setFitWidth(16);
            pauseButton.setGraphic(pauseIcon);

            ImageView stopIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/stop.png")));
            stopIcon.setFitHeight(16);
            stopIcon.setFitWidth(16);
            stopButton.setGraphic(stopIcon);

            ImageView prevIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/prev.png")));
            prevIcon.setFitHeight(16);
            prevIcon.setFitWidth(16);
            prevButton.setGraphic(prevIcon);

            ImageView nextIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/next.png")));
            nextIcon.setFitHeight(16);
            nextIcon.setFitWidth(16);
            nextButton.setGraphic(nextIcon);

            ImageView openIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/open.png")));
            openIcon.setFitHeight(16);
            openIcon.setFitWidth(16);
            openButton.setGraphic(openIcon);
        } catch (Exception e) {
            System.out.println("Error loading button icons: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenMedia(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Media");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Media Files", "*.mp3", "*.mp4", "*.wav", "*.aac"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) mainPane.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            String path = selectedFile.toURI().toString();
            addToPlaylist(selectedFile.getName(), path);

            currentMediaIndex = playlist.size() - 1;
            playMedia(path);
        }
    }

    private void addToPlaylist(String name, String path) {
        playlist.add(name);
        playlistPaths.add(path);
    }

    private void playMedia(String path) {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }

        try {
            media = new Media(path);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            DoubleProperty width = mediaView.fitWidthProperty();
            DoubleProperty height = mediaView.fitHeightProperty();
            width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
            height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height").multiply(0.7));
            mediaView.setPreserveRatio(true);

            String title = playlist.get(currentMediaIndex);
            titleLabel.setText(title);

            mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
            mediaPlayer.setOnReady(() -> {
                timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
                timeLabel.setText(formatTime(mediaPlayer.getCurrentTime(), mediaPlayer.getTotalDuration()));
            });

            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                timeSlider.setValue(newValue.toSeconds());
                timeLabel.setText(formatTime(mediaPlayer.getCurrentTime(), mediaPlayer.getTotalDuration()));
            });

            timeSlider.setOnMousePressed(event -> mediaPlayer.pause());
            timeSlider.setOnMouseReleased(event -> {
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
                if (isPlaying) {
                    mediaPlayer.play();
                }
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                stopMedia();
                isEndOfMedia = true;

                if (currentMediaIndex < playlistPaths.size() - 1) {
                    currentMediaIndex++;
                    playMedia(playlistPaths.get(currentMediaIndex));
                    playButton.fire();
                }
            });
            mediaPlayer.play();
            isPlaying = true;

        } catch (Exception e) {
            System.out.println("Error playing media: " + e.getMessage());
        }
    }

    @FXML
    private void handlePlayMedia(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            isPlaying = true;
            if (isEndOfMedia) {
                mediaPlayer.seek(Duration.ZERO);
                isEndOfMedia = false;
            }
        } else if (!playlistPaths.isEmpty()) {
            playMedia(playlistPaths.get(currentMediaIndex));
        }
    }

    @FXML
    private void handlePauseMedia(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    @FXML
    private void handleStopMedia(ActionEvent event) {
        stopMedia();
    }

    private void stopMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
        }
    }

    @FXML
    private void handlePrevMedia(ActionEvent event) {
        if (playlistPaths.isEmpty()) {
            return;
        }

        if (currentMediaIndex > 0) {
            currentMediaIndex--;
        } else {
            currentMediaIndex = playlistPaths.size() - 1;
        }

        playMedia(playlistPaths.get(currentMediaIndex));
    }

    @FXML
    private void handleNextMedia(ActionEvent event) {
        if (playlistPaths.isEmpty()) {
            return;
        }

        if (currentMediaIndex < playlistPaths.size() - 1) {
            currentMediaIndex++;
        } else {
            currentMediaIndex = 0;
        }

        playMedia(playlistPaths.get(currentMediaIndex));
    }

    @FXML
    private void handleMuteVolume(ActionEvent event) {
        if (mediaPlayer != null) {
            isMuted = !isMuted;

            if (isMuted) {
                volumeBeforeMute = mediaPlayer.getVolume();
                volumeSlider.setValue(0);
                mediaPlayer.setVolume(0);
            } else {
                volumeSlider.setValue(volumeBeforeMute * 100);
                mediaPlayer.setVolume(volumeBeforeMute);
            }
        }
    }

    private String formatTime(Duration current, Duration total) {
        int currentSeconds = (int) Math.floor(current.toSeconds());
        int totalSeconds = (int) Math.floor(total.toSeconds());

        int currentMinutes = currentSeconds / 60;
        int currentSecs = currentSeconds % 60;

        int totalMinutes = totalSeconds / 60;
        int totalSecs = totalSeconds % 60;

        return String.format("%02d:%02d / %02d:%02d", currentMinutes, currentSecs, totalMinutes, totalSecs);
    }

    public void shutdown() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
    }
}
