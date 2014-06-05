package tienlen.database;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class DataBaseAccessor {

	private Connection con;

	public boolean createAccount(String userName, String pass1, String pass2) {
		boolean check = true;
		Statement stmt = null;
		try {
			con = ConnectDataBase.CreateConnection();
			con.setAutoCommit(false);
			stmt = con.createStatement();

			String sql = "select * from ACCOUNTS where USERNAME = '" + userName
					+ "';";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				if (userName.equals(rs.getString("USERNAME"))) {
					check = false;
					break;
				}
			}
			rs.close();

			if (check) {
				sql = "insert into ACCOUNTS (USERNAME, PASS1, PASS2) values ('"
						+ userName + "', '" + pass1 + "', '" + pass2 + "');";
				stmt.executeUpdate(sql);
				con.commit();
			}

			stmt.close();
			con.close();
			return check;
		} catch (Exception ex) {
			return false;
		}
	}

	public int access(String userName, String pass) {
		int id = -1;
		Statement stmt = null;
		try {
			con = ConnectDataBase.CreateConnection();
			con.setAutoCommit(false);
			stmt = con.createStatement();
			String sql = "select ID, USERNAME, ISLOGIN from ACCOUNTS where USERNAME = '"
					+ userName + "' and PASS1 = '" + pass + "';";
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				if (userName.equals(rs.getString("USERNAME"))
						&& rs.getInt("ISLOGIN") == 0) {
					id = rs.getInt("ID");
					sql = "update ACCOUNTS set ISLOGIN = 1 where ID = " + id
							+ ";";
					stmt.executeUpdate(sql);
					con.commit();
				}
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception ex) {
		}
		return id;
	}

	public boolean logout(int idPlayer) {
		Statement stmt = null;
		try {
			con = ConnectDataBase.CreateConnection();
			con.setAutoCommit(false);
			stmt = con.createStatement();
			String sql = "update ACCOUNTS set ISLOGIN = 0 where ID = "
					+ idPlayer + ";";
			stmt.executeUpdate(sql);
			stmt.close();
			con.commit();
			con.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public String getDate() {
		Date todayD = new Date(System.currentTimeMillis());
		SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");
		String todayS = dayFormat.format(todayD.getTime());
		return todayS;
	}

	public ImageIcon getAvatar(int idPlayer) {
		String avatar = "";
		Statement stmt = null;
		try {
			con = ConnectDataBase.CreateConnection();
			con.setAutoCommit(false);
			stmt = con.createStatement();
			String sql = "select AVATAR from ACCOUNTS where ID = " + idPlayer
					+ ";";
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				avatar = rs.getString("AVATAR");
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception ex) {
		}
		if (!avatar.equals("")) {
			try {
				BufferedImage tmp = ImageIO.read(new File("Avatars/" + avatar
						+ ".png"));
				return new ImageIcon(tmp.getScaledInstance(100, 100,
						Image.SCALE_SMOOTH));
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}

	public int getCredit(int idPlayer) {
		int money = 0;
		Statement stmt = null;
		try {
			con = ConnectDataBase.CreateConnection();
			con.setAutoCommit(false);
			stmt = con.createStatement();
			String sql = "select CREDIT from ACCOUNTS where ID = " + idPlayer
					+ ";";
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				money = rs.getInt("CREDIT");
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception ex) {
		}
		return money;
	}

	public boolean updateUserName(int idPlayer, String userName, String pass2) {
		boolean check = false;
		Statement stmt = null;
		try {
			con = ConnectDataBase.CreateConnection();
			con.setAutoCommit(false);
			stmt = con.createStatement();

			String sql = "select PASS2 from ACCOUNTS where ID = " + idPlayer
					+ ";";
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				if (pass2.equals(rs.getString("PASS2"))) {
					check = true;
				}
			}
			sql = "select * from ACCOUNTS where USERNAME = '" + userName//
					+ "';";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {//
				if (userName.equals(rs.getString("USERNAME"))) {
					check = false;
					break;
				}
			}

			rs.close();

			if (check) {
				sql = "update ACCOUNTS set USERNAME = '" + userName
						+ "' where ID = " + idPlayer + ";";
				stmt.executeUpdate(sql);
			}

			stmt.close();
			con.commit();
			con.close();
			return check;
		} catch (Exception ex) {
			return false;
		}
	}

	public boolean updatePass(int idPlayer, String pass, String pass2) {
		boolean check = false;
		Statement stmt = null;
		try {
			con = ConnectDataBase.CreateConnection();
			con.setAutoCommit(false);
			stmt = con.createStatement();

			String sql = "select PASS2 from ACCOUNTS where ID = " + idPlayer
					+ ";";
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				if (pass2.equals(rs.getString("PASS2"))) {
					check = true;
				}
			}
			rs.close();

			if (check) {
				sql = "update ACCOUNTS set PASS1 = '" + pass + "' where ID = "
						+ idPlayer + ";";
				stmt.executeUpdate(sql);
			}

			stmt.close();
			con.commit();
			con.close();
			return check;
		} catch (Exception ex) {
			return false;
		}
	}

	public boolean updateAvatar(int idPlayer) {
		String name = idPlayer + "";
		Statement stmt = null;
		try {
			con = ConnectDataBase.CreateConnection();
			con.setAutoCommit(false);
			stmt = con.createStatement();
			String sql = "update ACCOUNTS set AVATAR = '" + name
					+ "' where ID = " + idPlayer + ";";
			stmt.executeUpdate(sql);
			stmt.close();
			con.commit();
			con.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public boolean updateCredit(int idPlayer, int amount) {
		Statement stmt = null;
		try {
			con = ConnectDataBase.CreateConnection();
			con.setAutoCommit(false);
			stmt = con.createStatement();
			String sql = "update ACCOUNTS set CREDIT = " + amount
					+ " where ID = " + idPlayer + ";";
			stmt.executeUpdate(sql);
			con.commit();
			stmt.close();
			con.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
}
