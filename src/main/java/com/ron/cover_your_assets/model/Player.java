package com.ron.cover_your_assets.model;

import java.util.List;
import java.util.Stack;

import com.google.gson.annotations.Expose;

public class Player {
	/**
	 * another field to be added soon for an id
	 * this should be more cryptic to avoid players impersonating other players
	 */
	@Expose
	private int order;
	
	/**
	 * has a default value of -1, meaning steal to all except self,
	 * after the first steal, the value is changed to the player whose asset was stolen
	 * when the player turn's starts, it should be restored to -1
	 */
	@Expose
	private int canStillToId;
	@Expose
	private boolean steal;
	@Expose
	private boolean yield;
	@Expose
	private boolean draw;
	@Expose
	private boolean cover;
	@Expose
	private boolean discard;
	@Expose
	private boolean initiator;
	@Expose
	private boolean inTurn;
	@Expose
	public String username;
	@Expose
	private Stack<AssetUnit> assets = new Stack<AssetUnit>();
	@Expose(serialize = false)
	private List<Card> pile;
	
	public Player(int order, String username, boolean initiator) {
		this.order = order;
		this.username = username;
		this.canStillToId = -1;
		this.initiator = initiator;
	}
	
	public int getOrder() {
		return order;
	}
	
	public boolean canYield() {
		return yield;
	}
	
	public boolean canDraw() {
		return draw;
	}
	
	public boolean canCover() {
		return cover;
	}
	
	public boolean canSteal() {
		return steal;
	}
	
	public boolean canDiscard() {
		return discard;
	}
	
	public boolean isInitiator() {
		return initiator;
	}
	
	public List<AssetUnit> getAssets() {
		return assets;
	}
	
	public void setAssets(Stack<AssetUnit> assets) {
		this.assets = assets;
	}
	
	public List<Card> getPile() {
		return pile;
	}
	
	public void setPile(List<Card> pile) {
		this.pile = pile;
	}
	
	public void setInTurn(boolean inTurn) {
		this.inTurn = inTurn;
	}
	
	public boolean isInTurn() {
		return inTurn;
	}
	
	public String getUsername() {
		return username;
	}
	
	public boolean yield(Game game, Player receiver) { 
		// if the player yielding is in turn, 
		// 1. update next actions
		// 2. draw cards
		// 3. set up next turn
		// if the player yielding is not in turn
		// 1. set his yield to false
		// 2. set the steal of the player in turn to true
		// 3. set the draw of the player in turn to true
		// sort the top asset-unit of the receiver
		if (inTurn) {
			receiver.resetPlayerTurn();
			receiver.assets.peek().sortAssetUnitByValue();
			drawAndAddCardsToPile(game);
			receiver.drawAndAddCardsToPile(game);
			setupNextTurn(game); 
		} else {
			yield = false;
			steal = false;
			draw = false;
			cover = false;
			discard = false;
			receiver.steal = true;
			receiver.draw = true;
			receiver.cover = false;
			receiver.yield = false;
			receiver.discard = false;
			receiver.assets.peek().sortAssetUnitByValue();
			game.setInTurn(receiver.order);
		}
		return true;
	}
	
	public boolean draw(Game game, Player counterPartPlayer) {
		// draw cards for the player in turn
		// draw cards for the counter part
		// reset both players
		// setup next turn
		drawAndAddCardsToPile(game);
		resetPlayerTurn();
		counterPartPlayer.drawAndAddCardsToPile(game);
		counterPartPlayer.resetPlayerTurn();
		setupNextTurn(game);
		return true;
	}
	
	public boolean steal(Game game, Player attacked, Card card) {
		// if the attacked player has no asset or his top AssetUnit is the bottom, or
		// if the top of the card of the asset does not match the card return false
		// otherwise perform the theft and update action variables
		// updating player actions
		// 1. set the thief's stealing to false
		// 2. set the attacked steal to true
		// 3. set the attacked yield to true
		// update the canStillToId of the players involved to each other's id
		if (!assetStollable(attacked, card)) {
			return false;
		}
		performStealing(attacked, card);
		updateAfterStealActions(game, attacked);
		updateCanStillToId(attacked);
		return true;
	}
	
