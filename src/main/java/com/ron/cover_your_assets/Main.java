package com.ron.cover_your_assets;

import java.net.ServerSocket;
import java.net.Socket;
import com.ron.cover_your_assets.controller.ClientThread;
import com.ron.cover_your_assets.controller.CoverYourAssetController;

public class Main {

	private final CoverYourAssetController controller;
    private boolean running = true;
    private static final int PORT = 3333;

    public Main() {
        this.controller = new CoverYourAssetController();
    }

    public void listen() {
    	try {
    		System.out.println("Waiting for connections...");
    		ServerSocket serverSocket = new ServerSocket(PORT);
            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("processing connection...");
                if (socket != null) {
                	ClientThread clientThread = new ClientThread(controller, socket);
                	clientThread.setDaemon(true);
                	clientThread.start();
                }
            }
            serverSocket.close();
    	} catch(Exception exception) {
    		// log
    	}
        
    }
    
    public static void main(String[] args) {
    	Main main = new Main();
    	main.listen();
    }
}
