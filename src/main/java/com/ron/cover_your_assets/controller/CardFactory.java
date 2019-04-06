package com.ron.cover_your_assets.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.ron.cover_your_assets.model.Card;

public class CardFactory {
	
	private static final String CARD_OBJECT_PATH = "data";
	private static final String CARD_FILE_TEXT_DATAPATH = "card_text";
	private static final String CARD_IMAGES_FOLDER = "cards/";
	private static final int BUFFER_SIZE = 4 * 1024;
	private CardFactory() {}
	
	public static List<Card> getCards() throws FileNotFoundException, ClassNotFoundException, IOException {
		List<Card> cards = readFromFile();
		if (cards == null) {
			cards = createCards();
			saveCards(cards);
		}
		return cards;
	}
	
	public static void uploadFile(Socket socket, String path) {
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(getFile(path)), BUFFER_SIZE);
			BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE);
			byte[] data = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = bis.read(data)) != -1) {
				bos.write(data, 0, bytesRead);
			}
			bos.flush();
			bos.close();
			bis.close();
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) { 
			
		}
	}
	
	private static void saveCards(List<Card> cards) throws FileNotFoundException, IOException {
		ObjectOutputStream ous = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getFile(CARD_OBJECT_PATH))));
		ous.writeObject(cards); 
		ous.close();
	}

	@SuppressWarnings("unchecked")
	private static List<Card> readFromFile() throws FileNotFoundException, IOException, ClassNotFoundException {
		File file = getFile(CARD_OBJECT_PATH);
		if (file == null || !file.exists()) {
			return null;
		}
		ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
		List<Card> cardList = (List<Card>) ois.readObject(); 
		ois.close();
		return cardList;
	}
	
	private static File getFile(String fileURI) {
		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			File file = new File(classLoader.getResource(CARD_IMAGES_FOLDER).getFile());
			file = new File(file.getAbsolutePath() + File.separatorChar + fileURI);
			return file;
		} catch (NullPointerException e) {
			// the file does not exist
			return null;
		}
		
	}
	
	private static List<Card> createCards() throws IOException {
		// card information is saved in the cards/card_text file
		// each line contains the details of one type of asset
		// the details are delimited by a ":;" in the following format
		// number_of_cards :; value :; text :; image_name :; wild_card
		// for wild_card, 0 means not wild card, 1 means is wild card
		File file = getFile(CARD_FILE_TEXT_DATAPATH);
		String content = new String(Files.readAllBytes(file.toPath()));
		List<Card> cards = new ArrayList<Card>();
		String[] cardLines = content.split("\r\n");
		int id = 0;
		for (String cardLine : cardLines) {
			String[] tokens = cardLine.split(":;");
			int numberOfCards = Integer.parseInt(tokens[0].trim());
			for (int i = 0; i < numberOfCards; i++) {
				Card card = createCard(id, tokens);
				cards.add(card);
				id++;
			}
		}
		return cards;
	}
	
	private static Card createCard(int id, String[] tokens) {
		// number_of_cards :; value :; text :; image_name :; wild_card
		// for wild_card, 0 means not wild card, 1 means is wild card
		int type = Integer.parseInt(tokens[1].trim());
		int value = Integer.parseInt(tokens[2].trim());
		String text = tokens[3].trim();
		String imageName = tokens[4].trim();
		boolean wildCard = getWildCard(Integer.parseInt(tokens[5].trim()));
		Card card = new Card(id, type, value, text, CARD_IMAGES_FOLDER + imageName, wildCard);
		return card;
	}

	private static boolean getWildCard(int wildCardID) {
		return (wildCardID == 0) ? false : true;
	}
}
