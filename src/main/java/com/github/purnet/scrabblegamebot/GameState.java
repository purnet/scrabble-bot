package com.github.purnet.scrabblegamebot;

import java.util.ArrayList;

public class GameState {
	private ArrayList<ArrayList<String>> gamestate;
	private ArrayList<String> tiles;

	public ArrayList<ArrayList<String>> getGamestate() {
		return gamestate;
	}

	public void setGamestate(ArrayList<ArrayList<String>> gamestate) {
		this.gamestate = gamestate;
	}

	public ArrayList<String> getTiles() {
		return tiles;
	}

	public void setTiles(ArrayList<String> tiles) {
		this.tiles = tiles;
	}
	
}
