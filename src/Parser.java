import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;


public class Parser {
	
	static Chat readChat(byte[] input) {
		Chat result = null;
		
		try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(input))){
			result = (Chat)ois.readObject();
		}catch(Exception e) {
			System.out.println("Serializing Failed : reading chat");
		}
		
		return result;
	}
	
	static Chat buildChat(int userNum, LocalDateTime time, boolean isEmoji, String body) {
		Chat result = new Chat();
		
		result.userNum = userNum;
		result.time = time;
		result.isEmoji = isEmoji;
		result.body = body;
		
		return result;
	}
	
	static byte[] writeChat(Chat chat) {
		byte[] result = null;
		
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos)){
			oos.writeObject(chat);
			result = baos.toByteArray();
		}catch (Exception e) {
			System.out.println("Serializing Failed : writing Chat");
		}
		
		return result;
	}
	
	static UserInfo readUserInfo(byte[] input) {
		UserInfo result = null;
		
		try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(input))){
			result = (UserInfo)ois.readObject();
		}catch(Exception e) {
			System.out.println("Serializing Failed : reading userinfo");
		}
		
		return result;
	}
	
	static UserInfo buildUserInfo(String fromId, String toId, boolean pass, String pw) {
		UserInfo result = new UserInfo();
		
		result.fromId = fromId;
		result.toId = toId;
		result.pass = pass;
		result.pw = pw;
		
		return result;
	}
	
	static byte[] writeUserInfo(UserInfo ui) {
		byte[] result = null;
		
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos)){
			oos.writeObject(ui);
			result = baos.toByteArray();
		}catch (Exception e) {
			System.out.println("Serializing Failed : writing UserInfo");
		}
		
		return result;
	}
}
