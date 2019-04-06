package com.ron.cover_your_assets.controller;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ron.cover_your_assets.model.Card;
import com.ron.cover_your_assets.model.Game;
import com.ron.cover_your_assets.model.Player;

public class CoverYourAssetController {
	
	private Map<String, Game> games = new TreeMap<String, Game>();
	private List<Card> cards;
	private static final int CODE_LENGTH = 4;
	private static final String DELIMITER = ":;";
	private transient static final String TRUE = "1";
	private transient static final String FALSE = "0";
	
	public CoverYourAssetController() {
		initCards();
	}
	
	private void initCards() {
		try {
			cards = CardFactory.getCards();
		} catch (FileNotFoundException e) {
			// log
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// log
			e.printStackTrace();
		} catch (IOException e) {
			// log
			e.printStackTrace();
		}
	}
	
	public void execute(String text, Socket socket) throws IOException {
		// if the code is not found the request is dropped
		String[] tokens = text.split(DELIMITER);
		int requestCode = Integer.parseInt(tokens[0].trim());
		if (requestCode == RequestCode.CRTGM.getNumber()) {
			createGame(tokens, socket);
		} else if (requestCode == RequestCode.JNGM.getNumber()) {
			joinGame(tokens, socket);
		} else if (requestCode == RequestCode.RCNNCT.getNumber()) {
			reconnect(tokens, socket);
		} else if (requestCode == RequestCode.DWNLD.getNumber()) {
			upload(socket, tokens);
		} else if (requestCode == RequestCode.GTCRDS.getNumber()) {
			getCards(tokens);
		} else if (requestCode == RequestCode.STL.getNumber()) {
			steal(tokens);
		} else if (requestCode == RequestCode.YLD.getNumber()) {
			yield(tokens);
		} else if (requestCode == RequestCode.DSCRD.getNumber()) {
			discard(tokens);
		} else if (requestCode == RequestCode.NSTRCTN.getNumber()) {
			getInstructions(tokens);
		} else if (requestCode == RequestCode.CVRDSCRD.getNumber()) {
			coverFromDiscard(tokens);
		} else if (requestCode == RequestCode.CVRWN.getNumber()) {
			coverWithOwn(tokens);
		} else if (requestCode == RequestCode.GTSCRS.getNumber()) {
			getScores(tokens);
		} else if (requestCode == RequestCode.GTWNSCR.getNumber()) {
			getOwnScore(tokens);
		} else if (requestCode == RequestCode.STRT.getNumber()) {
			startGame(tokens); 
		} else if (requestCode == RequestCode.DRW.getNumber()) {
			draw(tokens); 
		}
	}
	
	private void startGame(String[] tokens) throws IOException { 
		String code = tokens[1].trim();
		int playerId = Integer.parseInt(tokens[2].trim());
		Game game = games.get(code);
		if (game != null) {
			boolean success = game.start(playerId, cards);
			if (success) {
				String message = game.getInitiator().getUsername() + " has started the game";
				game.sendToAll(ResponseCode.RSTRT, true, TRUE, Integer.toString(playerId), message);
			} else {
				System.out.println("You are not allowed to add an asset");
				// log
			}
		}
	}

	private void getOwnScore(String[] tokens) {
		// to be completed
	}

	private void getScores(String[] tokens) {
		// to be completed 
	}

	private void coverWithOwn(String[] tokens) throws IOException { 
		String code = tokens[1].trim();
		int playerId = Integer.parseInt(tokens[2].trim());
		int card1Id = Integer.parseInt(tokens[3].trim());
		int card2Id = Integer.parseInt(tokens[4].trim());
		Game game = games.get(code);
		if (game != null) {
			Player player = game.getPlayers().get(playerId);
			Card card1 = cards.get(card1Id);
			Card card2 = cards.get(card2Id);
			boolean success = game.coverWithOwnCards(player, card1, card2);
			if (success) {
				String message = player.getUsername() + " created a new asset";
				game.sendToAll(ResponseCode.RCVRWN, true, TRUE, Integer.toString(playerId), message, card1.getText());
			} else {
				System.out.println("You are not allowed to add an asset");
				// log
			}
		}
	}

