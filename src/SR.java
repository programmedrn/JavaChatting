import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class SR{
	public static int send(byte[] type, byte[] obj, OutputStream out) {
		byte[] result;
		byte[] data = obj;
		int len = data.length;
		byte[] length = new byte[4];
		length[0] = (byte)(len>>24);
		length[1] = (byte)(len>>16);
		length[2] = (byte)(len>>8);
		length[3] = (byte)(len);
		
		result = new byte[len+8];
		System.arraycopy(type,  0, result, 0, 4);
		System.arraycopy(length, 0, result, 4, 4);
		System.arraycopy(data, 0, result, 8, len);
		try {
			out.write(result);
			return 1;
		}catch(Exception e) {
			System.out.println("SR : sending failed!");
			return -1;
		}
	}
	

	public static Pair<byte[], byte[]> receive(InputStream in, int len) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] typeLenBuffer = new byte[8];
		byte[] ansBuffer = new byte[len];
		try {
			in.read(typeLenBuffer);
			baos.write(typeLenBuffer,0,8);
			byte[] typeLen = baos.toByteArray();
			byte[] type = new byte[4];
			System.arraycopy(typeLen, 0, type, 0, 4);
			int ansLen = 0;
			for(int i=4;i<8;i++) {
				int key = (int)((typeLen[i]&0xFF)<<((7-i)*8));
				ansLen += key;
			}
			if(ansLen<=0)return null;
			int readLen;
//			int temp = ansLen;
//			while(temp>0) {
//				readLen = in.read(ansBuffer);
//				temp -= readLen;
//				baos.write(ansBuffer,0,readLen);
//				if(readLen<len)break;
//			}
			
			for(int i=0;i<ansLen/len;i++) {
				readLen = in.read(ansBuffer);
				baos.write(ansBuffer,0,readLen);
			}
			if(ansLen%len!=0) {
				byte[] resid = new byte[ansLen%len];
				readLen = in.read(resid);
				baos.write(resid,0,readLen);
			}
			
			
			byte[] answer = new byte[ansLen];
			System.arraycopy(baos.toByteArray(), 8, answer, 0, ansLen);
            return new Pair<byte[], byte[]>(type,answer);
		}catch(Exception e) {
			System.out.println("SR : getting answer failed!");
			return null;
		}
	}
}
