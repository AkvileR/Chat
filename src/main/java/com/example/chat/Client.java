package com.example.chat;

import java.io.*;
import java.net.Socket;

public class Client{
    public Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private String msgFromServer;
    private String room;
    private ClientUIController controller;
    public Client(Socket socket,String username,ClientUIController c,String room){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
            this.controller = c;
            this.room = room;
        }catch(IOException e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }
    public void sendMessage(String msg) throws IOException{
        bufferedWriter.write(username+": "+msg);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
    public void sendPrivateMessage(String msg,String recipient) throws IOException{
        bufferedWriter.write("(PRIVATE TO "+recipient+") "+username+": "+msg);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
    public void sendJoinMessage() throws IOException {
        try{
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            bufferedWriter.write(room);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }
    public void listenForMessage(){
        new Thread(() -> {
            while(socket.isConnected()){
                try{
                    msgFromServer = bufferedReader.readLine();
                    controller.addMessage(msgFromServer);
                }catch(IOException e){
                    closeEverything(socket,bufferedReader,bufferedWriter);
                }
            }
        }).start();
    }
    public void closeEverything(Socket socket,BufferedReader bufferedReader,BufferedWriter bufferedWriter){
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static Client createClient(String username, ClientUIController c,String r)throws IOException{
        Socket socket = new Socket("localhost",1234);
        Client client = new Client(socket, username,c,r);
        System.out.println("Connected");
        client.sendJoinMessage();
        client.listenForMessage();
        return client;
    }
}
