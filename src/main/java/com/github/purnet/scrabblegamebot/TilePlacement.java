package com.github.purnet.scrabblegamebot;

public class TilePlacement {
	private int row;
	private int col;
	private String tile;
	
	public TilePlacement(int row, int col, String tile) {
		super();
		this.row = row;
		this.col = col;
		this.tile = tile;
	}
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public String getTile() {
		return tile;
	}
	public void setTile(String tile) {
		this.tile = tile;
	}
	
}
