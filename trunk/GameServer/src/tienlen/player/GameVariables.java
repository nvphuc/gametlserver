package tienlen.player;

import java.util.Vector;

import tienlen.table.Table;

public class GameVariables {
	public int orderNumber;
	public boolean isReady;
	public Table table;
	
	public boolean isFinishGame;		
	public Vector<String> cards;
	
	public GameVariables(Table table, int orderNumber) {
		this.table = table;
		this.orderNumber = orderNumber;
	}
	
	public void resetVariables() {
		isReady = false;
		isFinishGame = false;
		cards = new Vector<String>();
	}
}
