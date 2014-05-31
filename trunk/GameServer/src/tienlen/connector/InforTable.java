package tienlen.connector;

import java.io.Serializable;

public class InforTable implements Serializable {
	private InforPlayer[] inforPlayers;
	
	public InforTable(InforPlayer[] inforPlayers) {
		this.inforPlayers = inforPlayers;		
	}

	public InforPlayer[] getInforPlayers() {
		return inforPlayers;
	}
}