package tienlen.connector;

import java.io.Serializable;

import javax.swing.ImageIcon;

public class InforPlayer implements Serializable {
	
	private String UserName;
	private ImageIcon Avatar;
	private int Credits;
	private int Status;
	
	// Status = 0: chua san sang; = 1: da san sang
	public InforPlayer(String UserName, ImageIcon Avatar, int Credit, int Status) {
		this.UserName = UserName;
		this.Avatar = Avatar;
		this.Credits = Credits;
		this.Status = Status;
	}

	public String getUserName() {
		return UserName;
	}

	public ImageIcon getAvatar() {
		return Avatar;
	}

	public int getCredit() {
		return Credits;
	}

	public int getStatus() {
		return Status;
	}
}