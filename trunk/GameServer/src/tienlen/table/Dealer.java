package tienlen.table;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

public class Dealer {
	private int deck[] = new int[52];
	private ArrayList<Integer> cards = new ArrayList<Integer>();
	Random ran = new Random();

	public Dealer() {
		cards.clear();
		for (int i = 0; i < 52; i++) {
			cards.add(i + 4);
		}
		int pos;
		for (int j = 0; j < 52; j++) {
			pos = ran.nextInt(cards.size());
			deck[j] = cards.get(pos);
			cards.remove(pos);
		}
	}

	// chia bai cho nguoi choi
	public void dealCards(Vector<Integer> playerCards, int index) {
		for(int i = 0; i < 13; i++) {
			playerCards.add(deck[index]);
			index += 4;
		}
	}
}
