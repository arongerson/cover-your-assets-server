package com.ron.cover_your_assets.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.ron.cover_your_assets.controller.ResponseCode;
import com.ron.cover_your_assets.controller.ResponseFactory;

public class Game {
	
	@Expose
	private String code;
	@Expose
	private String name;
	@Expose
	private Date createdDate;
	@Expose
	private Deck deck;
	@Expose
	private List<Player> players = new ArrayList<Player>();
	@Expose
	private Stack<Card> discardPile;
	@Expose
	private int cardsPerPlayer;
	public static final int MAX_NUM_OF_CARDS_PER_PLAYER = 5;
	public static final int MIN_NUM_OF_CARDS_PER_PLAYER = 4;
	private List<BufferedWriter> playerBufferedWriters = new ArrayList<BufferedWriter>();
	@Expose
	private boolean isOver;
	@Expose
	private boolean hasStarted;
	@Expose
	private int inTurn;
	
	public Game(String creatorName, String gameName, int cardsPerPlayer) {
		this.name = gameName;
		this.cardsPerPlayer = cardsPerPlayer;
		this.createdDate = Calendar.getInstance().getTime();
		this.isOver = false;
		hasStarted = false;
		inTurn = 0;
		setCardsPerPlayer();
		createPlayer(creatorName, true);
	}
	
	public synchronized void setCode(String code) {
		this.code =code;
	}

	private final void createPlayer(String name, boolean initiator) {
		// order starts with zero, equal to the size of the players list before the player is added
		Player player = new Player(players.size(), name, initiator);
		players.add(player);
	}
	
	private void setCardsPerPlayer() {
		// sets the number of cards per player within a reasonable range
		if (cardsPerPlayer < MIN_NUM_OF_CARDS_PER_PLAYER) {
			cardsPerPlayer = MIN_NUM_OF_CARDS_PER_PLAYER;
		} else if (cardsPerPlayer > MAX_NUM_OF_CARDS_PER_PLAYER) {
			cardsPerPlayer = MAX_NUM_OF_CARDS_PER_PLAYER;
		}
	}
	
	public synchronized Player getInitiator() {
		return players.get(0);
	}
	
	public synchronized boolean start(int playerId, List<Card> cardList) {
		Player player = players.get(0);
		if (player.getOrder() == playerId && !hasStarted) {
			player.setStart();
			deck = new Deck(cardList);
			deck.shuffle();
			dealCardsToPlayers();
			addInitialCardToDiscardPile();
			hasStarted = true;
			inTurn = player.getOrder();
			return true;
		}
		return false;
	}
	
	private void addInitialCardToDiscardPile() {
		discardPile = new Stack<Card>();
		Card card = deck.draw(1).get(0);
		discardPile.push(card);
	}

	private void dealCardsToPlayers() {
		for (Player player : players) {
			player.setPile(deck.draw(cardsPerPlayer));
		}
	}
	
	public void setInTurn(int playerId) {
		inTurn = playerId;
	}

	public synchronized int getCardsPerPlayer() {
		return cardsPerPlayer;
	}

	public synchronized String getName() {
		return name;
	}

	public synchronized Date getCreatedDate() {
		return createdDate;
	}

	public synchronized Deck getDeck() {
		return deck;
	}
	
	public synchronized String getCode() {
		return code;
	}

	public synchronized List<Player> getPlayers() {
		return players;
	}
	
	public synchronized List<BufferedWriter> getAllWriters() {
		return playerBufferedWriters;
	}

	public synchronized Player join(String playerName, BufferedWriter bufferedWriter) {
		if (validateJoiningGame(playerName)) {
			int order = players.size();
			Player player = new Player(order, playerName, false);
			players.add(player);
			playerBufferedWriters.add(bufferedWriter);
			return player;
		}
		return null;
	}
	
	private boolean validateJoiningGame(String playerName) {
		for (Player player : players) {
			if (player.getUsername().equalsIgnoreCase(playerName)) {
				return false;
			}
		}
		return !hasStarted;
	}

	public synchronized boolean draw(Player player, Player counterpartPlayer) {
		if (isValidDraw(player)) {
			return player.draw(this, counterpartPlayer);
		}
		return false;
	}
	
	private boolean isValidDraw(Player player) {
		return player.canDraw();
	}

	public synchronized boolean steal(Player attacker, Player attacked, Card card) {
		if (isValidSteal(attacker, attacked, card)) {
			return attacker.steal(this, attacked, card);
		}
		return false;
	}
	
