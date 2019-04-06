package com.ron.cover_your_assets.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThread extends Thread {

    private final Socket socket;
    private final CoverYourAssetController controller;

    public ClientThread(CoverYourAssetController controller, Socket socket) {
        this.socket = socket;
        this.controller = controller;
    }

    @Override
    public void run() {
        processMessage(socket);
    }

	private void processMessage(Socket socket) {
		try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String receivedData;
            while ((receivedData = reader.readLine()) != null) {
            	controller.execute(receivedData, socket);
            }
        } catch (IOException ex) {
        	// no better way of ending the loop than closing the client connection
            System.out.println("socket closed.");
        }
	}  
}
