import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.shape.SVGPath;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileCompressionApp extends Application {
    private Label statusLabel;
    private VBox fileInfoBox;
    private ComboBox<String> compressionLevelBox;
    private List<File> selectedFiles;
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.UNDECORATED);

        HBox mainContainer = new HBox();
        mainContainer.setStyle("-fx-background-color: linear-gradient(to right, #ffffff, #f8f9fa);" +
                "-fx-border-radius: 15;" +
                "-fx-background-radius: 15;");
        VBox leftPane = new VBox(20);

        leftPane.setPadding(new Insets(20));
        leftPane.setPrefWidth(450);
        leftPane.setAlignment(Pos.TOP_CENTER);

        mainContainer.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        mainContainer.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        DropShadow windowShadow = new DropShadow();
        windowShadow.setRadius(10.0);
        windowShadow.setOffsetX(3.0);
        windowShadow.setOffsetY(3.0);
        windowShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        mainContainer.setEffect(windowShadow);

        Label titleLabel = new Label("Easy Archive");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        titleLabel.setStyle("-fx-text-fill: #2196F3;");

        Label subtitleLabel = new Label("Compress and decompress files efficiently");
        subtitleLabel.setFont(Font.font("System", FontWeight.LIGHT, 16));
        subtitleLabel.setStyle("-fx-text-fill: #757575;");

        compressionLevelBox = new ComboBox<>();
        compressionLevelBox.getItems().addAll(
                "Arithmetic Algorithm",
                "Run-Length Encoding Algorithm",
                "Huffman Coding Algorithm",
                "Golomb code alggorithm",
                "Lempel-Ziv-Welch Algorithm");
        compressionLevelBox.setValue("Huffman Coding Algorithm");
        compressionLevelBox.setStyle("-fx-background-color: white;" +
                "-fx-border-color: #e0e0e0;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;" +
                "-fx-pref-width: 300px;");

        fileInfoBox = new VBox(10);
        fileInfoBox.setVisible(false);
        fileInfoBox.setStyle("-fx-background-color: #f8f9fa;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 15;");

        statusLabel = new Label("Ready to process files");
        statusLabel.setStyle("-fx-text-fill: #757575;");

        HBox closeButtonBox = new HBox();
        closeButtonBox.setAlignment(Pos.TOP_LEFT);
        Button closeButton = new Button("×");
        closeButton.setStyle("-fx-background-color: transparent; -fx-font-size: 20; -fx-text-fill: #666;");
        closeButton.setOnAction(e -> primaryStage.close());
        closeButtonBox.getChildren().add(closeButton);

        leftPane.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Label("Select Compression Algorithms:"),
                compressionLevelBox,
                fileInfoBox,
                statusLabel);

        leftPane.setAlignment(Pos.CENTER);

        VBox rightPane = new VBox();
        rightPane.setPrefWidth(450);
        rightPane.setAlignment(Pos.CENTER);
        rightPane.setPadding(new Insets(20));

        Button uploadButton = createUploadButton();
        uploadButton.setOnAction(e -> selectFiles(primaryStage, rightPane));

        rightPane.getChildren().addAll(

                new Region() {
                    {
                        setMinHeight(55);
                    }
                },
                uploadButton, new Label(""),
                new Label("Select Files to Compress") {
                    {
                        setTextFill(Color.GREY);
                        setFont(Font.font("System", 17));
                    }
                });

        mainContainer.getChildren().addAll(closeButtonBox, leftPane, rightPane);

        Scene scene = new Scene(mainContainer, 900, 600);
        primaryStage.setResizable(false);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createUploadButton() {
        Button uploadButton = new Button();

        SVGPath uploadIcon = new SVGPath();
        uploadIcon.setContent("M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z");
        uploadIcon.setFill(Color.WHITE);

        uploadButton.setGraphic(uploadIcon);
        uploadButton.setPrefSize(150, 150);
        uploadButton.setStyle(
                "-fx-background-color: #2196F3;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-cursor: hand;");

        uploadButton.setOnMouseEntered(e -> uploadButton.setStyle(
                "-fx-background-color: #1976D2;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-cursor: hand;"));

        uploadButton.setOnMouseExited(e -> uploadButton.setStyle(
                "-fx-background-color: #2196F3;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-cursor: hand;"));

        return uploadButton;
    }

    private void updateFileInfo(List<File> files) {
        fileInfoBox.getChildren().clear();
        fileInfoBox.setVisible(true);

        Label headerLabel = new Label("Selected Files:");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        fileInfoBox.getChildren().add(headerLabel);

        long totalSize = 0;
        for (File file : files) {
            totalSize += file.length();
            VBox fileBox = new VBox(5);
            fileBox.setStyle("-fx-background-color: #f8f8f8; -fx-padding: 10; -fx-background-radius: 5;");

            Label nameLabel = new Label("Name: " + file.getName());
            Label sizeLabel = new Label("Size: " + formatFileSize(file.length()));

            fileBox.getChildren().addAll(nameLabel, sizeLabel);
            fileInfoBox.getChildren().add(fileBox);
        }

        Label totalLabel = new Label("Total Size: " + formatFileSize(totalSize));
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        fileInfoBox.getChildren().add(totalLabel);
    }

    private String formatFileSize(long size) {
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private void selectFiles(Stage stage, VBox rightPane) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        if (files != null && !files.isEmpty()) {
            selectedFiles = files;
            updateFileInfo(files);
            showActionButtons(rightPane, stage);
        } else {
            statusLabel.setText("No files selected.");
        }
    }

    private void showActionButtons(VBox rightPane, Stage primaryStage) {
        {
            rightPane.getChildren().clear();

            VBox buttonsBox = new VBox(15);
            buttonsBox.setAlignment(Pos.CENTER);

            Button compressButton = new Button("Compress");
            compressButton.setPrefSize(200, 50);
            compressButton.setStyle(
                    "-fx-background-color: #2196F3;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;");
            compressButton.setOnAction(e -> compressFiles(selectedFiles));

            Button decompressButton = new Button("Decompress");
            decompressButton.setPrefSize(200, 50);
            decompressButton.setStyle(
                    "-fx-background-color: #4CAF50;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;");
            decompressButton.setOnAction(e -> decompressFiles());

            compressButton.setOnMouseEntered(e -> compressButton.setStyle(
                    "-fx-background-color: #1976D2;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;"));
            compressButton.setOnMouseExited(e -> compressButton.setStyle(
                    "-fx-background-color: #2196F3;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;"));

            decompressButton.setOnMouseEntered(e -> decompressButton.setStyle(
                    "-fx-background-color: #388E3C;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;"));
            decompressButton.setOnMouseExited(e -> decompressButton.setStyle(
                    "-fx-background-color: #4CAF50;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;"));

            Button backButton = new Button("← Back");
            backButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #666666;" +
                            "-fx-font-size: 14px;" +
                            "-fx-cursor: hand;");
            backButton.setOnAction(e -> resetToInitialState(rightPane, primaryStage));

            backButton.setOnMouseEntered(e -> backButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #333333;" +
                            "-fx-font-size: 14px;" +
                            "-fx-cursor: hand;"));
            backButton.setOnMouseExited(e -> backButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #666666;" +
                            "-fx-font-size: 14px;" +
                            "-fx-cursor: hand;"));

            buttonsBox.getChildren().addAll(
                    new Region() {
                        {
                            setMinHeight(60);
                        }
                    },
                    compressButton,
                    decompressButton,
                    new Region() {
                        {
                            setMinHeight(20);
                        }
                    },
                    backButton);

            rightPane.getChildren().add(buttonsBox);
        }
    }

    private void decompressFiles() {
        statusLabel.setText("Decompression feature will be implemented soon.");
    }

    private void compressFiles(List<File> files) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Compressed File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("binary Files", "*.bin"));
        File binaryFile = fileChooser.showSaveDialog(null);

        if (binaryFile != null) {
            Stage progressStage = new Stage();
            progressStage.initStyle(StageStyle.UNDECORATED);

            Label titleLabel = new Label("Compression Progress");
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            titleLabel.setStyle("-fx-text-fill: #1976D2;");

            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(350);
            progressBar.setPrefHeight(8); 
            progressBar.setStyle(
                    "-fx-accent: #2196F3;" +
                            "-fx-background-color: #E3F2FD;" 
            );

            HBox statusBox = new HBox(10);
            statusBox.setAlignment(Pos.CENTER_LEFT);

            SVGPath statusIcon = new SVGPath();
            statusIcon.setContent(
                    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z");
            statusIcon.setFill(Color.web("#1976D2"));
            statusIcon.setScaleX(0.7);
            statusIcon.setScaleY(0.7);

            Label progressLabel = new Label("Preparing to compress...");
            progressLabel.setStyle("-fx-text-fill: #424242; -fx-font-size: 14px;");

            statusBox.getChildren().addAll(statusIcon, progressLabel);

            Button cancelButton = new Button("Cancel");
            cancelButton.setPrefWidth(100);
            cancelButton.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #1976D2;" +
                            "-fx-border-color: #1976D2;" +
                            "-fx-border-radius: 20;" +
                            "-fx-background-radius: 20;" +
                            "-fx-font-size: 13px;" +
                            "-fx-padding: 8 15;" +
                            "-fx-cursor: hand;");

            cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(
                    "-fx-background-color: #1976D2;" +
                            "-fx-text-fill: white;" +
                            "-fx-border-color: #1976D2;" +
                            "-fx-border-radius: 20;" +
                            "-fx-background-radius: 20;" +
                            "-fx-font-size: 13px;" +
                            "-fx-padding: 8 15;" +
                            "-fx-cursor: hand;"));

            cancelButton.setOnMouseExited(e -> cancelButton.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #1976D2;" +
                            "-fx-border-color: #1976D2;" +
                            "-fx-border-radius: 20;" +
                            "-fx-background-radius: 20;" +
                            "-fx-font-size: 13px;" +
                            "-fx-padding: 8 15;" +
                            "-fx-cursor: hand;"));

            VBox progressBox = new VBox(15);
            progressBox.setAlignment(Pos.CENTER);
            progressBox.setPadding(new Insets(25));
            progressBox.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #E0E0E0;" +
                            "-fx-border-radius: 15;" +
                            "-fx-background-radius: 15;");

            progressBox.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), progressBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            DropShadow shadow = new DropShadow();
            shadow.setRadius(10.0);
            shadow.setOffsetY(3.0);
            shadow.setColor(Color.rgb(0, 0, 0, 0.2));
            progressBox.setEffect(shadow);


            progressBox.getChildren().addAll(
                    titleLabel,
                    new Region() {
                        {
                            setMinHeight(10);
                        }
                    },
                    statusBox,
                    progressBar,
                    cancelButton);

            Scene progressScene = new Scene(progressBox);
            progressScene.setFill(null);
            progressStage.setScene(progressScene);
            progressStage.setAlwaysOnTop(true);

            progressStage.show();

            final boolean[] isCancelled = { false };

            cancelButton.setOnAction(e -> {
                isCancelled[0] = true;
                progressStage.close();
            });

            Thread compressionThread = new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    FileCompressor fileCompressor = new FileCompressor();
                    String compressedFilePath = binaryFile.getAbsolutePath();
                    int totalFiles = files.size();

                    for (int i = 0; i < files.size(); i++) {
                        if (isCancelled[0]) {
                            Platform.runLater(() -> statusLabel.setText("Compression cancelled."));
                            break;
                        }

                        File file = files.get(i);
                        final int currentIndex = i;

                        Platform.runLater(() -> {
                            double progress = (double) currentIndex / totalFiles;
                            progressBar.setProgress(progress);
                            progressLabel.setText("Compressing: " + file.getName());
                        });

                        try {
                            switch (compressionLevelBox.getValue()) {
                                case "Arithmetic Algorithm":
                                    fileCompressor.compress("Arithmetic Algorithm", file, compressedFilePath);
                                    break;
                                case "Run-Length Encoding Algorithm":
                                    fileCompressor.compress("Run-Length Encoding Algorithm", file, compressedFilePath);
                                    break;
                                case "Huffman Coding Algorithm":
                                    fileCompressor.compress("Huffman Coding Algorithm", file, compressedFilePath);
                                    break;
                                case "Golomb code alggorithm":
                                    fileCompressor.compress("Golomb code alggorithm", file, compressedFilePath);
                                    break;
                                case "Lempel-Ziv-Welch Algorithm":
                                    fileCompressor.compress("Lempel-Ziv-Welch Algorithm", file, compressedFilePath);
                                    break;
                                default:
                                    fileCompressor.compress("Huffman Coding Algorithm", file, compressedFilePath);
                            }
                        } catch (IOException e) {
                            final String errorMessage = e.getMessage();
                            Platform.runLater(() -> {
                                statusLabel.setText("Error compressing file: " + errorMessage);
                                progressStage.close();
                            });
                            return;
                        }
                    }
                    if (!isCancelled[0]) {
                        Platform.runLater(() -> {
                            progressBar.setProgress(1.0);
                            statusLabel.setText("Files compressed successfully to: " + binaryFile.getAbsolutePath());
                            progressStage.close();
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Error during compression: " + e.getMessage());
                        progressStage.close();
                    });
                }
            });

            compressionThread.start();

        } else {
            statusLabel.setText("No destination file selected.");
        }
    }

    private void resetToInitialState(VBox rightPane, Stage primaryStage) {
        rightPane.getChildren().clear();

        Button uploadButton = createUploadButton();
        uploadButton.setOnAction(e -> selectFiles(primaryStage, rightPane));

        rightPane.getChildren().addAll(
                new Region() {
                    {
                        setMinHeight(55);
                    }
                },
                uploadButton,
                new Label(""),
                new Label("Select Files to Compress") {
                    {
                        setTextFill(Color.GREY);
                        setFont(Font.font("System", 17));
                    }
                });

        fileInfoBox.getChildren().clear();
        fileInfoBox.setVisible(false);
        statusLabel.setText("Status: Waiting for user action...");
        selectedFiles = null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

