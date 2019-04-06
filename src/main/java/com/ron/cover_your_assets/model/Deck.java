package com.ron.cover_your_assets.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.google.gson.annotations.Expose;

public class Deck {
	
	private List<Card> cardList;
	@Expose
	private Stack<Integer> cardIds;
	
	public Deck(List<Card> cardList) {
		cardIds = new Stack<Integer>();
		this.cardList = cardList;
	}
	
	public List<Card> draw(int numberOfCards) {
		if (cardIds.size() < numberOfCards) {
			numberOfCards = cardIds.size(); 
		}
		return drawCards(numberOfCards);
	}
	
	private List<Card> drawCards(int numberOfCards) {
		List<Card> drawn = new ArrayList<Card>();
		for (int i = 0; i < numberOfCards; i++) {
			drawn.add(cardList.get(cardIds.pop()));
		}
		return drawn;
	}
	
	public boolean isEmpty() {
		return cardIds.isEmpty();
	}
	
	public void shuffle() {
		// card Ids are unique consecutive integers from zero
		List<Integer> ids = createListOfShuffledIds(cardList.size());
		insertIntoStack(ids);
	}

	private List<Integer> createListOfShuffledIds(int size) {
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < cardList.size(); i++) {
			ids.add(i);
		}
		Collections.shuffle(ids);
		return ids;
	}

	private void insertIntoStack(List<Integer> ids) {
		for (int id : ids) {
			cardIds.push(id);
		}
	}

	public int getSize() {
		return cardIds.size();
	}
}
