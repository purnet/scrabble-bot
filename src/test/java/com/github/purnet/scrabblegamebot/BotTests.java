package com.github.purnet.scrabblegamebot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Stack;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class BotTests extends TestCase {

	public void testLexiconPopulate(){
		
		ScrabbleBotServlet sb = new ScrabbleBotServlet();
		try {
			sb.init();
		} catch (ServletException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		sb.getBot().populateLexicon();
		ArrayList<LexiconNode> nodes = sb.getBot().getNodes();
		
		// Print the tree -- comment this out for lager tests
//		for (int i = 0; i < nodes.size(); i++){
//			LexiconNode n = nodes.get(i);
//			System.out.println(Integer.toString(i) + " |" + n.letter + ": " + (n.terminal ? "T {" : " {") + n.getEdgesFlat() + "}");
//		}
		
		// check all words are traversable in the dictionary
		ArrayList<String> words = sb.getBot().getDictionary();
		int invalidWordCount = 0;
		for (String word : words){
			if (!sb.getBot().validWord(word)){
				invalidWordCount++;
			}
		}
		System.out.println("invalid words: " + Integer.toString(invalidWordCount));
		assertEquals(0, invalidWordCount);
	}
	
	public void testFirstMove() {
		// test to come up with words to  play at anchor position {10, 3}
		ScrabbleBotServlet sb = new ScrabbleBotServlet();
		try {
			sb.init();
		} catch (ServletException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		sb.getBot().populateLexicon();
		
		String state = "{" +
				" \"gamestate\": [" +
					"[\"\", \"\", \"\", \"\",\"\", \"\", \"\", \"\", \"\", \"\", \"\"]," +
					"[\"\", \"\", \"\", \"\",\"\", \"\", \"\", \"\", \"\", \" \", \"\"]," +
					"[\"\", \"\", \"\", \"\", \" C \", \"\", \"\", \"\", \"\",\"\", \"\"]," +
					"[\"\", \"\", \"\", \"N\", \"A\", \"W\", \"O\", \"B\", \"\", \"\", \"\"]," +
					"[\"\", \"\", \"\", \"\", \"L\", \"\", \"\", \"\", \"\", \"\", \"\"]," +
					"[\"\", \"\", \"\", \"\", \"C\", \"L\", \"O\", \"T\", \"H\", \"\", \"\"]," +
					"[\"\", \"\", \"\", \"\", \"U\",\"\", \"\", \"H\", \"E\", \"\", \"\"]," +
					"[\"\", \"\", \"\", \"\", \"L\",\"\", \"\", \"E\", \"A\", \"\", \"\"]," +
					"[\"\", \"\", \"\", \"\", \"A\",\"\", \"\", \"\", \"R\", \"O\", \"D\"]," +
					"[\"\", \"\", \"\", \"\", \"T\",\"\", \"\", \"\", \"T\", \"\", \"\"]," +
					"[\"\", \"\", \"\", \"\", \"E\",\"\", \"\", \"\", \"\", \"\", \"\"]" +
				"]," +
			" \"tiles\": [\"A\", \" T\", \" F \", \"R\", \"E\", \"S\", \"D\"] " +
	     "}";

		sb.getBot().setGameState(state);

		// Populate board with anchors and cross checks
		sb.getBot().setGameBoard();

		for (int i =0; i < sb.getBot().getGameBoard().size(); i++){
			for (int j=0;j <11; j++){
				Square s = sb.getBot().getGameBoard().get(i).get(j);
				System.out.print((s.letter != "") ? s.letter : " ");
				System.out.print((s.anchor ? "*" : " ") + "," );
			}
			System.out.println(" ");
		}
		
		for (Square a : sb.getBot().getAnchors()) {
			System.out.println("Anchor tile: {" + String.valueOf(a.row) + "," + String.valueOf(a.col) + "} crosschecks: " + a.crossChecks);
		}

		Square s = sb.getBot().getGameBoard().get(10).get(8);
		int limit = 0;
		StringBuilder builder = new StringBuilder();
		for(int i = s.col -1; i >= 0; i--){
			Square prev = sb.getBot().getGameBoard().get(s.row).get(i);
			if (prev.anchor){
				break;
			}
			if (prev.letter != ""){
				builder.append(prev.letter);
			} else {
				limit++;
			}
		}
		System.out.println(sb.getBot().getNodes().get(0).edges);
		System.out.println(sb.getBot().getNodes().get(36072).letter);
		Set<String> crossChecks = sb.getBot().getCrossChecks(s);
		System.out.println(crossChecks);
		//sb.getBot().extendLeft(builder.toString(), sb.getBot().getNodes().get(0), limit, s, s);
		//sb.getBot().extendRight("", sb.getBot().getNodes().get(0), s, s);

	}
	
	
}
