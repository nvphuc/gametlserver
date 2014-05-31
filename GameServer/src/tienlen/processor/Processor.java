package tienlen.processor;

import javax.swing.ImageIcon;

import tienlen.connector.Connector;
import tienlen.database.DataBaseAccessor;
import tienlen.player.Player;
import tienlen.server.Server;


public abstract class Processor extends Thread {
	protected Player player;
	protected DataBaseAccessor databaseAccessor;
	
	public Processor(Player player) {
		this.player = player;
		this.databaseAccessor = new DataBaseAccessor();
		this.start();
	}
	
	protected void disconnect() {
		databaseAccessor.logout(player.getIdPlayer());
		player.getConnector().disconnect();
		getServer().removePlayer(player);
	}
	
	protected Connector getConnector() {
		return player.getConnector();
	}
	
	protected Server getServer() {
		return player.getServer();
	}
	
	abstract public void run();

	abstract protected void handleMessage(String message);	
	
}
