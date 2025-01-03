package view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.RecordEntry;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CommuteTimerView {
    private final Label timerLabel = new Label("00시간 00분");
    private final Label arrivalTimeLabel = new Label("");
    private final Label leaveTimeLabel = new Label("");
    private final Button arrivalButton = new Button("출근");
    private final Button leaveButton = new Button("퇴근");
    private final Button viewRecordsButton = new Button("기록 보기");

    private Timeline timeline;
    private LocalDateTime startTime;
    private final String employeeName;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분");
    private final DateTimeFormatter fileDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final String encryptionKey = "simplekey";

    public CommuteTimerView(String employeeName) {
        this.employeeName = employeeName;
    }

    public VBox createTimerView() {
        Label nameLabel = new Label(employeeName + "님의 출퇴근 타이머");
        nameLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/font/NanumGothicBold.ttf"), 25));
        nameLabel.setPadding(new Insets(20));

        timerLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/font/NanumGothicBold.ttf"), 35));

        arrivalTimeLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/font/NanumGothicBold.ttf"), 15));
        arrivalTimeLabel.setStyle("-fx-text-fill: #4CAF50;");

        leaveTimeLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/font/NanumGothicBold.ttf"), 15));
        leaveTimeLabel.setStyle("-fx-text-fill: #F44336;");

        styleButton(arrivalButton, "#4CAF50");
        styleButton(leaveButton, "#F44336");
        styleButton(viewRecordsButton, "#2196F3");

        leaveButton.setDisable(true);

        arrivalButton.setOnAction(event -> handleArrival());
        leaveButton.setOnAction(event -> handleLeave());
        viewRecordsButton.setOnAction(event -> showRecordViewer());

        HBox buttonBox = new HBox(20, arrivalButton, leaveButton, viewRecordsButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox vbox = new VBox(20, nameLabel, timerLabel, arrivalTimeLabel, leaveTimeLabel, buttonBox);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(50));

        // 레이블과 버튼 사이의 여백 추가
        VBox.setMargin(buttonBox, new Insets(30, 0, 0, 0)); // 버튼 상단에 30px 여백 추가

        return vbox;
    }

    private void handleArrival() {
        startTime = LocalDateTime.now();
        arrivalTimeLabel.setText("출근 : " + startTime.format(dateTimeFormatter));
        leaveTimeLabel.setText("");
        arrivalButton.setDisable(true);
        leaveButton.setDisable(false);
        startTimer();
    }

    private void handleLeave() {
        LocalDateTime endTime = LocalDateTime.now();
        leaveTimeLabel.setText("퇴근 : " + endTime.format(dateTimeFormatter));
        arrivalButton.setDisable(false);
        leaveButton.setDisable(true);
        stopTimer();

        java.time.Duration duration = java.time.Duration.between(startTime, endTime);

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        String totalWorkTime = String.format("%02d시간 %02d분", hours, minutes);

        String arrivalTime = startTime.format(fileDateTimeFormatter);
        String leaveTime = endTime.format(fileDateTimeFormatter);

        saveRecord(arrivalTime, leaveTime, totalWorkTime);
    }

    private void saveRecord(String arrivalTime, String leaveTime, String totalWorkTime) {
        String fileName = "data/record_" + employeeName + ".csv";
        String record = String.format("%s,%s,%s,%s", employeeName, arrivalTime, leaveTime, totalWorkTime);

        try (FileOutputStream fos = new FileOutputStream(fileName, true)) {
            String encryptedRecord = xorEncrypt(record, encryptionKey);
            String encodedRecord = Base64.getEncoder().encodeToString(encryptedRecord.getBytes("UTF-8"));

            fos.write(encodedRecord.getBytes());
            fos.write(System.lineSeparator().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showRecordViewer() {
        Stage recordStage = new Stage();
        recordStage.initModality(Modality.APPLICATION_MODAL);
        recordStage.setTitle(employeeName + "님의 출퇴근 기록");

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker(LocalDate.now());

        // 시작 날짜를 최근 5일 전으로 설정
        startDatePicker.setValue(LocalDate.now().minusDays(5));

        Button searchButton = new Button("조회");

        TableView<RecordEntry> tableView = createRecordTableView();
        Label totalWorkTimeLabel = new Label("총 근무 시간: 00시간 00분");

        // 조회 버튼 클릭 시 동작
        searchButton.setOnAction(event -> {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            List<RecordEntry> records = loadRecords(startDate, endDate);
            tableView.getItems().setAll(records);
            totalWorkTimeLabel.setText("총 근무 시간: " + calculateTotalWorkTime(records));
        });

        // 기록 보기 창을 열 때 자동으로 최근 5일 기록 조회
        List<RecordEntry> initialRecords = loadRecords(startDatePicker.getValue(), endDatePicker.getValue());
        tableView.getItems().setAll(initialRecords);
        totalWorkTimeLabel.setText("총 근무 시간: " + calculateTotalWorkTime(initialRecords));

        HBox datePickers = new HBox(10, new Label("시작 날짜:"), startDatePicker, new Label("종료 날짜:"), endDatePicker, searchButton);
        datePickers.setAlignment(Pos.CENTER);
        datePickers.setPadding(new Insets(10));

        VBox layout = new VBox(10, datePickers, tableView, totalWorkTimeLabel);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 700, 500);
        recordStage.setScene(scene);
        recordStage.show();
    }

    private List<RecordEntry> loadRecords(LocalDate startDate, LocalDate endDate) {
        String fileName = "data/record_" + employeeName + ".csv";
        List<RecordEntry> records = new ArrayList<>();

        if (startDate == null || endDate == null) {
            return records;
        }

        try {
            Files.lines(Paths.get(fileName)).forEach(line -> {
                try {
                    // Base64 디코딩 및 XOR 복호화
                    String decodedLine = new String(Base64.getDecoder().decode(line), "UTF-8");
                    String decryptedLine = xorEncrypt(decodedLine, encryptionKey);

                    String[] parts = decryptedLine.split(",");
                    if (parts.length == 4) {
                        LocalDate recordDate = LocalDate.parse(parts[1].split(" ")[0]);
                        if (!recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)) {
                            records.add(new RecordEntry(formatDateTime(parts[1]), formatDateTime(parts[2]), parts[3]));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // 기록을 날짜 기준으로 정렬 (오름차순: 예전 날짜 -> 최근 날짜)
            records.sort((r1, r2) -> {
                LocalDateTime dateTime1 = LocalDateTime.parse(r1.getArrivalTime(), DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"));
                LocalDateTime dateTime2 = LocalDateTime.parse(r2.getArrivalTime(), DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"));
                return dateTime1.compareTo(dateTime2);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return records;
    }

    private TableView<RecordEntry> createRecordTableView() {
        TableView<RecordEntry> tableView = new TableView<>();

        TableColumn<RecordEntry, String> arrivalColumn = new TableColumn<>("출근 시간");
        arrivalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArrivalTime()));
        arrivalColumn.setPrefWidth(220);

        TableColumn<RecordEntry, String> leaveColumn = new TableColumn<>("퇴근 시간");
        leaveColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLeaveTime()));
        leaveColumn.setPrefWidth(220);

        TableColumn<RecordEntry, String> workDurationColumn = new TableColumn<>("총 근무 시간");
        workDurationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWorkDuration()));
        workDurationColumn.setPrefWidth(200);

        tableView.getColumns().addAll(arrivalColumn, leaveColumn, workDurationColumn);
        tableView.setPrefHeight(400);

        return tableView;
    }


    private String xorEncrypt(String data, String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            result.append((char) (data.charAt(i) ^ key.charAt(i % key.length())));
        }
        return result.toString();
    }

    private void startTimer() {
        timerLabel.setText("00시간 00분");
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTimer() {
        if (startTime != null) {
            LocalDateTime now = LocalDateTime.now();
            java.time.Duration duration = java.time.Duration.between(startTime, now);

            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            timerLabel.setText(String.format(String.format("%02d시간 %02d분", hours, minutes)));
        }
    }

    private void stopTimer() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void styleButton(Button button, String color) {
        button.setFont(Font.loadFont(getClass().getResourceAsStream("/font/NanumGothicBold.ttf"), 20));
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 15;" +      // 모서리를 둥글게 설정
                        "-fx-padding: 15 20;"               // 세로 크기 확장을 위해 패딩 설정
        );
        button.setPrefWidth(150);
        button.setPrefHeight(60);               // 버튼의 세로 크기를 키움
    }


    private String formatDateTime(String dateTimeStr) {
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"));
    }

    private String calculateTotalWorkTime(List<RecordEntry> records) {
        long totalMinutes = records.stream()
                .mapToLong(record -> {
                    String[] parts = record.getWorkDuration().split("시간|분");
                    long hours = Long.parseLong(parts[0].trim());
                    long minutes = Long.parseLong(parts[1].trim());
                    return hours * 60 + minutes;
                })
                .sum();

        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%02d시간 %02d분", hours, minutes);
    }

}
