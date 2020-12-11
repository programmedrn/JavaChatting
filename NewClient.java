import java.net.Socket;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NewClient {

	static String serverIp = "127.0.0.1";
	static int loginPort = 7777;
	static int chatPort = 8888;
	static boolean login = false;
	int myUserNum;
	
	Socket loginSocket;
	Socket chatSocket;
	LoginClient loginClient;
	ChattingClient chattingClient;
	
	public NewClient() {
		myUserNum = -1;
		try {
			loginSocket = new Socket(serverIp, loginPort);
			loginClient = new LoginClient(loginSocket);
		}catch (Exception e) {
			System.out.println("Client : Failed to get to the server!!");
			loginClient = null;
			chattingClient = null;
		}
	}
	
	public boolean chkConnected() {
		if(loginClient!=null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean login(String initId, String initPw) {
		myUserNum = loginClient.login(initId, initPw);
		
		if(myUserNum>0) {login = true;}
		
		if(login) {
			System.out.println("User logged in as : " + initId + " :: " + Integer.toString(myUserNum));
			try {
				chatSocket = new Socket(serverIp, chatPort);
				chattingClient = new ChattingClient(chatSocket,initId,myUserNum);
//				chattingClient.startChat(initId, myUserNum);
				return true;
			}catch (Exception e) {
				// TODO: handle exception
				System.out.println("Client : Failed to get to the chat server");
			}
		}
		return false;
	}
	
	public void inter(String str) {
		chattingClient.sendChatT(str);
	}
	
	public void passChatReq(int req, String message) {
		switch(req) {
		case 1://chatt
			chattingClient.sendChatT(message);
			break;
		case 2://chate
			chattingClient.sendChatE(message);
			break;
		case 3://logoutreq
			chattingClient.sendLogout();
			break;
		case 4://idchgreq
			chattingClient.sendIdChg(message);
			break;
		}
	}
	
	public void signup(String initId, String initPw) {
		loginClient.signup(initId, initPw);
	}

	public void setChatFrame(ChattingFrame chattingFrame) {
		chattingClient.setFrame(chattingFrame);
	}
}
