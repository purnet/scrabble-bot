package com.github.purnet.scrabblegamebot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.purnet.webhelperlib.HTTPRequestHelper;
import com.github.purnet.webhelperlib.Response;


public class ScrabbleBot {
	private ArrayList<LexiconNode> nodes;
	private ArrayList<String> dictionary;
	private int colLimit;
	private int rowLimit;
	private ArrayList<String> tiles;
	private ArrayList<String> fullTileRack;
	private ArrayList<ArrayList<BoardSquareScore>> standardBoard;
	private Map<String,Integer> letterPoints;
	private Move bestMove;
	private boolean boardInverted;
	private boolean firstMove;
	private ArrayList<ArrayList<Square>> gameBoard;
	
	public ScrabbleBot(){
		dictionary = new ArrayList<String>();
		nodes = new ArrayList<LexiconNode>();
		tiles = new ArrayList<String>();
		fullTileRack = new ArrayList<String>();
		standardBoard = new ArrayList<ArrayList<BoardSquareScore>>();
		letterPoints = new HashMap<String,Integer>();
		LexiconNode root = new LexiconNode(' ');
		nodes.add(root);
		bestMove = null;
		firstMove = true;
		gameBoard = new ArrayList<ArrayList<Square>>();
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
		
	public ArrayList<ArrayList<BoardSquareScore>> getStandardBoard() {
		return standardBoard;
	}
	public Map<String, Integer> getLetterPoints() {
		return letterPoints;
	}
	
	public void setStandardBoard(String json) {
		String objectJSON = "{ \"standardBoard\" : " + json + " }";
		ObjectMapper mapper = new ObjectMapper();
		StandardBoard board = new StandardBoard();
		//ArrayList<ArrayList<BoardSquare>>
		try {
			board = mapper.readValue(objectJSON, StandardBoard.class);
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
	
	private boolean isAnchor(Square s){
		// look above
		if (s.row > 0){
			String above = gameBoard.get(s.row -1).get(s.col).letter;
			if (Pattern.matches("[a-zA-Z]", above.trim())){
				return true;
			}
		}
		// look below
		if (s.row < rowLimit){
			String below = gameBoard.get(s.row +1).get(s.col).letter;
			if (Pattern.matches("[a-zA-Z]", below.trim())){
				return true;
			}
		}
		// look left
		if (s.col > 0){
			String left = gameBoard.get(s.row).get(s.col -1).letter;
			if (Pattern.matches("[a-zA-Z]", left.trim())){
				return true;
			}
		}
		// look right
		if (s.col < colLimit){
			String right = gameBoard.get(s.row).get(s.col +1).letter;
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
			String letter = gameBoard.get(i).get(s.col).letter;
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
				String letter = gameBoard.get(i).get(s.col).letter;
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
	public void setGameBoard(String state) {
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
		this.tiles.clear();
		this.fullTileRack.clear();
		this.colLimit = gs.getGamestate().get(0).size() -1;
		this.rowLimit = gs.getGamestate().size() -1;
		for (String tile : gs.getTiles()) {
			if (Pattern.matches("\\w", tile.trim())) {
				this.tiles.add(tile.trim().toUpperCase());
			} else {
				this.tiles.add("*");
			}
		}
		this.fullTileRack.addAll(tiles);
		this.gameBoard.clear();
		for (int i=0; i < gs.getGamestate().size(); i++) {
			ArrayList<String> row = gs.getGamestate().get(i);
			ArrayList<Square> newRow = new ArrayList<Square>();
			for (int j=0; j < row.size(); j++){		
				BoardSquareScore info = standardBoard.get(i).get(j);
				Square s = new Square(i,j, gs.getGamestate().get(i).get(j), info.getLS(), info.getWS(), info.isStart());
				newRow.add(s);
				if (s.letter != ""){
					firstMove = false;
				}
			}
			this.gameBoard.add(newRow);
		}
		for (int i=0; i < gameBoard.size(); i++) {
			for (int j=0; j < gameBoard.get(i).size(); j++) {
				Square s = gameBoard.get(i).get(j);
				s.anchor = ((s.letter != "") ? false : isAnchor(s));
				if  (s.anchor){
					gameBoard.get(i).get(j).crossChecks = getCrossChecks(s);
				}
			}
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
	
	public void populateDictionary(String body){
		BufferedReader bufReader = new BufferedReader(new StringReader(body));
		String line=null;
		ArrayList<String> temp = getDictionary();
		try {
			while( (line=bufReader.readLine()) != null )
			{
				temp.add(line);
			}
			setDictionary(temp);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		Square s = start;
		ArrayList<Integer> multipliers = new ArrayList<Integer>();
		ArrayList<TilePlacement> tilePlacement = new ArrayList<TilePlacement>();
		int acrossTotal = 0;
		int grandTotal = 0;
		for (char l : word.toCharArray()) { 
			if (s.letter == "") {
				String tile = (fullTileRack.contains(String.valueOf(l)) ? String.valueOf(l) : "*");
				TilePlacement tp = new TilePlacement((boardInverted ? s.col : s.row),(boardInverted ? s.row : s.col), tile);
				tilePlacement.add(tp);
				acrossTotal += (letterPoints.get(String.valueOf(l)) * s.letterScore);
				multipliers.add(s.wordScore);
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
				if (downTotal > 0){
					downTotal += (letterPoints.get(String.valueOf(l)) * s.letterScore);
					downTotal += downTotal * (s.wordScore > 1 ? s.wordScore : 0);
					grandTotal += downTotal;
				}
				
			} else {
				acrossTotal += letterPoints.get(String.valueOf(l));
			}
			s = gameBoard.get(s.row).get(Math.min(s.col + 1,colLimit));
		}

	    for (int m : multipliers) {
	    	if (m > 1){
	    		acrossTotal = acrossTotal * m;
	    	}
	    }
	    grandTotal += acrossTotal;	
		if (bestMove == null){
			bestMove = new Move("PLAY", tilePlacement, word, grandTotal);
		} else if (bestMove.getScore() < grandTotal || (bestMove.getScore() == grandTotal && word.length() < bestMove.getWord().length())){
			bestMove.setPlaytiles(tilePlacement);
			bestMove.setScore(grandTotal);
			bestMove.setWord(word);
		}
	}
	
	public void extendLeft(String partialWord, LexiconNode node, int limit, Square origin, Square anchor){
		extendRight(partialWord, node, anchor, origin, anchor);
		if (limit > 0 && (origin.col -1) >= 0){
			Square s = gameBoard.get(origin.row).get(origin.col -1);
			for (int e : node.edges){
				LexiconNode nextNode = nodes.get(e);
				if (tiles.contains(nextNode.letter)){
					tiles.remove(nextNode.letter);
					extendLeft(partialWord + nextNode.letter, nextNode, limit -1, s, anchor);
					tiles.add(nextNode.letter);
				} else if (tiles.contains("*")){
					tiles.remove("*");
					extendLeft(partialWord + nextNode.letter, nextNode, limit -1, s, anchor);
					tiles.add("*");
				}
			}
		}
	}
	
	public void extendRight(String partialWord, LexiconNode node, Square square, Square origin, Square anchor){
		if (square.letter == "") {
			for (int e : node.edges){				
				LexiconNode nextNode = nodes.get(e);
				if ((tiles.contains(nextNode.letter) || tiles.contains("*")) && 
						(!square.anchor || square.crossChecks.contains(nextNode.letter) || square.crossChecks.contains("*")) ){
					if (nextNode.terminal && 
							gameBoard.get(square.row).get(Math.min(colLimit, square.col + 1)).letter == ""){
						scoreWord(partialWord + nextNode.letter, origin);
					}
					String removeTile = (tiles.contains(nextNode.letter) ? nextNode.letter : "*");
					tiles.remove(removeTile);
					if ((square.col + 1) <= colLimit) {
						extendRight(partialWord + nextNode.letter, nextNode, gameBoard.get(square.row).get(square.col + 1), origin, anchor);
					}
					tiles.add(removeTile);
				}	
			}
		} else {
			String letter = square.letter;
			for (int e : node.edges){
				LexiconNode nextNode = nodes.get(e);
				if (letter.equals(nextNode.letter)){
					if (nextNode.terminal && 
							(partialWord + nextNode.letter).length() >= (anchor.col - origin.col) &&
							(square.col == colLimit || gameBoard.get(square.row).get(square.col + 1).letter == "")) {
						scoreWord(partialWord + nextNode.letter, origin);
					}
					if ((square.col + 1) <= colLimit){
						extendRight(partialWord + letter, nextNode, gameBoard.get(square.row).get(square.col + 1), origin, anchor);
					}
				}
			}
		}
	}
	
	public void traverseBoard(){
		for (int i=0; i < gameBoard.size(); i++) {
			for (int j=0; j < gameBoard.get(i).size(); j++) {
				Square s = gameBoard.get(i).get(j);
				Square origin = s;
				if (s.anchor){
					int limit = 0;
					StringBuilder builder = new StringBuilder();
					for(int k = s.col -1; k >= 0; k--){
						Square prev = gameBoard.get(s.row).get(k);
						if (prev.anchor){
							break;
						}
						if (prev.letter != ""){
							builder.append(prev.letter);
							origin = prev;
						} else {
							limit++;
						}
					}

					LexiconNode startNode = nodes.get(0);
					if (builder.toString().length() > 0) {
						for (char l : builder.reverse().toString().toCharArray()){
							for (int e : startNode.edges){
								if (nodes.get(e).letter.equals(String.valueOf(l))){
									startNode = nodes.get(e);
								}
							}
						}
					}
					extendLeft(builder.toString(), startNode, limit, origin, s);

					
				}
			}
		}
	}
	
	public Move makeBestMove(String gameState) {
		setGameBoard(gameState);
		bestMove = null;
		if (firstMove){
			for (int i=0; i <= colLimit; i++) {
				for (int j=0; j <= rowLimit; j++) {
					Square s = gameBoard.get(i).get(j);
					if (s.start){
						extendRight("",nodes.get(0),s,s,s);
						break;
					}
				}
			}
		} else {
			boardInverted = false;
			traverseBoard();

			// invert board and recalculate cross checks
			ArrayList<ArrayList<Square>> invertedBoard = new ArrayList<ArrayList<Square>>();	
			for (int i=0; i <= colLimit; i++) {
				ArrayList<Square> row = new ArrayList<Square>();
				for (int j=0; j <= rowLimit; j++){
					Square s = gameBoard.get(j).get(i);
					Square ns = new Square(i, j, s.letter, s.letterScore, s.wordScore, s.start);
					row.add(ns);
				}
				invertedBoard.add(row);
			}
			gameBoard.clear();
			gameBoard.addAll(invertedBoard);
	
			for (int i=0; i < gameBoard.size(); i++) {
				for (int j=0; j < gameBoard.get(i).size(); j++) {
					Square s = gameBoard.get(i).get(j);
					s.anchor = ((s.letter != "") ? false : isAnchor(s));
					if (s.anchor){
						gameBoard.get(i).get(j).crossChecks = getCrossChecks(s);
					}
				}
			}
			boardInverted = true;
			traverseBoard();
		}

		if (bestMove != null){
			System.out.println(bestMove.getWord() + " scores: " + bestMove.getScore());
			for (TilePlacement t : bestMove.getPlaytiles()) {
				System.out.println(" placement: "+ t.getTile() +" {" + t.getRow() + ","+ t.getCol()+"}");
			}
			return bestMove;
		} else {
			//TODO : swap tiles or pass decide here please
			bestMove.setType("PASS");
			return bestMove;
		}
	}
}
