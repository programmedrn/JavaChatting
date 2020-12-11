import java.io.Serializable;

public class UserInfo implements Serializable{
	// LOGINREQ, SIGNUPREQ, IDCHGREQ, LOGAUTH, LOGOUTREQ, LOGOUTPF, IDUSABLE, IDCHGED, CHATINIT
	String fromId;
	String toId;
	String pw;
	boolean pass;
}
