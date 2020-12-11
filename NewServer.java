import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class NewServer {
	
	static String serverIp = "127.0.0.1";
	static int loginPort = 7777;
	static int chatPort = 8888;

	static final String ubuntuJDBC = "com.mysql.jdbc.Driver";
	static final String windowJDBC = "com.mysql.cj.jdbc.Driver";
	static final String DBname = "ktchatdb";
	static final String UBUNTUurl = "jdbc:mysql://localhost/"+DBname+"?&useSSL=false";
	static final String WINDOWurl = "jdbc:mysql://localhost/"+DBname+"?&serverTimezone=UTC&useSSL=false";	
	static final String DBid = "manager";
	static final String DBpw ="manager123";
	static volatile Connection dbConn = null;
	static volatile Statement state = null;
	
	public static boolean dbConnect(String os) {
		try {
			if(os.equals("ubuntu"))	{
				Class.forName(ubuntuJDBC);
				dbConn = DriverManager.getConnection(UBUNTUurl,DBid,DBpw);
			}
			if(os.equals("window"))	{
				Class.forName(windowJDBC);
				dbConn = DriverManager.getConnection(WINDOWurl,DBid,DBpw);
			}
			System.out.println("DB connector found!");
			state = dbConn.createStatement();
			System.out.println("DB connected...");
			return true;
		} catch (ClassNotFoundException e) {
			System.out.println("unable to find DB connector!");
			return false;
		} catch (Exception e) {
			System.out.println("DB not connected!");
			return false;
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		while(!dbConnect("ubuntu")) {
			System.out.println("DB connect failed!");
			try {
				Thread.sleep(1000);
			} catch(Exception e) {
				System.out.println("sleep failed...");
			}
		}
		Thread loginThread = new Thread(new LoginServer(serverIp, loginPort, state));
		Thread chatThread = new Thread(new ChattingServer(serverIp, chatPort, state));
		loginThread.start();
		chatThread.start();
	}

}
