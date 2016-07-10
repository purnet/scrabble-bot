package com.github.purnet.scrabblegamebot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ScrabbleBot {
	private ArrayList<LexiconNode> nodes;
	private ArrayList<String> dictionary;
	private int colLimit;
	private int rowLimit;
	private ArrayList<String> tiles;
	
	// This game state should be as downloaded from Merknera - saved in persistence and will include player info
	ArrayList<ArrayList<String>> gameState;
	// the actual game board should be managed by the bot with information on how to play a move
	ArrayList<ArrayList<Square>> gameBoard = new ArrayList<ArrayList<Square>>();
	
	public ScrabbleBot(){
		dictionary = new ArrayList<String>();
		nodes = new ArrayList<LexiconNode>();
		tiles = new ArrayList<String>();
		LexiconNode root = new LexiconNode(' ');
		nodes.add(root);
	}
	public ArrayList<LexiconNode> getNodes() {
		return nodes;
	}

	public ArrayList<String> getDictionary() {
		return dictionary;
	}
	
	public void setDictionary(ArrayList<String> dictionary) {
		this.dictionary = dictionary;
	}
	
	
	public ArrayList<ArrayList<String>> getGameState() {
		return gameState;
	}
	public void setGameState(String state) {
		ObjectMapper mapper = new ObjectMapper();
		GameState gs = new GameState();
		try {
			gs = mapper.readValue(state, GameState.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.gameState = gs.getGamestate();
		this.colLimit = gameState.get(0).size() -1;
		this.rowLimit = gameState.size() -1;
		for (String tile : gs.getTiles()) {
			this.tiles.add(tile.replaceAll("\\s","").toUpperCase());
		}
	}
	public ArrayList<ArrayList<Square>> getGameBoard() {
		return gameBoard;
	}
	
	
	private boolean isAnchor(Square s){
		// look above
		if (s.row > 0){
			String above = gameState.get(s.row -1).get(s.col);
			if (above != null && above != ""){
				return true;
			}
		}
		// look below
		if (s.row < rowLimit){
			String below = gameState.get(s.row +1).get(s.col);
			if (below != null && below != ""){
				return true;
			}
		}
		// look left
		if (s.col > 0){
			String left = gameState.get(s.row).get(s.col -1);
			if (left != null && left != ""){
				return true;
			}
		}
		// look right
		if (s.col < colLimit){
			String right = gameState.get(s.row).get(s.col +1);
			if (right != null && right != ""){
				return true;
			}
		}
		return false;
	}
	
	// Populate board with anchors and cross checks
	public void setGameBoard() {
		for (int i=0; i < gameState.size(); i++) {
			ArrayList<String> row = gameState.get(i);
			ArrayList<Square> newRow = new ArrayList<Square>();
			for (int j=0; j < row.size(); j++){		
				Square s = new Square(i,j, gameState.get(i).get(j));
				s.anchor = isAnchor(s);
				newRow.add(s);	
			}
			this.gameBoard.add(newRow);
		}
	}
	
	public boolean validWord(String s){
		int currentNode = 0;
		for (char l : s.toCharArray()){
			char letter = Character.toUpperCase(l);
			boolean edgeFound = false;			
			for (int e : nodes.get(currentNode).edges) {
				if (letter == nodes.get(e).letter.charAt(0)){
					currentNode = e;
					edgeFound = true;
					break;
				}
			}
			if (!edgeFound){
				return false;
			}
		}
		if (nodes.get(currentNode).terminal) {
			return true;
		} else {
			return false;
		}
	}
	
	public void populateLexicon(){
		for (String word : dictionary) {
			int currentNode = 0;
			for (char l : word.toCharArray()) {
				char letter = Character.toUpperCase(l);
				boolean edgeFound = false;
				for (int e : nodes.get(currentNode).edges) {
					if (letter == nodes.get(e).letter.charAt(0)) {
						currentNode = e;
						edgeFound = true;
						break;
					}
				}
				if (!edgeFound) {
					LexiconNode ln = new LexiconNode(letter);
					nodes.add(ln);
					int newNode = nodes.size() -1;
					nodes.get(currentNode).edges.add(newNode);
					currentNode = newNode;
				}
			}
			nodes.get(currentNode).terminal = true;
		}
	}
	
	public void scoreWord(String word){
		System.out.println(word);
	}
	
	public void extendRight(String partialWord, LexiconNode node, Square square){
		if (square.letter == "") {
			for (int e : node.edges){				
				LexiconNode nextNode = nodes.get(e);
				//TODO the below check needs to do cross checks also
				if (tiles.contains(nextNode.letter)){
					if (nextNode.terminal) {
						scoreWord(partialWord + nextNode.letter);
					}
					tiles.remove(nextNode.letter);
					if ((square.col + 1) <= colLimit) {
						extendRight(partialWord + nextNode.letter, nextNode, gameBoard.get(square.row).get(square.col + 1));
					}
					tiles.add(nextNode.letter);
				}	
			}
		} else {
			String letter = square.letter;
			for (int e : node.edges){
				LexiconNode nextNode = nodes.get(e);
				if (letter.equals(nextNode.letter)){
					if (nextNode.terminal) {
						scoreWord(partialWord + nextNode.letter);
					}
					if ((square.col + 1) <= colLimit){
						extendRight(partialWord + letter, nextNode, gameBoard.get(square.row).get(square.col + 1));
					}
				}
			}
		}
	}
	
	
	
	public void extendRightaa(String partialWord, LexiconNode node, Square square){
		//System.out.println("extendRight(" + partialWord + (node.terminal ? ",T": ", ")+node.letter +"," + square.letter + ")");
		if (node.terminal) {
			System.out.println(partialWord);
		}
		if (square.letter == "") {
			for (int e : node.edges){
				//TODO the below check needs to do cross checks also
				String letter = nodes.get(e).letter;
				if (tiles.contains(letter) && (square.col + 1) <= colLimit){
					tiles.remove(letter);
					extendRight(partialWord + letter, nodes.get(e),gameBoard.get(square.row).get(square.col + 1));
					tiles.add(letter);
				}	
			}
		} else {
			String letter = square.letter;
			for (int e : node.edges){	
				if (letter.equals(nodes.get(e).letter) && (square.col + 1) <= colLimit){
					extendRight(partialWord + letter, nodes.get(e), gameBoard.get(square.row).get(square.col + 1));
				}
			}
		}
	}
	
}