	public boolean coverWithOwnCards(Game game, Card card1, Card card2) {
		// if the match is valid
		// remove the cards from the player's pile
		// create a new asset-unit
		// append the asset-unit, sort it, and append it to the player assets 
		// draw cards
		if (isValidMatch(card1, card2)) { 
			removeCardsFromPile(card1, card2);
			createAndAppendTheAssetUnit(card1, card2);
			drawAndAddCardsToPile(game);
			setupNextTurn(game);
			return true;
		}
		return false;
	}
	
	public boolean coverFromDiscardPile(Game game, Card card) {
		// verify match
		// remove the top card from the discard pile
		// remove the card from the player's pile
		// create a new asset-unit, sort it and append it to the player assets
		// draw cards
		// update player actions
		Card fromDiscardPile = game.getTopCardFromDiscardPile(); 
		// is valid method allows taking wild cards from discard pile, to be changed.
		if (isValidMatch(fromDiscardPile, card)) { 
			game.removeTopCardFromDiscardPile();
			pile.remove(card);
			createAndAppendTheAssetUnit(fromDiscardPile, card);
			drawAndAddCardsToPile(game);
			setupNextTurn(game);
			return true;
		}
		return false;
	}
	
	public boolean discard(Game game, Card card) {
		// remove the card from the player's pile
		// add the card to the discard pile
		// if the deck is not empty, draw a card and add it to the player's pile
		// update player actions
		pile.remove(card);
		game.addToDiscardPile(card);
		List<Card> drawnCard = game.draw(1);
		if (!drawnCard.isEmpty()) {
			pile.add(drawnCard.get(0));
		}
		setupNextTurn(game);
		return true;
	}

	private void setupNextTurn(Game game) {
		// end current player's turn
		// find the next player to play, might be None
		// set action variables appropriately
		resetPlayerTurn();
		Player nextPlayer = game.getNextPlayer(order + 1);
		if (nextPlayer != null) {
			nextPlayer.setPlayerTurn();
			game.setInTurn(nextPlayer.getOrder());
		}
	}
	
	private void setPlayerTurn() {
		inTurn = true;
		steal = true;
		yield = false;
		cover = true;
		discard = true;
		draw = false;
	}

	private void resetPlayerTurn() {
		inTurn = false;
		steal = false;
		yield = false;
		cover = false;
		discard = false;
		draw = false;
		canStillToId = -1;
	}

	private void createAndAppendTheAssetUnit(Card card1, Card card2) {
		boolean stolable = assets.isEmpty() ? false : true;
		AssetUnit newAssetUnit = new AssetUnit(stolable, card1, card2);
		newAssetUnit.sortAssetUnitByValue();
		assets.push(newAssetUnit);
	}

	private void removeCardsFromPile(Card card1, Card card2) {
		pile.remove(card1);
		pile.remove(card2);
	}

	private boolean isValidMatch(Card card1, Card card2) {
		// the two cards should either match or at least one should be a wild card
		return oneIsWildCard(card1, card2) || cardsMatch(card1, card2);
	}

	private boolean oneIsWildCard(Card card1, Card card2) {
		return card1.isWildCard() || card2.isWildCard();
	}

	private void updateCanStillToId(Player attacked) {
		canStillToId = attacked.order;
		attacked.canStillToId = order;
	}

