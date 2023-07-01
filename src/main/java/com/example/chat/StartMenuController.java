package com.example.chat;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class StartMenuController implements Initializable {
    @FXML
    private TextField usernameBox;
    @FXML
    private TextField roomBox;
    @FXML
    private ListView<String> roomView;
    public List<String> roomList = new ArrayList<>();
    @FXML
    public void enterData(){
        String username = usernameBox.getText();
        String selectedRoom = roomView.getSelectionModel().getSelectedItem();
        Stage currentStage = (Stage) usernameBox.getScene().getWindow();
        currentStage.close();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("clientUI.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            ClientUIController clientController = fxmlLoader.getController();
            clientController.setUsername(username);
            clientController.setRoom(selectedRoom);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void addRoom(){
        String room = roomBox.getText();
        roomView.getItems().add(room);
        updateRoomFile(room);
    }
    private void updateRoomFile(String room){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("rooms.txt", true))) {
            writer.newLine();
            writer.write(room);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadRoomsFromFile(){
        try (BufferedReader reader = new BufferedReader(new FileReader("rooms.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    roomList.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadRoomsFromFile();
        roomView.getItems().addAll(roomList);
    }
}
