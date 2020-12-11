
public class Pair<T,V> {
	T a;
	V b;
	public T getF() {
		return this.a;
	}
	public V getS() {
		return this.b;
	}
	Pair(T a, V b){
		this.a = a; this.b = b;
	}
}
