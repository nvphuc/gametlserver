package tienlen.processor;

import javax.swing.ImageIcon;

import tienlen.newtype.PlayerStatus;
import tienlen.player.Player;
import tienlen.table.Table;

public class PlayerProcessor extends Processor {

	public PlayerProcessor(Player player) {
		super(player);
	}

	@Override
	public void run() {	
		while (player.getStatus() == PlayerStatus.CONNECT
				|| player.getStatus() == PlayerStatus.EDIT_ACCOUNT) {
			handleMessage(getConnector().receiveMessage());
		}
		if (player.getStatus() == PlayerStatus.DISCONNECT) {
			System.out.println("Thoat tai player");
			disconnect();
		}
	}

	@Override
	protected void handleMessage(String message) {
		System.out.println("PlayerProcessor : " + message);
		
		String[] args = message.split("@");

		switch (args[0]) {

		case "Register":
			processRegister(args[1]);
			break;
			
		case "Login":
			processLogin(args[1]);
			break;

		case "CreateTable":
			processCreateTable(args[1]);
			break;

		case "PlayRight":
			processPlayRight();
			break;

		case "JoinTable":
			processJoinTable(args[1]);
			break;
			
		case "UpdataTables":
			processUpdateTables();
			break;
			
		case "Edit":
			getConnector().sendMessage("RSEdit");
			player.setStatus(PlayerStatus.EDIT_ACCOUNT);
			break;
			
		case "BackWaitingRoom":
			getConnector().sendMessage("RSBack");
			player.setStatus(PlayerStatus.CONNECT);
			break;
			
		case "UpdateUserName":
			processUpdateAccount(0, args[1]);
			break;

		case "UpdatePass":
			processUpdateAccount(1, args[1]);
			break;
			
		case "UpdateAvatar":
			processUpdateAvatar();
			break;
			
		// Thoat
		default:
			player.setStatus(PlayerStatus.DISCONNECT);
			break;
		}

	}

	private void processUpdateAvatar() {
		boolean check = getConnector().receiveImage(player.getIdPlayer());
		if(check) {
			getConnector().sendMessage("OK");
			databaseAccessor.updateAvatar(player.getIdPlayer());
		}
		else {
			getConnector().sendMessage("ERROR");
		}		
	}

	private void processUpdateAccount(int type, String args) {
		String[] data = args.split(":");
		
		boolean check;
		
		switch(type) {
		case 0:
			check = databaseAccessor.updateUserName(player.getIdPlayer(), data[0], data[1]);
			break;
			
		case 1:
			check = databaseAccessor.updatePass(player.getIdPlayer(), data[0], data[1]);
			break;
			
		default:
			check = false;
			break;
		}
		
		if(check) {
			getConnector().sendMessage("OK");
		}
		else {
			getConnector().sendMessage("ERROR");
		}
	}

	private void processUpdateTables() {
		String msg = "RSUpdateTables@";
		int numberTable = getServer().getTables().size();
		if (numberTable > 0) {
			for (int i = 0; i < numberTable - 1; i++) {
				Table table = getServer().getTable(i);
				msg += table.getTableName() + ":" + table.getAmountBet() + "#";
			}
			Table table = getServer().getTable(numberTable - 1);
			msg += table.getTableName() + ":" + table.getAmountBet();
		} else {
			msg += "NONE";
		}
		getConnector().sendMessage(msg);		
	}

	private void processJoinTable(String tableName) {
		Table table = getTable(tableName);
		if (table != null) {
			if (table.addPlayer(player)) {
				getConnector().sendMessage("RSJoinTable@OK:" + table.getSize());
				player.setStatus(PlayerStatus.PLAY_GAME);			
				new GameProcessor(player);
			}
		} else {
			getConnector().sendMessage("RSJoinTable@ERROR");
		}	
	}

	private void processPlayRight() {
		for (Table table : getServer().getTables()) {
			if (table.addPlayer(player)) {
				getConnector().sendMessage("RSPlayRight@OK:" + table.getSize());
				player.setStatus(PlayerStatus.PLAY_GAME);
				new GameProcessor(player);
				return;
			}
		}
		getConnector().sendMessage("RSPlayRight@ERROR");		
	}

	private void processCreateTable(String args) {
		// args = "muctiencuoc:songuoichoi"
		String[] data = args.split(":");
		String tableName = data[0];
		int amount = Integer.parseInt(data[1]);
		int maxPlayer = Integer.parseInt(data[2]);
		
		if (getTable(data[0]) == null && amount <= player.getCredit()) {
			new Table(getServer(), player, tableName, amount, maxPlayer);
			getConnector().sendMessage("RSCreateTable@OK:" + maxPlayer);
			player.setStatus(PlayerStatus.PLAY_GAME);
			new GameProcessor(player);
		} else {
			getConnector().sendMessage("RSCreateTable@ERROR");
		}
	}

	private Table getTable(String tableName) {
		for (Table table : getServer().getTables()) {
			if (tableName.equals(table.getTableName())) {
				return table;
			}
		}
		return null;
	}

	private void processLogin(String args) {
		String[] data = args.split(":");
		player.setIdPlayer(databaseAccessor.access(data[0], data[1]));
		if (player.getIdPlayer() > 0) {

			player.setUserName(data[0]);
			getConnector().sendMessage("OK");

			player.setCredit(databaseAccessor.getCredit(player.getIdPlayer()));
			getConnector().sendMessage(player.getCredit() + "");

			player.setAvatar(databaseAccessor.getAvatar(player.getIdPlayer()));
			getConnector().sendImage(player.getAvatar());

		} else {
			getConnector().sendMessage("ERROR");
		}
	}
	
	private void processRegister(String args) {
		String[] data = args.split(":");
		boolean check = databaseAccessor.createAccount(data[0], data[1],data[2]);
		if (check)
			getConnector().sendMessage("OK");
		else
			getConnector().sendMessage("ERROR");
	}
}
