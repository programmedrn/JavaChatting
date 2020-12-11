import java.io.Serializable;
import java.time.*;

public class Chat implements Serializable{
	//CHATT, CHATE, CHATSET
	int userNum;
	boolean isEmoji;
	LocalDateTime time;
	String body;
}
