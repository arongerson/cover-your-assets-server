package com.ron.cover_your_assets.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gson.annotations.Expose;

public class AssetUnit {
	
	@Expose
	private boolean stolable;
	@Expose
	private List<Card> cards = new ArrayList<Card>();
	
	public AssetUnit(boolean stolable, Card card1, Card card2) {
		this.stolable = stolable;
		addCard(card1);
		addCard(card2);
	}
	
	public boolean isStolable() {
		return stolable;
	}
	
	public final void addCard(Card card) {
		this.cards.add(card);
	}
	
	public Card getTopCard() {
		return cards.get(cards.size() - 1);
	}
	
	public void sortAssetUnitByValue() {
		cards.sort(new Comparator<Card>() {
			public int compare(Card card1, Card card2) {
				return card2.getValue() - card1.getValue();
			}
		});
	}
}