	private boolean isValidSteal(Player attacker, Player attacked, Card card) {
		// if the player owns the card do the stealing
		// check if the offender has the right to steal
		// check if the offender has the right to steal from the offended
		if (!attacker.canSteal()) {
			System.out.println("can't steal");
			return false;
		} else if (!attacker.hasRightToStealTo(attacked)) {
			System.out.println("no right to");
			return false;
		} 
		return attacker.ownsCard(card);
	}
	
	public synchronized boolean coverFromDiscardPile(Player player, Card card) {
		if (isValidCoverFromDisacrdPile(player, card)) {
			return player.coverFromDiscardPile(this, card);
		}
		return false;
	}
	
	private boolean isValidCoverFromDisacrdPile(Player player, Card card) {
		// check if the player owns the card
		// check if the discard pile is not empty
		// check if the player has the right to cover
		if (!player.canCover()) {
			return false;
		} else if (discardPile.isEmpty()) {
			return false;
		}
		return player.ownsCard(card);
	}

	public synchronized boolean coverWithOwnCards(Player player, Card card1, Card card2) {
		if (isValidCoverWithOwnCards(player, card1, card2)) {
			return player.coverWithOwnCards(this, card1, card2);
		}
		return false;
	}
	
	private boolean isValidCoverWithOwnCards(Player player, Card card1, Card card2) {
		// check if the player has the right to cover
		// check if the player owns the cards
		if (!player.canCover()) {
			return false;
		}
		return player.ownsCard(card1) && player.ownsCard(card2);
	}

	public synchronized boolean discard(Player player, Card card) {
		if (isValidDiscard(player, card)) {
			return player.discard(this, card);
		}
		return false;
	}
	
	private boolean isValidDiscard(Player player, Card card) {
		// check if the player has the right to discard
		// check if the player owns the card he is discarding
		if (!player.canDiscard()) {
			return false;
		}
		return player.ownsCard(card);
	}

	public synchronized boolean yield(Player player, Player receiver) {
		if (isValidYield(player, receiver)) {
			return player.yield(this, receiver);
		}
		return false;
	}

	private boolean isValidYield(Player player, Player receiver) {
		// check if the player has the right to yield
		// check if the player can yield to the receiver
		if (!player.canYield()) {
			return false;
		}
		return player.canYieldTo(receiver);
	}

	public synchronized void addToDiscardPile(Card card) {
		discardPile.add(card);
	}

	public synchronized List<Card> draw(int numberOfCards) { 
		return deck.draw(numberOfCards);
	}

	public synchronized Player getNextPlayer(int order) { 
		// cycle through all the players starting with the player with the passed order until a player with at least one card is found
		// if no player with a card is found, the game is over, return null
		for (int i = 0; i < players.size(); i++) {
			int index = (i + order) % players.size();
			Player player = players.get(index);
			if (!player.getPile().isEmpty()) {
				return player;
			}
		}
		isOver = true;
		return null;
	}

	public synchronized Card getTopCardFromDiscardPile() { 
		return discardPile.peek();
	}

	public synchronized void removeTopCardFromDiscardPile() { 
		discardPile.pop();
	}
	
	public synchronized boolean isGameOver() {
		return isOver;
	}
	
	public synchronized String generateGameJson(Player player) {
		Map<String, Object> map = new HashMap<String, Object>();
		int deckSize = 0;
		if (deck != null) {
			deckSize = deck.getSize();
		}
		map.put("name", name);
		map.put("code", code);
		map.put("isOver", isOver);
		map.put("hasStarted", hasStarted);
		map.put("deckSize", deckSize);
		map.put("created", createdDate);
		map.put("players", players);
		map.put("discardPile", discardPile);
		map.put("pile", player.getPile());
		map.put("inTurn", inTurn);
		System.out.println("in-turn: " + inTurn);
		Player canStillToPlayer = players.get(inTurn);
		if (canStillToPlayer != null) {
			map.put("canStillToId", canStillToPlayer.getCanStillToID());
		} else {
			map.put("canStillToId", -1);
		}
		
		Gson gson = new  GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		return gson.toJson(map);
	}

	public synchronized void sendToAll(ResponseCode code, boolean success, String...tokens) throws IOException {
		String json = null;
		for (int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			if (success) {
				json = generateGameJson(player);
			}
			String response = ResponseFactory.generateRequestString(code, json, tokens);
			BufferedWriter writer = playerBufferedWriters.get(i);
			writer.write(response);
			writer.flush();
		}
	}

	public void closeWriter(int playerId) {
		try {
			playerBufferedWriters.get(playerId).close();
		} catch (IOException e) {
			// log
		}
	}

	public void send(BufferedWriter bufferedWriter, String response) throws IOException { 
		bufferedWriter.write(response);
		bufferedWriter.flush();
	}

}
