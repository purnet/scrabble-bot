package com.github.purnet.scrabblegamebot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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
			if (Pattern.matches("\\w", tile.trim())) {
				this.tiles.add(tile.trim().toUpperCase());
			} else {
				this.tiles.add("");
			}
		}
	}
	public ArrayList<ArrayList<Square>> getGameBoard() {
		return gameBoard;
	}
	
	
	private boolean isAnchor(Square s){
		// look above
		if (s.row > 0){
			String above = gameState.get(s.row -1).get(s.col);
			if (Pattern.matches("[a-zA-Z]", above.trim())){
				return true;
			}
		}
		// look below
		if (s.row < rowLimit){
			String below = gameState.get(s.row +1).get(s.col);
			if (Pattern.matches("[a-zA-Z]", below.trim())){
				return true;
			}
		}
		// look left
		if (s.col > 0){
			String left = gameState.get(s.row).get(s.col -1);
			if (Pattern.matches("[a-zA-Z]", left.trim())){
				return true;
			}
		}
		// look right
		if (s.col < colLimit){
			String right = gameState.get(s.row).get(s.col +1);
			if (Pattern.matches("[a-zA-Z]", right.trim())){
				return true;
			}
		}
		return false;
	}
	
	public Set<String> getCrossChecks(Square s) {
		Set<String> crossChecks = new HashSet<String>();
		StringBuilder topPart = new StringBuilder();
		for (int i = s.row -1; i >= 0; i--){
			String letter = gameState.get(i).get(s.col).trim();
			if (Pattern.matches("[a-zA-Z]", letter)){
				topPart.append(letter);
			} else {
				break;
			}
		}
		System.out.println("TopPart: "+ topPart + "$");
		int nodeStart = 0;
		// find starting node from lexicon traversal of top part
		for (char l : topPart.toString().toCharArray()){
			for (int e : nodes.get(nodeStart).edges) {
				if (l == nodes.get(e).letter.charAt(0)){
					nodeStart = e;
					break;
				}
			}
		}
		System.out.println("nodeStart "+String.valueOf(nodeStart));
		// for all edges of node from top part attempt to go down
		for (int e : nodes.get(nodeStart).edges) {
			LexiconNode nextNode = nodes.get(e);
			System.out.println("-----------------");
			System.out.println("edge e:"+ String.valueOf(e)+ " letter: " + nextNode.letter );
			System.out.println("------------------------------------------------------");
			boolean checkTerminal = true;
			for (int i = s.row +1; i <= rowLimit; i++){
				String letter = gameState.get(i).get(s.col).trim();
				System.out.println("row: "+ String.valueOf(i) + " letter:" + letter + "$");
				if (Pattern.matches("[a-zA-Z]", letter)){
					boolean found = false;
					for (int j : nextNode.edges){
						//System.out.println("edge j:" + String.valueOf(j) + " letter:" + nodes.get(j).letter);
						if (letter.equals(nodes.get(j).letter)){
							nextNode = nodes.get(j);
							System.out.println("FOUND " + nextNode.letter + " terminal:" + (nextNode.terminal ? "true" : "false"));
							found = true;
							break;
						}
					}
					if (!found){
						checkTerminal = false;
						break;
					}
				} else {
					break;
				}
			}
			if (checkTerminal && nextNode.terminal){
				System.out.println("Adding letter " + nodes.get(e).letter);
				crossChecks.add(nodes.get(e).letter);
			}
		}
		return crossChecks;
	}
	
	// Populate board with anchors and cross checks
	public void setGameBoard() {
		for (int i=0; i < gameState.size(); i++) {
			ArrayList<String> row = gameState.get(i);
			ArrayList<Square> newRow = new ArrayList<Square>();
			for (int j=0; j < row.size(); j++){		
				Square s = new Square(i,j, gameState.get(i).get(j));
				s.anchor = ((s.letter != "") ? false : isAnchor(s));
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
	
	public void scoreWord(String word, Square start){
		StringBuilder positions = new StringBuilder();
		int pos = start.col;
		for (char l : word.toCharArray()){
			positions.append(String.valueOf(pos) + ",");
			pos++;
		}
		positions.deleteCharAt(positions.lastIndexOf(","));
		System.out.println(word + " positions {"+ positions+"}");
	}
	
	public void extendLeft(String partialWord, LexiconNode node, int limit, Square origin, Square anchor){
		System.out.println("------------------------------------------------------");
		System.out.println("GOING RIGHT(" +partialWord + ", "+node.letter+", "+String.valueOf(origin.col));
		System.out.println("------------------------------------------------------");
		extendRight(partialWord, node, anchor, origin, anchor);
		if (limit > 0 && (origin.col -1) >= 0){
			Square s = gameBoard.get(origin.row).get(origin.col -1);
			for (int e : node.edges){
				LexiconNode nextNode = nodes.get(e);
				// TODO add crosschecks for column letters
				if (tiles.contains(nextNode.letter)){
					tiles.remove(nextNode.letter);
					extendLeft(partialWord + nextNode.letter, nextNode, limit -1, s, anchor);
					tiles.add(nextNode.letter);
				}
			}
		}
	}
	
	public void extendRight(String partialWord, LexiconNode node, Square square, Square origin, Square anchor){
		if (square.letter == "") {
			for (int e : node.edges){				
				LexiconNode nextNode = nodes.get(e);
				//TODO the below check needs to do cross checks also
				if (tiles.contains(nextNode.letter)){
					if (nextNode.terminal && (partialWord + nextNode.letter).length() > (anchor.col - origin.col + 1) ) {
						scoreWord(partialWord + nextNode.letter, origin);
					}
					tiles.remove(nextNode.letter);
					if ((square.col + 1) <= colLimit) {
						extendRight(partialWord + nextNode.letter, nextNode, gameBoard.get(square.row).get(square.col + 1), origin, anchor);
					}
					tiles.add(nextNode.letter);
				}	
			}
		} else {
			String letter = square.letter;
			for (int e : node.edges){
				LexiconNode nextNode = nodes.get(e);
				if (letter.equals(nextNode.letter)){
					if (nextNode.terminal && (partialWord + nextNode.letter).length() >= (anchor.col - origin.col)) {
						scoreWord(partialWord + nextNode.letter, origin);
					}
					if ((square.col + 1) <= colLimit){
						extendRight(partialWord + letter, nextNode, gameBoard.get(square.row).get(square.col + 1), origin, anchor);
					}
				}
			}
		}
	}
	
}
