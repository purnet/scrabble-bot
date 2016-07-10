package com.github.purnet.scrabblegamebot;


public class Square {
	public int row;
	public int col;
	public String letter;
	public boolean anchor;
	
	public Square(int row, int col, String l){
		this.row = row;
		this.col = col;
		this.letter = l.replaceAll("\\s","").toUpperCase();
	}
}
