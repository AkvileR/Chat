package com.example.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    private final ServerSocket serverSocket;
    public static HashMap<String, ArrayList<ClientHandler>> roomMap;
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        roomMap = new HashMap<>();
    }
    public void startServer(){
        try{
            System.out.println("Server launched.");
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("A user connected.");
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }catch (IOException e){
            closeServerSocket();
        }
    }
    public void closeServerSocket(){
        try{
            if(serverSocket != null){
                serverSocket.close();
                System.out.println("Server closed.");
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
