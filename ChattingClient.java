import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class ChattingClient {
	
	Socket socket;
	InputStream in;
	OutputStream out;
	String myIdString;
	int myUserNum;
	LinkedHashMap<String, String> usernameMap;
	ArrayList<Pair<byte[], Chat>> chatLog;
	ChattingFrame myChattingFrame;
	
	public void setFrame(ChattingFrame chattingFrame) {
		if(chattingFrame == null) {
			System.out.println("Chat : frame is empty!");
			return;
		}
		this.myChattingFrame = chattingFrame;
		this.myChattingFrame.setTitle(this.myChattingFrame.getTitle() + " : " + myIdString);
		this.startChat(myIdString);
	}
	
	public ChattingClient(Socket socket,String initId, int userNum) {
		
		this.usernameMap = new LinkedHashMap<String, String>();
		this.chatLog = new ArrayList<Pair<byte[], Chat>>();
		this.myIdString = initId;
		this.myUserNum = userNum;
		
		this.socket = socket;
		try {
			this.in = socket.getInputStream();
			this.out = socket.getOutputStream();
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("Chat : Failed to get IO!");
		}
	}
	
	public void startChat(String id) {
		// TODO Auto-generated method stub
		System.out.println("Chat : Entering to the Chatting System...");
		
//		usernameMap.put(Integer.toString(myUserNum), id);
		
		while(!initChat(id)){
			try {
				Thread.sleep(1000);
			}catch (Exception e) {
				System.out.println("Chat : Exception in init");
			}
		}
		Thread chatReceiver = new Thread(new ChatReceiver());
		chatReceiver.start();
	}
	
	public boolean initChat(String id) {
		UserInfo userInfo = Parser.buildUserInfo(id, Integer.toString(myUserNum), true, null);
		try {
			SR.send(TLVdef.CHATINIT.getBarr(), Parser.writeUserInfo(userInfo), out);
			Pair<byte[], byte[]> ansPair = null;
			ansPair = SR.receive(in, 100);
			//get userinfo
			while(Arrays.equals(ansPair.a, TLVdef.CHATINIT.getBarr())){
				UserInfo ansUserInfo = Parser.readUserInfo(ansPair.b);
				usernameMap.put(ansUserInfo.fromId, ansUserInfo.toId);
				ansPair = SR.receive(in, 100);
			}
			//get chat log
			while(
					!Arrays.equals(ansPair.a, TLVdef.CHATINVI.getBarr())
//					Arrays.equals(ansPair.a, TLVdef.CHATINIT.getBarr()) || Arrays.equals(ansPair.a, TLVdef.CHATE.getBarr())
					){
				Chat ansChat = Parser.readChat(ansPair.b);
				chatLog.add(new Pair<byte[], Chat>(ansPair.a, ansChat));
				ansPair = SR.receive(in, 100);
			}
			System.out.println("here");
			//chk finished
			if(Arrays.equals(ansPair.a, TLVdef.CHATINVI.getBarr())) {
				//succeed
				System.out.println("INIT : Succeed init chatting");
				if(Parser.readUserInfo(ansPair.b).pass) return true;
				else return false;
			}else {
				System.out.println("INIT : Failed init chatting");
				return false;
			}
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("INIT : Exception in init chatting");
			e.printStackTrace();
			return false;
		}
	}
	
	//sending methods
	public void sendChatT(String message) {
		if(message == null) {
			return;
		}
		try {
			SR.send(TLVdef.CHATT.getBarr(), Parser.writeChat(Parser.buildChat(myUserNum, null, false, message)), out);
		}catch (Exception e) {
			System.out.println("sending chatt failed!");
		}
	}
	
	public void sendChatE(String message) {
		if(message == null) {
			return;
		}
		try {
			SR.send(TLVdef.CHATE.getBarr(), Parser.writeChat(Parser.buildChat(myUserNum, null, false, message)), out);
		}catch (Exception e) {
			System.out.println("sending chate failed!");
		}
	}
	
	public void sendIdChg(String newId) {
		try {
			SR.send(TLVdef.IDCHGREQ.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(Integer.toString(myUserNum), newId, false, null)), out);
		}catch (Exception e) {
			System.out.println("sending idchg failed!");
		}
	}
	
	public void sendLogout() {
		try {
			SR.send(TLVdef.LOGOUTREQ.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(usernameMap.get(Integer.toString(myUserNum)), Integer.toString(myUserNum), false, null)), out);
		}catch (Exception e) {
			System.out.println("sending idchg failed!");
		}
	}
	
	public class ChatReceiver extends Thread{

		public void printChat(Pair<byte[], Chat> inChat) {
			Chat chat = inChat.b;
			if(Arrays.equals(inChat.a, TLVdef.CHATIN.getBarr())) {
				if(inChat.b.isEmoji) {
					//log in
					myChattingFrame.printOnChat(
							"###SERVER###"+" : "+usernameMap.get(Integer.toString(chat.userNum))+ " is in! / " + chat.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
				}else {
					//log out
					myChattingFrame.printOnChat(
							"###SERVER###"+" : "+chat.body+ " is leaving! / " + chat.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
				
				}
			}else if(Arrays.equals(inChat.a, TLVdef.IDCHGED.getBarr())) {
				//id chged
				myChattingFrame.printOnChat(
						"###SERVER###"+" : "+ chat.body + "/" + chat.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			
			}else if(Arrays.equals(inChat.a, TLVdef.CHATT.getBarr()) || Arrays.equals(inChat.a, TLVdef.CHATE.getBarr())){
				if(chat.isEmoji == false) {
					//chat text
					myChattingFrame.printOnChat(
						usernameMap.get(Integer.toString(chat.userNum))+" : "+chat.body+ " / " + chat.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
				}else {
					//chat emoji
					myChattingFrame.emojiOnChat(usernameMap.get(Integer.toString(chat.userNum)), chat.body, chat.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
				}
			}
		}
		
		public void printChat(ArrayList<Pair<byte[], Chat>> chatArrayList) {
			myChattingFrame.clearChat();
			for(Pair<byte[], Chat> item : chatArrayList) {
				printChat(item);
			}
		}
		
		public void printUser() {
			myChattingFrame.userBoardArea.setText("");
			for(String numString : usernameMap.keySet()){
				myChattingFrame.getUserArea().append((String)usernameMap.get(numString));
				myChattingFrame.userBoardArea.append("\n");
			}
		}
		
		public void chatT(Chat chat) {
			Pair<byte[], Chat> pair = new Pair<byte[], Chat>(TLVdef.CHATT.getBarr(), chat);
			chatLog.add(pair);
			printChat(pair);
		}
		
		public void chatE(Chat chat) {
			Pair<byte[], Chat> pair = new Pair<byte[], Chat>(TLVdef.CHATT.getBarr(), chat);
			chatLog.add(pair);
			printChat(pair);
		}
		
		public void userInOrOut(Chat chat) {
			//about someone
			//put in the log
			Pair<byte[], Chat> pair = new Pair<byte[], Chat>(TLVdef.CHATIN.getBarr(), chat);
			chatLog.add(pair);
			if(chat.isEmoji) {//is in
				//put in the username map
				usernameMap.put(Integer.toString(chat.userNum), chat.body);
				//print
				printChat(pair);
				printUser();
			}else {//is out
				//remove from the map
				printChat(pair);
				usernameMap.remove(Integer.toString(chat.userNum));
				//print
				printUser();
			}
		}
		
		public void idchged(Chat chat) {
			Pair<byte[], Chat> pair = new Pair<byte[], Chat>(TLVdef.IDCHGED.getBarr(), chat);
			chatLog.add(pair);
			
			String nextId = chat.body.substring(chat.body.indexOf("[")+1, chat.body.indexOf("]"));
			usernameMap.replace(Integer.toString(chat.userNum), nextId);
			//1209 temp
			printUser();
			printChat(chatLog);
		}
		
		public void inputHandler() {
			Pair<byte[], byte[]> ansPair = SR.receive(in, 100);
			if(ansPair == null) {
				in = null;
			}
			if(Arrays.equals(ansPair.a, TLVdef.CHATT.getBarr())) {
				//chat sent (Chat)
				Chat sentChat = Parser.readChat(ansPair.b);
				chatT(sentChat);
			}else if(Arrays.equals(ansPair.a, TLVdef.CHATE.getBarr())) {
				//emoji sent (Chat)
				Chat sentChat = Parser.readChat(ansPair.b);
				chatE(sentChat);				
			}else if(Arrays.equals(ansPair.a, TLVdef.CHATIN.getBarr())) {
				//someone's leaving or incoming(Chat)
				Chat sentChat = Parser.readChat(ansPair.b);
				userInOrOut(sentChat);
			}else if(Arrays.equals(ansPair.a, TLVdef.LOGOUTPF.getBarr())) {
				//end of logout seq (UserInfo)
				UserInfo sentUserInfo = Parser.readUserInfo(ansPair.b);
				if(sentUserInfo.pass) {
					//logout
					System.out.println("Good Bye");
					System.exit(0);;
				}else {
					//failed to logout
					System.out.println("Failed to leave!");
				}
			}else if(Arrays.equals(ansPair.a, TLVdef.IDCHGED.getBarr())) {
				//someone changed id (Chat)
				Chat sentChat = Parser.readChat(ansPair.b);
				idchged(sentChat);
				
			}else if(Arrays.equals(ansPair.a, TLVdef.IDUSABLE.getBarr())) {
				//id usable(UserInfo)
				UserInfo sentUserInfo = Parser.readUserInfo(ansPair.b);
				if(sentUserInfo.pass) {
					myChattingFrame.printOnChat("### Accepted Changing ###");
					myChattingFrame.setTitle("Chat : " + sentUserInfo.toId);
				}else {
					myChattingFrame.printOnChat("### Not available name... ###");
				}
				
			}else {
				System.out.println("Chatting Client : Unknown request");
			}
		}
		
		public void run() {
			printChat(chatLog);
			printUser();
			while(in!=null) {
				try {
					inputHandler();
				}catch (Exception e) {
					System.out.println("Chatting Client : Fail to receive from server!");
				}
			}
		}
		
	}
}
