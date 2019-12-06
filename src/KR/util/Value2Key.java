package KR.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Value2Key {

	private Map<Object, Object> map = new HashMap<Object, Object>();

	public Value2Key(Map<Object, Object> map) {
		this.map = map;
	}

	public List<Object> getKeys(Object value) {
		ArrayList<Object> keys = new ArrayList<Object>();
		for (Entry<Object, Object> entry : this.map.entrySet()) {
			if (value.equals(entry.getValue())) {
				keys.add(entry.getKey());
			} else {
				continue;
			}
		}
		return keys;
	}

	public static void main(String[] args) {
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		map.put(1, "a");
		map.put(2, "d");
		map.put(3, "a");
		map.put(4, "b");
		map.put(5, "a");

		Value2Key searcher = new Value2Key(map);
		for (Object obj : searcher.getKeys("a")) {
			System.out.println(obj);
		}
	}
}