	private void coverFromDiscard(String[] tokens) throws IOException {
		String code = tokens[1].trim();
		int playerId = Integer.parseInt(tokens[2].trim());
		int cardId = Integer.parseInt(tokens[3].trim());
		Game game = games.get(code);
		if (game != null) {
			Player player = game.getPlayers().get(playerId);
			Card card = cards.get(cardId);
			boolean success = game.coverFromDiscardPile(player, card);
			if (success) {
				String message = player.getUsername() + " created a new asset";
				game.sendToAll(ResponseCode.RCVRDSCRD, true, TRUE, Integer.toString(playerId), message, card.getText());
			} else {
				System.out.println("You are not allowed to add an asset from discard pile");
				// log
			}
		}
	}

	private void getInstructions(String[] tokens) {
		// to be implemented
	}

	private void discard(String[] tokens) throws IOException {
		String code = tokens[1].trim();
		int playerId = Integer.parseInt(tokens[2].trim());
		int cardId = Integer.parseInt(tokens[3].trim());
		Game game = games.get(code);
		if (game != null) {
			Player player = game.getPlayers().get(playerId);
			Card card = cards.get(cardId);
			boolean success = game.discard(player, card);
			if (success) {
				String message = player.getUsername() + " added '" + card.getText() + "' to the discard pile";
				game.sendToAll(ResponseCode.RDSCRD, true, TRUE, Integer.toString(playerId), message, card.getText());
			} else {
				System.out.println("You are not allowed to discard");
				// log
			}
		}
	}
	
	private void draw(String[] tokens) throws IOException {
		String code = tokens[1].trim();
		int playerId = Integer.parseInt(tokens[2].trim());
		Game game = games.get(code);
		if (game != null) {
			Player player = game.getPlayers().get(playerId);
			Player counterpartPlayer = game.getPlayers().get(player.getCanStillToID());
			boolean success = game.draw(player, counterpartPlayer);
			if (success) {
				String message = player.getUsername() + " and " + counterpartPlayer.getUsername() + " drew cards";
				game.sendToAll(ResponseCode.RDRW, true, TRUE, Integer.toString(playerId), message, player.getUsername(), counterpartPlayer.getUsername());
			} else {
				System.out.println("You are not allowed to draw");
				// log
			}
		}
	}

	private void yield(String[] tokens) throws IOException {
		String code = tokens[1].trim();
		int yielderId = Integer.parseInt(tokens[2].trim());
		int yieldedToId = Integer.parseInt(tokens[3].trim());
		Game game = games.get(code);
		if (game != null) {
			Player yielder = game.getPlayers().get(yielderId);
			Player yielded = game.getPlayers().get(yieldedToId);
			boolean success = game.yield(yielder, yielded);
			if (success) {
				String message = yielder.getUsername() + " lost the fight from " + yielded.getUsername();
				game.sendToAll(ResponseCode.RYLD, true, TRUE, Integer.toString(yielderId), message, yielder.getUsername(), yielded.getUsername());
			} else {
				System.out.println("You are not allowed to yield");
				// log
			}
		}
	}

	private void steal(String[] tokens) throws IOException {
		// game code, attacker's id, attacked id, attacker's card id
		String code = tokens[1].trim();
		int attackerId = Integer.parseInt(tokens[2].trim());
		int attackedId = Integer.parseInt(tokens[3].trim());
		int cardId = Integer.parseInt(tokens[4].trim());
		Game game = games.get(code);
		if (game != null) {
			Player attacker = game.getPlayers().get(attackerId);
			Player attacked = game.getPlayers().get(attackedId);
			Card card = cards.get(cardId);
			boolean stolen = game.steal(attacker, attacked, card);
			if (stolen) {
				String message = attacker.username + " stole assets from " + attacked.username;
				game.sendToAll(ResponseCode.RSTL, true, TRUE, Integer.toString(attackerId), message, attacker.getUsername(), attacked.getUsername());
			} else {
				System.out.println("You are not allowed to steal");
				// log
			}
		}
	}

