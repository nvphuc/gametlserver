package tienlen.connector;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;

public class Connector {
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	public Connector(Socket socket) {
		this.socket = socket;
		try {
			oos = new ObjectOutputStream(this.socket.getOutputStream());
			ois = new ObjectInputStream(this.socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized boolean disconnect() {
		try {
			oos.close();
			ois.close();
			socket.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized boolean sendMessage(String message) {
		try {
			oos.writeObject(message);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized boolean sendImage(ImageIcon image) {
		try {
			oos.writeObject(image);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public String receiveMessage() {
		String message = "";
		try {
			message = (String) ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
		}
		return message;
	}
	
	public boolean receiveImage(int id) {
		try {
			byte[] image = (byte[]) ois.readObject();
			FileOutputStream outToHardDisk = new FileOutputStream("Avatars/" + id + ".png");
			outToHardDisk.write(image);
			outToHardDisk.close();
			return true;
		} catch (IOException | ClassNotFoundException e) {
			return false;
		}
	}
}