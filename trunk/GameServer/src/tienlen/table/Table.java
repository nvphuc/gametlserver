package tienlen.table;

import javax.swing.ImageIcon;

import tienlen.newtype.CardsType;
import tienlen.player.GameVariables;
import tienlen.player.Player;
import tienlen.server.Server;

public class Table extends Thread {
	public Server server;

	public String tableName;
	public int amountBet;
	public int tableSize;
	public Player[] players;
	public int playersNumber;
	public int[] listFinishGame;
	public int readyNumber;
	public int finishNumber;
	public int curPlayer;
	public int prePlayer;

	public int[] preCards;
	public int[] listSkipTurn;

	public boolean isEndGame;
	public boolean active = true;// neu chu phong roi phong thi active = false,
									// va yeu cau moi nguoi roi phong

	public void kiemtralistBoLuot() {
		System.out.print("ListSkipTurn:" );
		for(int i = 0; i < tableSize; i++) {
			System.out.print(" " + listSkipTurn[i]);
		}
		System.out.println();
	}
	
	public void kiemtralistFinishGame() {
		System.out.print("ListFinishGame:" );
		for(int i = 0; i < tableSize; i++) {
			System.out.print(" " + listFinishGame[i]);
		}
		System.out.println();
	}
	
	public void ktra() {
		kiemtralistBoLuot();
		kiemtralistFinishGame();
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

		this.readyNumber = 0;
		this.start();
	}

	public void run() {
		exit:while (players[0] != null) {
			// cho tat ca ready
			System.out.println("Doi readyNumber:");
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
				sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//sendMessageToAllPlayers("HideReady");

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
			System.out.println("Doi endGame:");
			while (!isEndGame) {
				try {
					sleep(3000);
				} catch (InterruptedException e) {
				}
			}

			// Gui ket qua ve client
			/*sendMessageToAllPlayers("GameResult");

			try {
				sleep(3000);
			} catch (InterruptedException e) {
			}

			String content = "";
			/*double delta = 2;
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
			}*/
			/*for(int i = 0; i < tableSize; i++) {
				if(content.equals("")) {
					content += listFinishGame[i];
					System.out.println(listFinishGame[i]);
				}
				else {
					content += ":" + listFinishGame[i];
				}
			}
			String report = "Report@" + content;
			sendMessageToAllPlayers(report);
			try {
				sleep(3000);
			} catch (InterruptedException e) {
			}*/
		}
		active = false;
		sendMessageToAllPlayers("RequiteLeaveTable");
		/*while(playersNumber != 0) {
			try {
				sleep(1000);
			} catch (InterruptedException e) {
			}
		}*/
		server.removeTable(this);
	}

	public void sendCards(Player player) {
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

	public void resetTable() {
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

	public synchronized boolean addPlayer(Player player) {
		if (isAvailable(player)) {
			for (int index = 0; index < tableSize; index++) {
				if (players[index] == null) {
					players[index] = player;
					playersNumber++;
					System.out.println("addPlayer: " + playersNumber + " " + index);//
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

	public int getNumberPlayerInTable() {
		int count = 0;
		for (int i = 0; i < tableSize; i++) {
			if (players[i] != null)
				count++;
		}
		return count;
	}

	public String getTableName() {
		return this.tableName;
	}

	public void sendInforTableToAllPlayers() {
		for(int i = 0; i < tableSize; i++) {
			if(players[i] != null) {
				sendMessageToAllPlayers("InforTable@" + i);
				sendMessageToAllPlayers(players[i].getUserName());
				sendMessageToAllPlayers(players[i].getCredit() + "");
				sendImageForAllPlayer(players[i].getAvatar());
				sendMessageToAllPlayers(players[i].getGame().isReady+"");
			}
		}
	}
	
	public void sendImageForAllPlayer(ImageIcon img) {
		for(int i = 0; i < tableSize; i++){
			if(players[i]!=null) {
				players[i].getConnector().sendImage(img);
			}
		}
	}

	public Player getPlayer(int index) {
		return players[index];
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
				int index = (prePlayer + 1) % tableSize;
				if (players[index] != null) {
					System.out.println("0 gui new turn");
					for(int j = 0; j < tableSize; j++) {
						listSkipTurn[j] = 0;
					}
					preCards = null;
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
						System.out.println("1 gui new turn");
						for(int j = 0; j < tableSize; j++) {
							listSkipTurn[j] = 0;
						}
						preCards = null;
						sendMessageToAllPlayers("NewTurn@" + i);
						return;
					}
				}
			}
			break;

		default:
			for (int i = 1; i < tableSize; i++) {
				int index = (getVariables(player).orderNumber + 1) % tableSize;
				if (players[index] != null) {
					if (listSkipTurn[index] == 0
							&& getVariables(players[index]).isFinishGame == false) {
						System.out.println("Con lai gui turn");
						sendMessageToAllPlayers("Turn@" + index);
						return;
					}
				}
			}
			break;
		}
	}

	public GameVariables getVariables(Player player) {
		return player.getGame();
	}

	public synchronized void updateGame(Player player, String listCards) {
		String[] cards = parseCardToString(listCards);

		// luu cac la da danh lai vao preCards
		preCards = parseCardToInt(listCards);
		for (int i = 0; i < cards.length; i++) {
			getVariables(player).cards.remove(cards[i]);
		}

		// Kiem tra player co het bai
		if (getVariables(player).cards.size() == 0) {
			addFinishPlayer(player);
		}
		ktra();
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
				int index = (getVariables(player).orderNumber + i) % tableSize;
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

	private int[] parseCardToInt(String listCards) {
		String[] tmp = listCards.split("_");
		int[] arrCard = new int[tmp.length];
		for(int i = 0; i < tmp.length; i++) {
			arrCard[i] = Integer.parseInt(tmp[i]);
		}
		return arrCard;
	}

	public void addFinishPlayer(Player player) {
		for (int rank = 0; rank < tableSize; rank++) {
			if (listFinishGame[rank] == -1) {
				listFinishGame[rank] = getVariables(player).orderNumber;
				getVariables(player).isFinishGame = true;
				
				String content = "";
				for(int i = 0; i< tableSize; i++) {
					if(content.equals("")) {
						content += listFinishGame[i];
					}
					else {
						content += ":" + listFinishGame[i];
					}
				}
				player.getConnector().sendMessage("Finish@"+content);
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
			//sendInforTableToAllPlayers();
			sendMessageToAllPlayers("LeaveTable@"+orderNumber);
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
		int[] cards = parseCardToInt(listCards);
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

	public String[] parseCardToString(String strCard) {
		return strCard.split("_");
	}

	public CardsType getType(int[] cards) {
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

	public boolean isDoi(int card1, int card2) {
		if (card1 / 4 == card2 / 4)
			return true;
		return false;
	}

	public boolean isSam(int[] cards) {
		if (cards.length == 3 && isDoi(cards[0], cards[1])
				&& isDoi(cards[0], cards[2]))
			return true;
		return false;
	}

	public boolean isTuquy(int[] cards) {
		if (cards.length == 4 && isDoi(cards[0], cards[1])
				&& isDoi(cards[2], cards[3]) && isDoi(cards[0], cards[2]))
			return true;
		return false;
	}

	public boolean isBadoithong(int[] cards) {
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

	public boolean isBondoithong(int[] cards) {
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

	public boolean isSanh(int[] cards) {
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
