package tienlen.player;

import java.net.Socket;

import javax.swing.ImageIcon;

import tienlen.processor.PlayerProcessor;
import tienlen.processor.Processor;
import tienlen.connector.Connector;
import tienlen.newtype.PlayerStatus;
import tienlen.server.Server;

public class Player {

	private Server server;
	private Connector connector;
	private int idPlayer;
	private String userName;
	private ImageIcon avatar;
	private int credit;	
	private GameVariables game;
	private PlayerStatus status;
	
	public Player(Server server, Socket socket) {
		this.server = server;
		connector = new Connector(socket);
		userName = "";
		avatar = null;
		credit = 0;
		idPlayer = -1;
		game = null;
		status = PlayerStatus.CONNECT;
		new PlayerProcessor(this);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public ImageIcon getAvatar() {
		return avatar;
	}

	public void setAvatar(ImageIcon avatar) {
		this.avatar = avatar;
	}

	public int getCredit() {
		return credit;
	}

	public void setCredit(int Credit) {
		this.credit = Credit;
	}

	public void addCredit(int amount) {
		credit += amount;
		if(credit < 0) {
			credit = 0;
		}
	}

	public int getIdPlayer() {
		return idPlayer;
	}

	public void setIdPlayer(int idPlayer) {
		this.idPlayer = idPlayer;
	}

	public PlayerStatus getStatus() {
		return status;
	}

	public void setStatus(PlayerStatus status) {
		this.status = status;
	}

	public Server getServer() {
		return server;
	}

	public Connector getConnector() {
		return connector;
	}

	public GameVariables getGame() {
		return game;
	}

	public void setGame(GameVariables game) {
		this.game = game;
	}
}
