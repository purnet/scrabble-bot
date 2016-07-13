package com.github.purnet.scrabblegamebot;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"word", "score"})
public class Move {
	private String type;
	private ArrayList<String> exchangetiles;
	private ArrayList<TilePlacement> playtiles;
	private String word;
	private int score;
	public Move(String type, ArrayList<TilePlacement> playtiles, String word,
			int score) {
		super();
		this.type = type;
		this.playtiles = playtiles;
		this.word = word;
		this.score = score;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public ArrayList<String> getExchangetiles() {
		return exchangetiles;
	}
	public void setExchangetiles(ArrayList<String> exchangetiles) {
		this.exchangetiles = exchangetiles;
	}
	public ArrayList<TilePlacement> getPlaytiles() {
		return playtiles;
	}
	public void setPlaytiles(ArrayList<TilePlacement> playtiles) {
		this.playtiles = playtiles;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
    
	
}
