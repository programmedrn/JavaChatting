
public enum TLVdef {//usage : TLVdef.XXXXXX;
	LOGINREQ(1),	//client	login	1	no-log
	LOGAUTH(7),		//server	login	1	no-log
	LOGOUTREQ(2),	//client	login	2	no-log
	LOGOUTPF(8),	//server	chat	1	log
	SIGNUPREQ(3),	//client	id		1	no-log
	IDCHGREQ(4),	//client	id		2	no-log
	IDUSABLE(9),	//server	id		1	no-log
	IDCHGED(10),	//server	id		2	log
	CHATIN(11),		//server	chat	2	log
	CHATINIT(12),	//client	chat	3	no-log
	CHATT(5),		//client	chat	1	log
	CHATE(6),		//client	chat	2	log
	CHATINVI(13);	//server 	chat	3	no-log

	final byte[] arr;
	
	
	private TLVdef(int type) {
		byte[] temp = new byte[4];
		switch(type) {//from client or from server
		case 1: case 2: case 3: case 4: case 5: case 6: case 12: temp[0]=(byte)1;break;
		case 7: case 8: case 9: case 10: case 11: case 13: temp[0]=(byte)2;break;
		}
		switch(type) {//1 : login, 2: id, 3: chat
		case 1: case 2: case 7: temp[1]=(byte)1;break;
		case 3: case 4: case 9: case 10: temp[1]=(byte)2;break;
		case 5: case 6: case 8: case 11: case 12: case 13: temp[1]=(byte)3;break;
		}
		switch(type) {//numbering
		case 1: case 3: case 5: case 7: case 9: case 8: temp[2]=(byte)1;break;
		case 2: case 4: case 6: case 11: case 10: temp[2]=(byte)2;break;
		case 12: case 13: temp[2]=(byte)3;break;
		}
		switch(type) {//0: no-log 1: log
		case 5: case 6: case 8: case 10: case 11: temp[3]=(byte)1;break;
		default: temp[3]=(byte)0;
		}
		this.arr = temp;
	}
	public byte[] getBarr() {
		return this.arr;
	}
}
