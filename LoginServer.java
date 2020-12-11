import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

public class LoginServer extends Thread{
	private String serverIp;
	private int loginPort;
	Statement statement;
	
	public LoginServer(String serverIp, int loginPort, Statement statement) {
		// TODO Auto-generated constructor stub
		this.serverIp = serverIp;
		this.loginPort = loginPort;
		this.statement = statement;
	}
	
	public void run() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		
		try {
			serverSocket = new ServerSocket(loginPort);
			System.out.println("login server is on...");
			//get login request
			while(true) {
				socket = serverSocket.accept();
				System.out.println("Login : from "+socket.getInetAddress()+":"+socket.getPort()+" trying to connect...");
				LoginHandler loginHandler = new LoginHandler(socket);
				loginHandler.start();
			}
		}catch(Exception e) {
			System.out.println("Login Server Failed!");
		}
	}
	
	public class LoginHandler extends Thread{
		
		Socket socket;
		InputStream in;
		OutputStream out;
		
		public LoginHandler(Socket socket) {
			// TODO Auto-generated constructor stub
			this.socket = socket;
			try {
				this.in = socket.getInputStream();
				this.out = socket.getOutputStream();
			}catch(Exception e) {
				System.out.println("Failed to connect login server!");
			}
		}
		
		public void inputHandler() {
			Pair<byte[], byte[]> ansPair = SR.receive(in, 100);
			if(ansPair == null) {
				in = null;
				out = null;
			}
			
			UserInfo ansUserInfo = Parser.readUserInfo(ansPair.b);
			
			if(Arrays.equals(ansPair.a, TLVdef.LOGINREQ.getBarr())) {
				loginSeq(ansUserInfo);
			}else if(Arrays.equals(ansPair.a, TLVdef.SIGNUPREQ.getBarr())) {
				signupSeq(ansUserInfo);
			}else {
				System.out.println("Login Server : Unknow request");
			}
		}
		
		public void run() {
			while(in != null) {
				try {
					inputHandler();
				}catch (Exception e) {
					System.out.println("Login Server : Fail to receive from client!");
				}
			}
		}
		
		private void loginSeq(UserInfo inUserInfo) {
			String sql = "select * from users where user_name = '" + inUserInfo.fromId+"'";
			try {
				ResultSet rs = statement.executeQuery(sql);
				String id = null;
				String pw = null;
				int userNum = -1;
				while(rs.next()) {
					userNum = rs.getInt("user_num");
					id = rs.getString("user_name");
					pw = rs.getString("user_pw");
				}
				if(id==null || pw==null) {
					System.out.println("Wrong UserInfo!");
					byte[] data = Parser.writeUserInfo(Parser.buildUserInfo(id, Integer.toString(userNum), false, pw));
					SR.send(TLVdef.LOGAUTH.getBarr(), data, out);
				}
				else {
					if(pw.equals(inUserInfo.pw)) {
						System.out.println(socket.getPort()+ " has loginned!");
						byte[] data = Parser.writeUserInfo(Parser.buildUserInfo(id, Integer.toString(userNum), true, pw));
						SR.send(TLVdef.LOGAUTH.getBarr(), data, out);
					}
					else {
						System.out.println("Wrong Password Or Id!");
						byte[] data = Parser.writeUserInfo(Parser.buildUserInfo(id, Integer.toString(userNum), false, pw));
						SR.send(TLVdef.LOGAUTH.getBarr(), data, out);
					}
				}
			}catch(Exception e) {
				System.out.println("Login : exception in login!");
			}
		}
		
		private void signupSeq(UserInfo inUserInfo) {
			//check if the name available
			String sqlString = "select * from users where user_name = '" + inUserInfo.toId+"'";
			try {
				ResultSet rs = statement.executeQuery(sqlString);
				if(rs.next()) {
					System.out.println("Signup : Not available name!");
					SR.send(TLVdef.IDUSABLE.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(null, null, false, null)), out);
				}else {
					try {
						sqlString = "insert into users (user_name, user_pw) values ('"+inUserInfo.toId+"','"+inUserInfo.pw+"');";
						int result = statement.executeUpdate(sqlString);
						if(result != 1) {
							System.out.println("Signup : failure in signup sql!");
							SR.send(TLVdef.IDUSABLE.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(null, null, false, null)), out);
						}
						System.out.println("Signup : signed up as "+inUserInfo.toId+"!");
						
						//send the result
						sqlString = "select * from users where user_name = '" + inUserInfo.toId +"'";
						ResultSet returnResultSet = statement.executeQuery(sqlString);
						String gotId = null;
						String gotPw = null;
						int userNum = -1;
						while(returnResultSet.next()) {
							userNum = returnResultSet.getInt("user_num");
							gotId = returnResultSet.getString("user_name");
							gotPw = returnResultSet.getString("user_pw");
						}
						if(gotId==null | gotPw==null) {
							System.out.println("Signup : Wrong UserInfo!");
							SR.send(TLVdef.IDUSABLE.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(null, null, false, null)), out);
						}else {
							SR.send(TLVdef.IDUSABLE.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(gotId, Integer.toString(userNum), true, gotPw)), out);
							System.out.println("Signup : signed up at : " + Integer.toString(userNum));
						}
					}catch (Exception e) {
						try {
							SR.send(TLVdef.IDUSABLE.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(null, null, false, null)), out);
							System.out.println("Signup : Exception!");
						}catch (Exception ee) {
						}
						System.out.println("Signup : Exception!");
					}
				}
			}catch (Exception e) {
				System.out.println("Signup : query failed!");
			}
		}
	
	}//LoginHandler
	
	
}
