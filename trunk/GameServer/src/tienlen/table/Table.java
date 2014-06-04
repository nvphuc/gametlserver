package tienlen.table;

import tienlen.connector.InforPlayer;
import tienlen.connector.InforTable;
import tienlen.newtype.CardsType;
import tienlen.player.GameVariables;
import tienlen.player.Player;
import tienlen.server.Server;

public class Table extends Thread {
	private Server server;

	private String tableName;
	private int amountBet;
	private int tableSize;
	private Player[] players;
	private int playersNumber = 0;
	private int[] listFinishGame;
	private int readyNumber;
	private int finishNumber;
	private int curPlayer;
	private int prePlayer;

	private int[] preCards;
	private int[] listSkipTurn;

	private boolean isEndGame;
	private boolean active = true;//neu chu phong roi phong thi active = false, va yeu cau moi nguoi roi phong

	public void run() {
exit:	while (players[0] != null) {
			// cho tat ca ready
			while (readyNumber != tableSize) {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (players[0] == null)
					break exit;
			}

			// Reset lai ban
			resetTable();

			// Reset lai cac gia tri cua nguoi choi
			for (int i = 0; i < tableSize; i++) {
				Player player = players[i];
				getVariables(player).resetVariables();
			}

			// Gui thong bao bat dau choi
			sendMessageToAllPlayers("StartGame");

			try {
				sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			sendMessageToAllPlayers("HideReady");

			Dealer dealer = new Dealer();

			// thuc hien chia bai cho cac nguoi choi
			for (int i = 0; i < tableSize; i++) {
				dealer.dealCards(getVariables(players[i]).cards, i);
			}

			// server gui cac quan bai xuong client
			for (int i = 0; i < tableSize; i++) {
				sendCards(players[i]);
			}

			// Gui thong bao danh xuong cac nguoi choi
			sendMessageToAllPlayers("Turn@" + curPlayer);

			// Cho den khi van dau ket thuc
			while (!isEndGame) {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
				}
			}

			// Gui ket qua ve client
			sendMessageToAllPlayers("GameResult");

			try {
				sleep(3000);
			} catch (InterruptedException e) {
			}

			String content = "";
			double delta = 2;
			if (tableSize % 4 == 0) {
				delta = 0.5;
			}
			double amount = 1;

			for (int i = 0; i < tableSize; i++) {
				double money = amountBet * amount;
				amount -= delta;
				int index = listFinishGame[i];
				if (!content.equals("")) {
					content += "#";
				}
				if (players[index] != null) {
					content += players[index].getUserName() + ":" + money;
				} else {
					content += "*";
				}
			}
			String report = "Report@" + content;
			sendMessageToAllPlayers(report);
			try {
				sleep(3000);
			} catch (InterruptedException e) {
			}
		}
		active = false;
		sendMessageToAllPlayers("RequiteLeaveTable");
		while(playersNumber != 0) {
			try {
				sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		server.removeTable(this);
	}

	private void sendCards(Player player) {
		// Cau truc message gui: "DealCards@card1_card2_..._card13"
		String message = "DealCards@";
		for (int i = 0; i < 13; i++) {
			if (i != 0) {
				message += "_";
			}
			message += getVariables(player).cards.get(i);
		}
		// message = "DealCards@4_5_8_9_12_13_14_15_16_17_18_19_20"; // test
		player.getConnector().sendMessage(message);
	}

	private void resetTable() {
		readyNumber = 0;
		finishNumber = 0;

		preCards = null;

		// moi phan tu tuong ung voi 1 player trong ban: 0-condanh, 1-boluot
		listSkipTurn = new int[tableSize];
		for (int i = 0; i < tableSize; i++) {
			listSkipTurn[i] = 0;
		}

		curPlayer = listFinishGame[0];
		if (curPlayer == -1)
			curPlayer = 0;

		prePlayer = -1;

		for (int i = 0; i < tableSize; i++) {
			listFinishGame[i] = -1;
		}
	}

	public Table(Server server, Player player, String tableName, int amountBet,
			int tableSize) {
		this.server = server;
		this.server.addTable(this);
		this.tableName = tableName;
		this.amountBet = amountBet;
		this.tableSize = tableSize;

		players = new Player[tableSize];
		for (int i = 0; i < tableSize; i++) {
			players[i] = null;
		}
		addPlayer(player);

		// stt tu 0 - > 3 tuong ung nhat nhi ba tu
		listFinishGame = new int[tableSize];
		for (int i = 0; i < tableSize; i++) {
			listFinishGame[i] = -1;
		}

		// moi phan tu tuong ung voi 1 player trong ban: 0-condanh, 1-boluot
		listSkipTurn = new int[tableSize];
		for (int i = 0; i < tableSize; i++) {
			listSkipTurn[i] = 0;
		}

		this.start();
	}

	public synchronized boolean addPlayer(Player player) {
		if (isAvailable(player)) {
			for (int index = 0; index < tableSize; index++) {
				if (players[index] == null) {
					players[index] = player;
					playersNumber++;
					players[index].setGame(new GameVariables(this, index));
					return true;
				}
			}
		}
		return false;
	}

	public boolean isAvailable(Player player) {
		return (getNumberPlayerInTable() < tableSize && amountBet < player
				.getCredit());
	}

	private int getNumberPlayerInTable() {
		int count = 0;
		for (int i = 0; i < tableSize; i++) {
			if (players[i] != null)
				count++;
		}
		return count;
	}

	public String getTableName() {
		return tableName;
	}

	public void sendInforTableToAllPlayers() {
		InforPlayer[] inforPlayers = new InforPlayer[tableSize];
		for (int i = 0; i < tableSize; i++) {
			if (players[i] != null) {
				Player player = players[i];
				int ready = 0;
				if (player.getGame().isReady)
					ready = 1;
				inforPlayers[i] = new InforPlayer(player.getUserName(),
						player.getAvatar(), player.getCredit(), ready);
			} else {
				inforPlayers[i] = null;
			}
		}
		InforTable inforTable = new InforTable(inforPlayers);
		for (int i = 0; i < tableSize; i++) {
			if (players[i] != null) {
				players[i].getConnector().sendMessage("InforTable");
				players[i].getConnector().sendInforTable(inforTable);
			}
		}
	}

	public void setSkipTurn(Player player) {
		// bo luot player
		listSkipTurn[getVariables(player).orderNumber] = 1;

		// dem so nguoi bo luot
		int count = 0;
		for (int i = 0; i < tableSize; i++) {
			if (listSkipTurn[i] == 1)
				count++;
		}
		// Chuyen luot
		switch (tableSize - finishNumber - count) {
		case 0:
			for (int i = 1; i < tableSize; i++) {
				int index = (prePlayer + 1) % 4;
				if (players[index] != null) {
					sendMessageToAllPlayers("NewTurn@" + index);
					return;
				}
			}
			break;

		case 1:
			for (int i = 0; i < tableSize; i++) {
				if (players[i] != null) {
					if (listSkipTurn[i] == 0
							&& getVariables(players[i]).isFinishGame == false) {
						sendMessageToAllPlayers("NewTurn@" + i);
						return;
					}
				}
			}
			break;

		default:
			for (int i = 1; i < tableSize; i++) {
				int index = (getVariables(player).orderNumber + 1) % 4;
				if (players[index] != null) {
					if (listSkipTurn[index] == 0
							&& getVariables(players[index]).isFinishGame == false) {
						sendMessageToAllPlayers("Turn@" + index);
						return;
					}
				}
			}
			break;
		}
	}

	private GameVariables getVariables(Player player) {
		return player.getGame();
	}

	public synchronized void updateGame(Player player, String listCards) {
		int[] cards = parseCard(listCards);
		preCards = cards;
		for (int i = 0; i < cards.length; i++) {
			getVariables(player).cards.remove(cards[i]);
		}

		// Kiem tra player co het bai
		if (getVariables(player).cards.size() == 0) {
			addFinishPlayer(player);
		}
		// neu tat ca deu het bai
		if (finishNumber == tableSize - 1) {
			for (int i = 0; i < tableSize; i++) {
				if (players[i] != null) {
					if (getVariables(players[i]).isFinishGame == false) {
						addFinishPlayer(players[i]);
						break;
					}
				}
			}
			isEndGame = true;
		}
		// nguoc lai con > 2 nguoi danh
		else {
			for (int i = 1; i < tableSize; i++) {
				int index = (getVariables(player).orderNumber + i) % 4;
				if (players[index] != null) {
					if (listSkipTurn[index] == 0
							&& getVariables(players[index]).isFinishGame != true) {
						sendMessageToAllPlayers("Turn@" + index);
						break;
					}
				}
			}
		}
	}

	private void addFinishPlayer(Player player) {
		for (int rank = 0; rank < tableSize; rank++) {
			if (listFinishGame[rank] == -1) {
				sendMessageToAllPlayers("Finish@"
						+ getVariables(player).orderNumber + ":" + rank);
				listFinishGame[rank] = getVariables(player).orderNumber;
				getVariables(player).isFinishGame = true;
				finishNumber++;
				return;
			}
		}
	}

	public synchronized int removePlayer(int orderNumber) {
		int valueReturn = 0;

		if (isPlaying()) {
			int rank = tableSize - 1;
			for (; rank > -1; rank--) {
				if (listFinishGame[rank] == -1) {
					listFinishGame[rank] = orderNumber;
					finishNumber++;
					break;
				}
			}
			valueReturn = countMoneyBet(rank);
		}

		players[orderNumber].setGame(null);
		players[orderNumber] = null;
		playersNumber--;
		if (active) {
			sendInforTableToAllPlayers();
		}

		return valueReturn;
	}

	public int countMoneyBet(int rank) {
		switch (tableSize) {
		case 2:
			switch (rank) {
			case 0:
				return amountBet;
			case 1:
				return -amountBet;
			default:
				return 0;
			}

		case 4:
			switch (rank) {
			case 0:
				return amountBet;
			case 1:
				return amountBet / 2;
			case 2:
				return -(amountBet / 2);
			case 3:
				return -amountBet;
			default:
				return 0;
			}

		default:
			return 0;
		}
	}

	public boolean isPlaying() {
		return !isEndGame;
	}

	public int getAmountBet() {
		return amountBet;
	}

	public int getSize() {
		return tableSize;
	}

	public synchronized void sendMessageToAllPlayers(String message) {
		for (Player player : players) {
			if (player != null)
				player.getConnector().sendMessage(message);
		}
	}

	public synchronized void increaseReadyNumber() {
		if (readyNumber < tableSize)
			readyNumber++;
	}

	public synchronized void decreaseReadyNumber() {
		if (readyNumber > 0)
			readyNumber--;
	}

	public boolean checkHitCards(String listCards) {
		int[] cards = parseCard(listCards);
		if (preCards == null) {
			if (getType(cards) == CardsType.ERROR) {
				return false;
			} else {
				return true;
			}
		} else {
			if (preCards.length == cards.length) {
				if (getType(preCards) != getType(cards)
						|| preCards[0] > cards[0]) {
					return false;
				} else {
					return true;
				}
			} else {
				if (getType(preCards) == CardsType.LE
						&& preCards[0] / 4 == 13
						&& (getType(cards) == CardsType.TUQUY || getType(cards) == CardsType.BADOITHONG)) {
					return true;
				} else {
					if (getType(preCards) == CardsType.DOI
							&& preCards[0] / 4 == 13
							&& (getType(cards) == CardsType.BONDOITHONG)) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
	}

	private int[] parseCard(String strCard) {
		String[] temp = strCard.split("_");
		int[] cards = new int[temp.length];
		for (int i = 0; i < cards.length; i++) {
			cards[i] = Integer.parseInt(temp[i]);
		}
		return cards;
	}

	private CardsType getType(int[] cards) {
		switch (cards.length) {

		case 1:
			return CardsType.LE;

		case 2:
			if (isDoi(cards[0], cards[1]))
				return CardsType.DOI;
			return CardsType.ERROR;

		case 3:
			if (isSam(cards))
				return CardsType.SAM;
			if (isSanh(cards))
				return CardsType.SANH;
			return CardsType.ERROR;

		case 4:
			if (isTuquy(cards))
				return CardsType.TUQUY;
			if (isSanh(cards))
				return CardsType.SANH;
			return CardsType.ERROR;

		case 6:
			if (isBadoithong(cards))
				return CardsType.BADOITHONG;
			if (isSanh(cards))
				return CardsType.SANH;
			return CardsType.ERROR;

		case 8:
			if (isBondoithong(cards))
				return CardsType.BONDOITHONG;
			if (isSanh(cards))
				return CardsType.SANH;
			return CardsType.ERROR;

		default:
			if (isSanh(cards))
				return CardsType.SANH;
			return CardsType.ERROR;
		}
	}

	private boolean isDoi(int card1, int card2) {
		if (card1 / 4 == card2 / 4)
			return true;
		return false;
	}

	private boolean isSam(int[] cards) {
		if (cards.length == 3 && isDoi(cards[0], cards[1])
				&& isDoi(cards[0], cards[2]))
			return true;
		return false;
	}

	private boolean isTuquy(int[] cards) {
		if (cards.length == 4 && isDoi(cards[0], cards[1])
				&& isDoi(cards[2], cards[3]) && isDoi(cards[0], cards[2]))
			return true;
		return false;
	}

	private boolean isBadoithong(int[] cards) {
		if (cards.length == 6 && isDoi(cards[0], cards[1])
				&& isDoi(cards[2], cards[3]) && isDoi(cards[4], cards[5])) {
			for (int i = 0; i < 3; i++) {
				if (cards[i * 2] / 4 != cards[0] / 4 - i) {
					return false;
				}
			}
			if (cards[0] / 4 == 13) {
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean isBondoithong(int[] cards) {
		if (cards.length == 8 && isDoi(cards[0], cards[1])
				&& isDoi(cards[2], cards[3]) && isDoi(cards[4], cards[5])
				&& isDoi(cards[6], cards[7])) {
			for (int i = 0; i < 4; i++) {
				if (cards[i * 2] / 4 != cards[0] / 4 - i) {
					return false;
				}
			}
			if (cards[0] / 4 == 13) {
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean isSanh(int[] cards) {
		if (cards.length > 2) {
			for (int i = 0; i < cards.length; i++) {
				if (cards[i] / 4 != cards[0] / 4 - i) {
					return false;
				}
			}
			if (cards[0] / 4 == 13) {
				return false;
			}
			return true;
		}
		return false;
	}
}
