package com.example.chat;

import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


import java.io.*;

public class ClientUIController {
    @FXML
    private TextField tfMessage;
    @FXML
    private ListView<String> messageView;
    @FXML
    private Label label;
    @FXML
    private ListView<String> clientView;
    private String username;
    private Client client;
    private String room;
    public void setUsername(String username) {
        this.username = username;
    }
    public void setRoom(String room) {
        this.room = room;
    }
    public void connect()throws IOException{
        label.setText(room+": "+username);
        client = Client.createClient(username,this,room);
    }
    public void addMessage(String msg){
        Platform.runLater(() -> messageView.getItems().add(msg));
    }
    @FXML
    public void updateList(){
        clientView.getItems().removeAll();
        try (BufferedReader reader = new BufferedReader(new FileReader("clients.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    clientView.getItems().add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void leaveRoom()throws IOException{
        client.socket.close();
        Stage currentStage = (Stage) label.getScene().getWindow();
        currentStage.close();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("startMenu.fxml"));
        Parent root = fxmlLoader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    }
    @FXML
    public void sendMessage() throws IOException {
        String msg = tfMessage.getText();
        client.sendMessage(msg);
    }
    @FXML
    public void sendPrivateMessage()throws IOException{
        String msg = tfMessage.getText();
        String selectedUser = clientView.getSelectionModel().getSelectedItem();
        client.sendPrivateMessage(msg,selectedUser);
    }
}