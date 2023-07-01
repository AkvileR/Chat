package com.example.chat;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private String clientRoom;
    public ClientHandler(Socket socket){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            this.clientRoom = bufferedReader.readLine();
            logClient(clientUsername);
            if(Server.roomMap.containsKey(clientRoom)){
                Server.roomMap.get(clientRoom).add(this);
            }else{
                ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
                Server.roomMap.put(clientRoom,clientHandlers);
                Server.roomMap.get(clientRoom).add(this);
            }
            broadcastMessage("SERVER: "+clientUsername+" has entered the chat!");
        }catch(IOException e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }
    @Override
    public void run() {
        String messageFromClient;
        while(socket.isConnected()){
            try{
               messageFromClient = bufferedReader.readLine();
               if(messageFromClient.startsWith("(PRIVATE")){
                   broadcastToPerson(messageFromClient);
               }else{
                   broadcastMessage(messageFromClient);
               }
            }catch(IOException|NullPointerException e){
                closeEverything(socket,bufferedReader,bufferedWriter);
                break;
            }
        }
    }
    public void broadcastMessage(String messageToSend){
        ArrayList<ClientHandler> clientHandlers = Server.roomMap.get(clientRoom);
        for(ClientHandler clientHandler: clientHandlers){
            try{
                clientHandler.bufferedWriter.write(messageToSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            }catch(IOException e){
                closeEverything(socket, bufferedReader,bufferedWriter);
            }
        }
        writeToLog(messageToSend,clientRoom);
    }
    public void broadcastToPerson(String msg){
        int startIndex = msg.indexOf("(PRIVATE TO ") + "(PRIVATE TO ".length();
        int endIndex = msg.indexOf(")", startIndex);
        String recipient = msg.substring(startIndex, endIndex);
        ClientHandler dest = findClientHandler(recipient);
        try{
            if(!recipient.equals(clientUsername)){
                dest.bufferedWriter.write(msg);
                dest.bufferedWriter.newLine();
                dest.bufferedWriter.flush();
            }
            this.bufferedWriter.write(msg);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
        writeToLog(msg,clientRoom);
        if(!clientRoom.equals(dest.clientRoom)) {
            writeToLog(msg, dest.clientRoom);
        }
    }
    public ClientHandler findClientHandler(String recipient) {
        for (ArrayList<ClientHandler> clientHandlers : Server.roomMap.values()) {
            for (ClientHandler clientHandler : clientHandlers) {
                if (clientHandler.clientUsername.equals(recipient)) {
                    return clientHandler;
                }
            }
        }
        return null;
    }
    private void writeToLog(String msg,String room){
        String cleanedName = room.replaceAll("[^a-zA-Z0-9-_]+", "_");
        String fileName = cleanedName +"MsgLog.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(msg);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void logClient(String name){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("clients.txt", true))) {
            writer.write(name);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void removeClient(String name){
        File inputFile = new File("clients.txt");
        File tempFile = new File("temp.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.equals(name))
                    continue;
                writer.write(currentLine);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inputFile.delete()) {
            if (tempFile.renameTo(inputFile)) {
                System.out.println("Removed client from log: " + name);
            } else {
                System.out.println("Failed to rename temporary file: " + name);
            }
        } else {
            System.out.println("Failed to delete original file: " + name);
        }
    }
    public void removeClientHandler(){
        Server.roomMap.get(clientRoom).remove(this);
        broadcastMessage("SERVER: "+clientUsername+" has left the chat!");
        removeClient(clientUsername);
    }
    public void closeEverything(Socket socket,BufferedReader bufferedReader,BufferedWriter bufferedWriter){
        removeClientHandler();
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
}
