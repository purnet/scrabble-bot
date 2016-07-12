package com.github.purnet.scrabblegamebot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	private ArrayList<Square> anchors;
	private ArrayList<ArrayList<BoardSquareScore>> standardBoard;
	private Map<String,Integer> letterPoints;
	
	// This game state should be as downloaded from Merknera - saved in persistence and will include player info
	ArrayList<ArrayList<String>> gameState;
	// the actual game board should be managed by the bot with information on how to play a move
	ArrayList<ArrayList<Square>> gameBoard = new ArrayList<ArrayList<Square>>();
	
	public ScrabbleBot(){
		dictionary = new ArrayList<String>();
		nodes = new ArrayList<LexiconNode>();
		tiles = new ArrayList<String>();
		anchors = new ArrayList<Square>();
		standardBoard = new ArrayList<ArrayList<BoardSquareScore>>();
		letterPoints = new HashMap<String,Integer>();
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
	
	public ArrayList<ArrayList<BoardSquareScore>> getStandardBoard() {
		return standardBoard;
	}
	public Map<String, Integer> getLetterPoints() {
		return letterPoints;
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
	public void setStandardBoard(String json) {
		ObjectMapper mapper = new ObjectMapper();
		StandardBoard board = new StandardBoard();
		//ArrayList<ArrayList<BoardSquare>>
		try {
			board = mapper.readValue(json, StandardBoard.class);
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
		this.standardBoard = board.getStandardBoard();
	}
	public void setLetterPoints(String json) {
		ObjectMapper mapper = new ObjectMapper();
		LetterInfo letters = new LetterInfo();
		try {
			letters = mapper.readValue(json, LetterInfo.class);
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
		this.letterPoints = letters.getPoints();
	}
	public ArrayList<ArrayList<Square>> getGameBoard() {
		return gameBoard;
	}
	
	public ArrayList<Square> getAnchors() {
		return anchors;
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
		if (gameBoard.get(Math.max(s.row -1, 0)).get(s.col).letter == "" && 
				gameBoard.get(Math.min(s.row +1, rowLimit)).get(s.col).letter == ""){
			crossChecks.add("*");
			return crossChecks;
		}
		// build up the upper part of the cross check word above the anchor
		StringBuilder topPart = new StringBuilder();
		for (int i = s.row -1; i >= 0; i--){
			String letter = gameState.get(i).get(s.col).trim();
			if (Pattern.matches("[a-zA-Z]", letter)){
				topPart.append(letter);
			} else {
				break;
			}
		}
		topPart.reverse();
		
		// find starting node from lexicon traversal of top part
		int nodeStart = 0;
		for (char l : topPart.toString().toCharArray()){
			for (int e : nodes.get(nodeStart).edges) {
				if (l == nodes.get(e).letter.charAt(0)){
					nodeStart = e;
					break;
				}
			}
		}

		// for all edges of node from top part attempt to go down
		for (int e : nodes.get(nodeStart).edges) {
			LexiconNode nextNode = nodes.get(e);
			boolean checkTerminal = true;
			for (int i = s.row +1; i <= rowLimit; i++){
				String letter = gameState.get(i).get(s.col).trim();
				if (Pattern.matches("[a-zA-Z]", letter)){
					boolean found = false;
					for (int j : nextNode.edges){
						if (letter.equals(nodes.get(j).letter)){
							nextNode = nodes.get(j);
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
				BoardSquareScore info = standardBoard.get(i).get(j);
				Square s = new Square(i,j, gameState.get(i).get(j), info.getLS(), info.getWS(), info.isStart());
				s.anchor = ((s.letter != "") ? false : isAnchor(s));
				newRow.add(s);
				if (s.anchor){
					anchors.add(s);
				}
			}
			this.gameBoard.add(newRow);
		}
		for (Square anchor : anchors) {
			gameBoard.get(anchor.row).get(anchor.col).crossChecks = getCrossChecks(anchor);
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
		StringBuilder positions = new StringBuilder();// TODO delete this
		int pos = start.col;// TODO delete this

		Square s = start;
		ArrayList<Integer> multipliers = new ArrayList<Integer>();
		int acrossTotal = 0;
		int grandTotal = 0;
		for (char l : word.toCharArray()) { 
			positions.append(String.valueOf(pos) + ","); // TODO delete this
			pos++; // TODO delete this
			if (s.letter != "") {
				multipliers.add(s.wordScore);
				acrossTotal += (letterPoints.get(String.valueOf(l)) * s.letterScore);
				int downTotal = 0;
				// look upwards
				for (int i = s.row -1; i >= 0; i--){
					Square squareUp = gameBoard.get(i).get(s.col);
					if (squareUp.letter != ""){
						downTotal += letterPoints.get(squareUp.letter);
					} else {
						break;
					}
				}
				// look downwards
				for (int i = s.row +1; i <= rowLimit; i++){
					Square squareDown = gameBoard.get(i).get(s.col);
					if (squareDown.letter != ""){
						downTotal += letterPoints.get(squareDown.letter);
					} else {
						break;
					}
				}
				downTotal += downTotal * s.wordScore;
				grandTotal += downTotal;
			}
			s = gameBoard.get(s.row).get(s.col + 1);
		}

	    for (int m : multipliers) {
	    	acrossTotal += acrossTotal * m;
	    }
	    grandTotal += acrossTotal;	
	    positions.deleteCharAt(positions.lastIndexOf(","));
		System.out.println(word + " row:" + String.valueOf(start.row) + " positions {"+ positions+"}" + " score:" +String.valueOf(grandTotal));
		
	}
	
	public void extendLeft(String partialWord, LexiconNode node, int limit, Square origin, Square anchor){
//		System.out.println("------------------------------------------------------");
//		System.out.println("GOING RIGHT(" +partialWord + ", "+node.letter+", "+String.valueOf(origin.col));
//		System.out.println("------------------------------------------------------");
		extendRight(partialWord, node, anchor, origin, anchor);
		if (limit > 0 && (origin.col -1) >= 0){
			Square s = gameBoard.get(origin.row).get(origin.col -1);
			for (int e : node.edges){
				LexiconNode nextNode = nodes.get(e);
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
				if (tiles.contains(nextNode.letter) && 
						(square.crossChecks.contains(nextNode.letter) || square.crossChecks.contains("*")) ){
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
	
	public void makeBestMove() {
		for (Square anchor : anchors){
			int limit = 0;
			StringBuilder builder = new StringBuilder();
			for(int i = anchor.col -1; i >= 0; i--){
				Square prev = gameBoard.get(anchor.row).get(i);
				if (prev.anchor){
					break;
				}
				if (prev.letter != ""){
					builder.append(prev.letter);
				} else {
					limit++;
				}
			}
			extendLeft(builder.toString(), nodes.get(0), limit, anchor, anchor);
		}
	}
}
