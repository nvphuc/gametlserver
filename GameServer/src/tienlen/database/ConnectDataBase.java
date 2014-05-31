package tienlen.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectDataBase {
	
    private static Connection con;

    public static Connection CreateConnection() throws Exception {
    	String strConnect = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=database\\gametienlen.accdb";
    	Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        con = DriverManager.getConnection(strConnect);
        return con;
    }
}
