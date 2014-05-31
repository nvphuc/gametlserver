package tienlen.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import tienlen.player.Player;
import tienlen.table.Table;

public class Server {
	private Vector<Player> players;
	private Vector<Table> tables;

	public Server() {
		players = new Vector<Player>();
		tables = new Vector<Table>();
		ServerSocket server;
		try {
			server = new ServerSocket(9999);
			while (true) {
				Socket socket = server.accept();
				players.add(new Player(this, socket));
			}
		} catch (IOException e) {
		}
	}

	public Vector<Player> getPlayers() {
		return players;
	}

	public Player getPlayer(String playerName) {
		for (Player player : players) {
			if (playerName.equals(player.getUserName())) {
				return player;
			}
		}
		return null;
	}

	public Player getPlayer(int index) {
		if (index > -1 && index < tables.size())
			return players.get(index);
		return null;
	}

	public void removePlayer(Player player) {
		players.remove(player);
	}

	public Vector<Table> getTables() {
		return tables;
	}

	public Table getTable(int index) {
		if (index > -1 && index < tables.size())
			return tables.get(index);
		return null;
	}

	public void addTable(Table table) {
		tables.add(table);
	}

	public void removeTable(Table table) {
		tables.remove(table);
	}

	public static void main(String[] args) {
		System.out.println("Start");
		new Server();
	}
}
