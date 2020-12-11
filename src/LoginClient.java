import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class LoginClient {
	
	InputStream in;
	OutputStream out;
	
	public LoginClient(Socket socket) {
		try {
			this.in = socket.getInputStream();
			this.out = socket.getOutputStream();
		}catch (Exception e) {
			System.out.println("Login : Failed to get IO!");
		}
	}
	
	public int login(String id, String pw) {
		int myUserNum = -1;
		//null check
		if(id == null || pw == null) {
			System.out.println("LoginSeq : null id / pw!");
			return -2;
		}
		//send user data
		try {
			byte[] data = Parser.writeUserInfo(Parser.buildUserInfo(id, null, false, pw));
			SR.send(TLVdef.LOGINREQ.getBarr(), data, out);
			
			
		}catch (Exception e) {
			System.out.println("LoginSeq : Exception occurred while sending!");
		}
		//get answer
		try {
			Pair<byte[], byte[]>ansPair = SR.receive(in, 100);
			if(Arrays.equals(ansPair.a, TLVdef.LOGAUTH.getBarr())) {
				UserInfo ansUserInfo = Parser.readUserInfo(ansPair.b);
				if(ansUserInfo.pass) {
					System.out.println("LoginSeq : Successfully logged in!");
					myUserNum = Integer.parseInt(ansUserInfo.toId);
				}else {
					System.out.println("LoginSeq : Failed to logged in!");
					myUserNum = -1;
				}
			}else {
				System.out.println("LoginSeq : Unexpected return!");
				myUserNum = -3;
			}
			return myUserNum;
		}catch (Exception e) {
			System.out.println("LoginSeq : Exception while receiving!");
			return -4;
		}
	}
	
	public int signup(String id, String pw) {
		byte[] data = Parser.writeUserInfo(Parser.buildUserInfo(null, id, false, pw));
		//send data
		try {
			SR.send(TLVdef.SIGNUPREQ.getBarr(), data, out);
		}catch(Exception e) {
			System.out.println("SignupSeq : sending failed!");
			return -1;
		}
		//get answer
		try {
			Pair<byte[], byte[]> ansPair = SR.receive(in, 101);
			if (Arrays.equals(TLVdef.IDUSABLE.getBarr(),ansPair.a)) {
				UserInfo userInfo = Parser.readUserInfo(ansPair.b);
				if(userInfo.pass) {
					System.out.println("SignupSeq : Signed up as " + userInfo.fromId + " : " + userInfo.pw);
					return 1;
				}else {
					System.out.println("SignupSeq : Signing failed!");
					return -1;
				}
			}else {
				System.out.println("SignupSeq : Unexpected return!");
				return -1;
			}
		}catch(Exception e) {
			System.out.println("getting answer failed!");
			return -1;
		}
	}
	
	public class receives extends Thread{
		
		public void run() {
			while(in!=null) {
				try {
					Pair<byte[], byte[]> ansPair = SR.receive(in, 100);
					if (Arrays.equals(TLVdef.IDUSABLE.getBarr(),ansPair.a)) {
						UserInfo userInfo = Parser.readUserInfo(ansPair.b);
						if(userInfo.pass) {
							System.out.println("SignupSeq : Signed up as " + userInfo.fromId + " : " + userInfo.pw);
						}else {
							System.out.println("SignupSeq : Signing failed!");
							
						}
					}else {
						System.out.println("SignupSeq : Unexpected return!");
					}
				}catch(Exception e) {}
			}
			
		}
		
	}
	
}
