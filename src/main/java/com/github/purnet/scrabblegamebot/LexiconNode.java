package com.github.purnet.scrabblegamebot;

import java.util.ArrayList;
import java.util.List;

public class LexiconNode {
	public char letter;
	public List<Integer> edges;
	public boolean terminal;
	
	LexiconNode(char letter){
		this.letter = letter;
		this.edges = new ArrayList<Integer>();
	}
}
