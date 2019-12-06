package KR.util;

import java.util.HashMap;

/**
 * A pair of object IDs
 * @author ZP
 *
 */
public class KeyPair {

	private int key1;
	private int key2;

	public KeyPair() {
	}

	public KeyPair(int key1, int key2) {
		this.key1 = key1;
		this.key2 = key2;
	}

	public int getFirst() {
		return key1;
	}

	public int getSecond() {
		return key2;
	}

	@Override
	public boolean equals(Object o) {
		KeyPair k = (KeyPair) o;
		if (k.getFirst() == this.key1 && k.getSecond() == this.key2) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {

		// return (int)this.key1*10000 + (int)this.key2;
		return 42;
	}

	public boolean Contains(int key) {
		if (key == this.getFirst() || key == this.getSecond())
			return true;
		else
			return false;
	}

	public static void main(String[] args){
		KeyPair k1 = new KeyPair(1,2);
		KeyPair k2 = new KeyPair(1,2);
		KeyPair k3 = new KeyPair(3,4);
		KeyPair k4 = new KeyPair(5,6);
		HashMap<KeyPair,Integer> hm = new HashMap<KeyPair,Integer>();
		hm.put(k1, 1);
		hm.put(k2, 2);
		hm.put(k3, 3);
		hm.put(k4, 4);
		for(KeyPair kp : hm.keySet())
		{
			System.out.print(kp.hashCode()+" " + hm.get(kp)+ " \n");
		}
	}
}
