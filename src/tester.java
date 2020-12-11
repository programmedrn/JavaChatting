import java.util.LinkedHashMap;

public class tester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		LinkedHashMap<String, Integer> testHashMap = new LinkedHashMap<>();
		testHashMap.put("first", 2);
		testHashMap.put("second", 3);
		testHashMap.put("third", 8);
		testHashMap.put("fourth", 7);
		testHashMap.put("fifth", 0);
		LinkedHashMap<Integer, String> intStrMap = new LinkedHashMap<Integer, String>();
		for(String keyString : testHashMap.keySet()) {
			intStrMap.put(testHashMap.get(keyString), keyString);
		}
//		
		for(String itemString : testHashMap.keySet()) {
			System.out.println(itemString +" : " + testHashMap.get( itemString));
		}
		
		testHashMap.forEach((k,v)->{
			System.out.println(k +" : " + v);
		});
		System.out.println("-------------------");
		for(Integer itemString : intStrMap.keySet()) {
			System.out.println(itemString +" : " + intStrMap.get( itemString));
		}
		 
		intStrMap.forEach((k,v)->{
			System.out.println(k +" : " + v);
		});
		
//		int len = Integer.MAX_VALUE;
//		System.out.println((byte)(len>>24));
//		System.out.println((byte)(len>>16));
//		System.out.println((byte)(len>>8));
//		System.out.println((byte)(len>>0));
		
//		[______][______][___________]
		
	}

}
