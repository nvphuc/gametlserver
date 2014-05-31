package tienlen.processor;

import tienlen.newtype.PlayerStatus;
import tienlen.player.GameVariables;
import tienlen.player.Player;
import tienlen.table.Table;

public class GameProcessor extends Processor {

	public GameProcessor(Player player) {
		super(player);
		getTable().sendInforTableToAllPlayers();
	}

	@Override
	public void run() {
		while (player.getStatus() == PlayerStatus.PLAY_GAME) {
			handleMessage(getConnector().receiveMessage());
		}
		if (player.getStatus() == PlayerStatus.DISCONNECT) {
			disconnect();
		}
	}

	@Override
	protected void handleMessage(String message) {
		String[] args = message.split("@");

		switch (args[0]) {
		case "Chat":
			processChat(message);
			break;

		case "Ready":
			processReady();
			break;

		case "HitCards":
			processHitCards(args[1]);
			break;

		case "SkipTurn":
			processSkipTurn();
			break;

		case "LeaveTable":
			processLeaveTable();
			break;

		case "GetPlayerCanInvite":
			processGetListInvite();
			break;
			
		case "Invite":
			processInvite(args[1]);
			break;

		default:
			int amount = getTable()
					.removePlayer(getGameVariables().orderNumber);
			amount += player.getCredit();
			player.setCredit(amount);
			databaseAccessor.updateCredit(player.getIdPlayer(), player.getCredit());
			player.setStatus(PlayerStatus.DISCONNECT);
			break;
		}
	}

	private void processInvite(String playerName) {
		Player invitedplayer = getServer().getPlayer(playerName);
		if(invitedplayer != null && invitedplayer.getStatus() == PlayerStatus.CONNECT) {
			String msg = "Invited@" + this.player.getUserName() + ":" + getTable().getTableName() + ":" + getTable().getAmountBet();
			invitedplayer.getConnector().sendMessage(msg);
		}
	}

	private void processGetListInvite() {
		String content = "";
		for (Player player : getServer().getPlayers()) {
			if (player.getStatus() == PlayerStatus.CONNECT && player.getCredit() > getTable().getAmountBet()) {
				if (content.equals("")) {
					content += player.getUserName() + ":" + player.getCredit();
				}
				else {
					content += "#" + player.getUserName() + ":" + player.getCredit();
				}
			}
		}
		if(content.equals("")) {
			content += "NONE";
		}
		getConnector().sendMessage("RSGetPlayerCanInvite@" + content);
	}

	public void processLeaveTable() {
		// roi phong
		int amount = getTable().removePlayer(getGameVariables().orderNumber);
		player.addCredit(amount);
		// cap nhat lai tien khi roi ban
		databaseAccessor.updateCredit(player.getIdPlayer(), player.getCredit());

		// cap nhat lai trang thai player
		player.setStatus(PlayerStatus.CONNECT);
		new PlayerProcessor(player);
	}

	private void processSkipTurn() {
		getTable().setSkipTurn(player);
	}

	private void processHitCards(String listCards) {
		if (getTable().checkHitCards(listCards)) {
			getConnector().sendMessage("RSHitCards@OK");
			String msg = "HitCards@" + player.getGame().orderNumber + ":"
					+ listCards;
			getTable().sendMessageToAllPlayers(msg);
			getTable().updateGame(player, listCards);
		} else {
			getConnector().sendMessage("RSHitCards@ERROR");
		}
	}

	private void processReady() {
		player.getGame().isReady = true;
		String msg = "Ready@" + player.getGame().orderNumber;
		getTable().sendMessageToAllPlayers(msg);
		getTable().increaseReadyNumber();
	}

	private void processChat(String content) {
		String msg = "Chat@" + player.getUserName() + ":" + content;
		getTable().sendMessageToAllPlayers(msg);
	}

	private Table getTable() {
		return getGameVariables().table;
	}

	private GameVariables getGameVariables() {
		return player.getGame();
	}
}