	private void getCards(String[] tokens) {
		// not implemented, cards saved in client apps
	}

	private void upload(Socket socket, String[] tokens) {
		CardFactory.uploadFile(socket, tokens[1].trim());
	}

	private void reconnect(String[] tokens, Socket socket) throws IOException {
		// game code, player_id
		// close the current writer
		// set the new writer at the same position
		String code = tokens[1].trim();
		int playerId = Integer.parseInt(tokens[2].trim());
		Game game = games.get(code);
		BufferedWriter bufferedWriter = getBufferedWriter(socket);
		if (game != null) {
			Player player = game.getPlayers().get(playerId);
			if (player != null) {
				game.closeWriter(playerId);
				game.getAllWriters().set(playerId, bufferedWriter);
				String gameJson = game.generateGameJson(player);
				String response = ResponseFactory.generateRequestString(ResponseCode.RRCNNCT, gameJson, TRUE, Integer.toString(playerId), "connected again");
				game.send(bufferedWriter, response);
			} else {
				bufferedWriter.close();
			}
		} else {
			bufferedWriter.close();
		}
	}

	private void joinGame(String[] tokens, Socket socket) throws IOException {
		// from index 1: game code, random_request _id, player's name
		// only one player (thread) can access the game at a time, so the synchronized block will be used when adding the player
		String code = tokens[1].trim();
		String requestId = tokens[2].trim();
		String playerName = tokens[3].trim();
		System.out.println("trying game doesn't exist.");
		Game game = games.get(code);
		System.out.println(game);
		BufferedWriter bufferedWriter = getBufferedWriter(socket);
		if (game != null) {
			Player player = game.join(playerName, bufferedWriter); 
			if (player != null) {
				game.sendToAll(ResponseCode.RJNGM, true, TRUE, requestId, playerName + " has joined the game", playerName, Integer.toString(player.getOrder()), game.getCode());
			} else {
				String response = ResponseFactory.generateRequestString(ResponseCode.RJNGM, null, FALSE, requestId, "name already taken or game unavailable.");
				game.send(bufferedWriter, response);
			}
		} else {
			System.out.println("game doesn't exist.");
			String response = ResponseFactory.generateRequestString(ResponseCode.RJNGM, null, FALSE, requestId, "game with code: " + code + " not found.");
			System.out.println(response + "===");
			bufferedWriter.write(ResponseFactory.generateRequestString(ResponseCode.RJNGM, null, FALSE, requestId, "game with code: " + code + " not found.")); 
			bufferedWriter.flush();
		}
	}


	private void createGame(String[] tokens, Socket socket) throws IOException {
		// starting from index 1: game name, username, number of cards
		// create the game
		// add the game to the map
		// create the buffered-writer for the socket
		// add the writer in the game writers
		// generate output and write it to the writer
		String gameName = tokens[1].trim();
		String username = tokens[2].trim();
		int numberOfCardsPerPlayer = Integer.parseInt(tokens[3]);
		Game game = new Game(username, gameName, numberOfCardsPerPlayer);
		assignGameUniqueCode(game);
		BufferedWriter bufferedWriter = getBufferedWriter(socket);
		game.getAllWriters().add(bufferedWriter);
		String response = ResponseFactory.generateRequestString(ResponseCode.RCRTGM, game.generateGameJson(game.getInitiator()), TRUE, "game created", 
				Integer.toString(game.getInitiator().getOrder()), game.getCode());
		game.send(bufferedWriter, response); 
	}
	
	private BufferedWriter getBufferedWriter(Socket socket) throws IOException { 
		return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	}

	private synchronized void assignGameUniqueCode(Game newGame) {
		// generate code and check if it is not currently in use
		// the game has to be added to the map here to avoid other threads from using the same code
		while (true) {
			String code = CodeFactory.generateCode(CODE_LENGTH);
			Game game = games.get(code);
			if (game == null) {
				newGame.setCode(code);
				games.put(code, newGame);
				return;
			}
		}
	}
	
}
