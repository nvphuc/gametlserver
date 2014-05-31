package tienlen.connector;

import java.io.Serializable;

import javax.swing.ImageIcon;

public class InforPlayer implements Serializable {
	
	private String UserName;
	private ImageIcon Avatar;
	private int money;
	private int Status;
	
	// Status = 0: chua san sang; = 1: da san sang
	public InforPlayer(String UserName, ImageIcon Avatar, int money, int Status) {
		this.UserName = UserName;
		this.Avatar = Avatar;
		this.money = money;
		this.Status = Status;
	}

	public String getUserName() {
		return UserName;
	}

	public ImageIcon getAvatar() {
		return Avatar;
	}

	public int getMoney() {
		return money;
	}

	public int getStatus() {
		return Status;
	}
}