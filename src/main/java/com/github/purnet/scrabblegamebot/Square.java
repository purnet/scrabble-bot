package com.github.purnet.scrabblegamebot;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


public class Square {
	public int row;
	public int col;
	public int letterScore;
	public int wordScore;
	public String letter;
	public boolean anchor;
	public boolean start;
	Set<String> crossChecks;
	
	public Square(int row, int col, String l, int ls, int ws, boolean start){
		this.crossChecks = new HashSet<String>();
		this.row = row;
		this.col = col;
		this.letterScore = ls;
		this.wordScore = ws;
		this.start = start;
		if (Pattern.matches("[a-zA-Z]", l.trim())){
			this.letter = l.trim().toUpperCase();
		} else {
			this.letter = "";
		}
		
	}
}
