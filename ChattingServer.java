import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class ChattingServer extends Thread{
	private final String serverIp;
	private final int chatPort;
	Statement statement;
	HashMap<Integer, OutputStream> clients;		//int, outputstream		need to be sync
	LinkedHashMap<Integer, String> usernameMap;	//int, namestring		need to be sync
	ArrayList<Pair<byte[], Chat>> chatLog;//						need to be sync
	volatile AtomicInteger chatCount;//				need to be sync
	HashMap<String, ArrayList<Pair<AtomicInteger, AtomicInteger>>> logPerUsers; //"usernum", <startCnt, endCnt>
	
	public ChattingServer(String serverIp, int chatPort, Statement statement) {
		// TODO Auto-generated constructor stub
		this.serverIp = serverIp;
		this.chatPort = chatPort;
		this.statement = statement;
		
		clients = new HashMap<Integer, OutputStream>();
		usernameMap = new LinkedHashMap<Integer, String>();
		chatLog = new ArrayList<Pair<byte[], Chat>>();
		chatCount = new AtomicInteger(0);
		logPerUsers = new HashMap<String, ArrayList<Pair<AtomicInteger, AtomicInteger>>>();
	}
	
	public void run() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		
		try {
			serverSocket = new ServerSocket(chatPort);
			System.out.println("chatting server is on...");
			//get login request
			while(true) {
				socket = serverSocket.accept();
				System.out.println("Chatting : from "+socket.getInetAddress()+":"+socket.getPort()+" trying to connect...");
				ChatHandler chatHandler = new ChatHandler(socket);
				chatHandler.start();
			}
		}catch(Exception e) {
			System.out.println("Chatting Server Failed!");
		}
	}
	
	
	public class ChatHandler extends Thread{
		
		Socket socket;
		InputStream in;
		OutputStream out;
		int myChatCount;
		int myUserNum;
		boolean alive;
		
		public ChatHandler(Socket socket) {
			this.socket = socket;
			try {
				this.in = socket.getInputStream();
				this.out = socket.getOutputStream();
				alive = true;
			}catch (Exception e) {
				System.out.println("Failed to connect chatting server!");
			}
		}
		
		public void chatText(byte[] input) {
			Chat chat = Parser.readChat(input);
			Chat serverChat = Parser.buildChat(chat.userNum, LocalDateTime.now(), false, chat.body);
			byte[] result = Parser.writeChat(serverChat);
			synchronized (chatCount) {
				//save in chatlog
				chatLog.add(new Pair<byte[], Chat>(TLVdef.CHATT.getBarr(), serverChat));
				chatCount.incrementAndGet();
				//send to all
				Iterator it = clients.keySet().iterator();
				while(it.hasNext()) {
					OutputStream outputStream = (OutputStream)clients.get(it.next());
					try {
						SR.send(TLVdef.CHATT.getBarr(), result, outputStream);
					}catch (Exception e) {
						System.out.println("ChatT : Failed to send chat text!");
					}
				}
			}
		}
		
		public void chatEmoji(byte[] input) {
			Chat chat = Parser.readChat(input);
			Chat serverChat = Parser.buildChat(chat.userNum, LocalDateTime.now(), true, chat.body);
			byte[] result = Parser.writeChat(serverChat);
					
			//does this emoji exist?
			
			
			synchronized (chatCount) {
				//save in chatlog
				chatLog.add(new Pair<byte[], Chat>(TLVdef.CHATE.getBarr(), serverChat));
				chatCount.incrementAndGet();
				//send to all
				Iterator it = clients.keySet().iterator();
				while(it.hasNext()) {
					OutputStream outputStream = (OutputStream)clients.get(it.next());
					try {
						SR.send(TLVdef.CHATE.getBarr(), result, outputStream);
					}catch (Exception e) {
						System.out.println("ChatE : Failed to send chat emoji!");
					}
				}
			}
		}
		
		public void initChat(byte[] input) {
			UserInfo userInfo = Parser.readUserInfo(input);
			myUserNum = Integer.parseInt(userInfo.toId);
			//remember nowa chatcount
			synchronized (chatCount) {
				this.myChatCount = chatCount.intValue();
			}
			//3. put in the map
			//num, out
			clients.put(myUserNum, out);
			//num, name
			usernameMap.put(myUserNum, userInfo.fromId);
			//1. send users info\
			try {
				usernameMap.forEach((key, value)->{
					UserInfo tempInfo = Parser.buildUserInfo(Integer.toString((int)key), (String)value, false, null);
					SR.send(TLVdef.CHATINIT.getBarr(), Parser.writeUserInfo(tempInfo), out);
				});
			}catch (Exception e) {
				System.out.println("INIT : failed to init userInfo!");
				e.printStackTrace();
			}
			
			//2. send chat logs
			ArrayList<Pair<AtomicInteger, AtomicInteger>> chatArrayList = logPerUsers.get(Integer.toString(myUserNum));
			if(chatArrayList!=null) {
				for (Pair<AtomicInteger, AtomicInteger> item : chatArrayList) {
					//resend all chats
					for(int i = item.a.intValue();i<=item.b.intValue();i++) {
						Pair<byte[],Chat> chatPair = chatLog.get(i);
						try {
								SR.send(chatPair.a, Parser.writeChat(chatPair.b), out);
						}catch (Exception e) {
							System.out.println("INIT : failed to send chat log info!");
						}
					}//for each chat
				}
			}
			for(int i=myChatCount; i<chatCount.intValue(); i++) {
				Pair<byte[],Chat> chatPair = chatLog.get(i);
				try {
						SR.send(chatPair.a, Parser.writeChat(chatPair.b), out);
				}catch (Exception e) {
					System.out.println("INIT : failed to send residual chat!");
				}
			}
			
			
			try {//invite
				SR.send(TLVdef.CHATINVI.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(null, null, true, null)), out);
			}catch (Exception e) {
				System.out.println("INIT : Couldn't invite!");
			}
			//4. notify welcome
			Chat serverChat = Parser.buildChat(myUserNum, LocalDateTime.now(), true, userInfo.fromId);
			byte[] result = Parser.writeChat(serverChat);
			synchronized (chatCount) {
				//save in chatlog
				chatLog.add(new Pair<byte[], Chat>(TLVdef.CHATIN.getBarr(), serverChat));
				chatCount.incrementAndGet();
			}
			//send to all
			Iterator it = clients.keySet().iterator();
			try {
				while(it.hasNext()) {
					OutputStream outputStream = (OutputStream)clients.get(it.next());
					SR.send(TLVdef.CHATIN.getBarr(), result, outputStream);
				}
			}catch (Exception e) {
				System.out.println("INIT : failed to notify welcome!");
			}
			System.out.println("INIT : " + myUserNum);
		}
		
		public void logoutChat(byte[] input) {
			UserInfo userInfo = Parser.readUserInfo(input);
			//1. leave log
			synchronized (chatCount) {
				if(logPerUsers.get(Integer.toString(myUserNum))==null) {
					logPerUsers.put(Integer.toString(myUserNum), new ArrayList<Pair<AtomicInteger, AtomicInteger>>());
				}
				logPerUsers.get(Integer.toString(myUserNum)).add(new Pair<AtomicInteger, AtomicInteger>(new AtomicInteger(myChatCount), new AtomicInteger(chatCount.intValue())));
			}
			
			//2. notify logout
			Chat serverChat = Parser.buildChat(myUserNum, LocalDateTime.now(), false, userInfo.fromId);
			byte[] result = Parser.writeChat(serverChat);
			synchronized (chatCount) {
				//save in chatlog
				chatLog.add(new Pair<byte[], Chat>(TLVdef.CHATIN.getBarr(), serverChat));
				chatCount.incrementAndGet();
			}

			//send to all
			Iterator it = clients.keySet().iterator();
			try {
				while(it.hasNext()) {
					OutputStream outputStream = (OutputStream)clients.get(it.next());
						SR.send(TLVdef.CHATIN.getBarr(), result, outputStream);
					}
			}catch (Exception e) {
				System.out.println("Logout : failed to notify logout!");
			}
			//3. send ok sign
			try {
				SR.send(TLVdef.LOGOUTPF.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo((String)usernameMap.get(myUserNum), null, true, null)), out);
			}catch (Exception e) {
				System.out.println("Logout : failed to logout!");
			}

			clients.remove(myUserNum);
			usernameMap.remove(myUserNum);
			System.out.println(myUserNum);
			System.out.println(": out");
			
		}
		
		public void idchgReq(byte[] input) {
			UserInfo userInfo = Parser.readUserInfo(input);
			if(userInfo.toId == null) {//no null id
				try {
					SR.send(TLVdef.IDUSABLE.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(null, null, false, null)), out);
				}catch(Exception e) {
					System.out.println("IDCHG : ID can't be null!");
				}
			}
			//check if the name available
			String sqlString = "select * from users where user_name = '" + userInfo.toId+"'";
			try {
				ResultSet rs = statement.executeQuery(sqlString);
				if(rs.next()) {//id not available
					System.out.println("IDCHG : Not available name!");
					SR.send(TLVdef.IDUSABLE.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(null, null, false, null)), out);
				}else {//id available
					sqlString = "update users set user_name = '"+userInfo.toId+"' where user_num = "+Integer.parseInt(userInfo.fromId)+";";
					try {
						//update
						statement.executeUpdate(sqlString);
						String beforeId = usernameMap.get(Integer.parseInt(userInfo.fromId));
						usernameMap.replace(Integer.parseInt(userInfo.fromId), userInfo.toId);
						//send ok
						SR.send(TLVdef.IDUSABLE.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(userInfo.fromId, userInfo.toId, true, null)), out);
						//notify to all
						Chat serverChat = Parser.buildChat(myUserNum, LocalDateTime.now(), false,
								beforeId + " changed name : to ["+userInfo.toId+ "]!");
						byte[] result = Parser.writeChat(serverChat);
						synchronized (chatCount) {
							//save in chatlog
							chatLog.add(new Pair<byte[], Chat>(TLVdef.IDCHGED.getBarr(), serverChat));
							chatCount.incrementAndGet();
						}
						Iterator it = clients.keySet().iterator();
						try {
							while(it.hasNext()) {
								OutputStream outputStream = (OutputStream)clients.get(it.next());
									SR.send(TLVdef.IDCHGED.getBarr(), result, outputStream);
								}
						}catch (Exception e) {
							System.out.println("IDCHG : failed to notify welcome!");
						}
					}catch (Exception e) {
						System.out.println("IDCHG : Failed to update DB!");
						e.printStackTrace();
						SR.send(TLVdef.IDUSABLE.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(null, null, false, null)), out);
					}
				}
			}catch (Exception e) {
				System.out.println("IDCHG : Failed to get data from DB!");
				SR.send(TLVdef.IDUSABLE.getBarr(), Parser.writeUserInfo(Parser.buildUserInfo(null, null, false, null)), out);
			}
		}
		
		public void inputHandler() throws IOException {
			Pair<byte[], byte[]> ansPair = SR.receive(in, 100);
			if(ansPair == null) {
				in = null;
			}
			if(Arrays.equals(ansPair.a, TLVdef.CHATT.getBarr())) {
				//chat send
				chatText(ansPair.b);
				
			}else if(Arrays.equals(ansPair.a, TLVdef.CHATE.getBarr())) {
				//emoji send
				chatEmoji(ansPair.b);
				
			}else if(Arrays.equals(ansPair.a, TLVdef.CHATINIT.getBarr())) {
				//chat init req
				initChat(ansPair.b);
				
			}else if(Arrays.equals(ansPair.a, TLVdef.LOGOUTREQ.getBarr())){
				//logout req
				logoutChat(ansPair.b);
			}else if(Arrays.equals(ansPair.a, TLVdef.IDCHGREQ.getBarr())) {
				//idchg req
				idchgReq(ansPair.b);
				
			}else {
				System.out.println("Chatting Server : Unknown request");
			}
		}
		
		public void run() {
			while(in!=null) {
				try {
					inputHandler();
				}catch (Exception e) {
					System.out.println("Chatting Server : Fail to receive from client! client seems like to be closed");
				}
			}
		}
		
	}//ChatHandler
}