	private void updateAfterStealActions(Game game, Player attacked) {
		// there are four main categories: luckily the third and fourth options have the same actions
		// 1. both players have no cards
		// 2. attacker has at least one card, the attacked has no card
		// 3. the attacked has at least one card, the attacker has no card
		// 4. both have cards
		if (pile.size() == 0 && attacked.pile.size() == 0) {
			setupForBothHaveNoCards(game, attacked);
		} else if (pile.size() > 0 && attacked.pile.size() == 0) {
			setupForAttackerHasMoreCards(game, attacked);
		} else {
			steal = false;
			cover = false;
			yield = false;
			draw = false;
			discard = false;
			attacked.steal = true;
			attacked.yield = true;
			attacked.cover = false;
			attacked.draw = false;
			attacked.discard = false;
			game.setInTurn(attacked.order);
		}
	}

	private void setupForAttackerHasMoreCards(Game game, Player attacked) {
		// if the attacker is the player in turn:
		// keep on attacking
		// else: got to draws and setup next turn
		if (inTurn) {
			steal = true;
			cover = false;
			yield = false;
			draw = true;
			discard = false;
			game.setInTurn(order); 
			attacked.steal = false;
			attacked.yield = false;
			attacked.cover = false;
			attacked.draw = false;
			attacked.discard = false;
		} else {
			attacked.drawAndAddCardsToPile(game);
			this.drawAndAddCardsToPile(game);
			setupNextTurn(game);
		}
	}

	private void setupForBothHaveNoCards(Game game, Player attacked) {
		// make the users draw the cards starting with the player in turn
		// setup the next turn
		if (inTurn) {
			this.drawAndAddCardsToPile(game);
			attacked.drawAndAddCardsToPile(game);
		} else {
			attacked.drawAndAddCardsToPile(game);
			this.drawAndAddCardsToPile(game);
		}
		setupNextTurn(game); 
	}

	private void performStealing(Player attacked, Card card) {
		// the asset-unit is popped from the attacked
		// the card is removed from the attacker's pile
		// the card is added to the asset-unit
		// the asset-unit is pushed to the attacker
		AssetUnit assetUnit = attacked.assets.pop();
		pile.remove(card);
		assetUnit.addCard(card);
		assets.push(assetUnit);
	}

	private boolean assetStollable(Player attacked, Card card) {
		if (attacked.assets.size() < 2) {
			return false;
		} else if (stealingFromTheWrongPlayer(attacked)) {
			return false;
		} else {
			return isValidCard(attacked, card);
		}
	}

	private boolean stealingFromTheWrongPlayer(Player attacked) {
		return canStillToId != -1 && canStillToId != attacked.order;
	}
	
	private boolean isValidCard(Player attacked, Card card) {
		// the card is valid if it matches the top card or it is a wild card not less than the top card
		return cardMatchesTopOfStack(attacked, card) || isValidWildCard(attacked, card);
	}

	private boolean isValidWildCard(Player attacked, Card card) {
		return card.isWildCard() && card.getValue() >= attacked.assets.peek().getTopCard().getValue();
	}

	private boolean cardMatchesTopOfStack(Player attacked, Card card) {
		return cardsMatch(attacked.assets.peek().getTopCard(), card);
	}
	
	private boolean cardsMatch(Card card1, Card card2) {
		return card1.getType() == card2.getType();
	}
	
	private void drawAndAddCardsToPile(Game game) {
		List<Card> cards = game.draw(game.getCardsPerPlayer() - pile.size());
		for (Card card : cards) {
			pile.add(card);
		}
	}

	public void setStart() {
		inTurn = true;
		cover = true;
		discard = true;
		yield = false;
		steal = true;
	}

	public boolean hasRightToStealTo(Player attacked) {
		return canStillToId == -1 || canStillToId == attacked.order;
	}

	public boolean ownsCard(Card card) {
		for (Card c : pile) {
			if (c.getId() == card.getId()) {
				return true;
			}
		}
		return false;
	}

	public boolean canYieldTo(Player receiver) {
		return canStillToId == receiver.order;
	}

	public int getCanStillToID() {
		return canStillToId;
	}
}
