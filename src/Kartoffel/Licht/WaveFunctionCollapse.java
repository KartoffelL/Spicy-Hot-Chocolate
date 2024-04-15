package Kartoffel.Licht;

import java.util.ArrayList;
import java.util.List;

import Kartoffel.Licht.WaveFunctionCollapse.TileRules.entry;

public class WaveFunctionCollapse {
	
	public static class TileRules {
		public static class entry {
			int x, y, up, left, bottom, right;
			float e;

			public entry(int x, int y, int up, int left, int bottom, int right, float e) {
				super();
				this.x = x;
				this.y = y;
				this.up = up;
				this.left = left;
				this.bottom = bottom;
				this.right = right;
				this.e = e;
			}
			
		}
		public List<entry> rules = new ArrayList<entry>();
		public TileRules(String text) {
			String[] entries = text.split("\n");
			for(int i = 0; i < entries.length; i++) {
				if(entries[i].startsWith("#") || entries[i].isEmpty())
					continue;
				String tile = get(entries[i], '[', ']');
				String[] tileC = tile.split(";");
				int x = Integer.parseInt(tileC[0].strip());
				int y = Integer.parseInt(tileC[1].strip());
				String ids = get(entries[i], '{', '}');
				String[] idss = ids.split(";");
				int a = Integer.parseInt(idss[0].strip());
				int b = Integer.parseInt(idss[1].strip());
				int c = Integer.parseInt(idss[2].strip());
				int d = Integer.parseInt(idss[3].strip());
				float e = 1;
				if(idss.length > 4)
					e = Float.parseFloat(idss[4].strip());
				rules.add(new entry(x, y, a, b, c, d, e));
			}
		}
		public String get(String s, char start, char end) {
			int a = s.indexOf(start);
			int b = s.indexOf(end);
			return s.substring(a+1, b);
		}
		public entry getEntry(int x, int y) {
			for(entry e : rules)
				if(e.x == x && e.y == y)
					return e;
			return null;
		}
		public void getValid(entry up, entry left, entry down, entry right, List<entry> base) {
			base.removeIf((e)->!valid(e, up, left, down, right));
		}
		public boolean valid(entry that, entry up, entry left, entry down, entry right) {
			if(that == null)
				return true;
			if(up != null)
				if(up.bottom != that.up)
					return false;
			if(left != null)
				if(left.right != that.left)
					return false;
			if(down != null)
				if(down.up != that.bottom)
					return false;
			if(right != null)
				if(right.left != that.right)
					return false;
			return true;
		}
	}
	
	public static entry pickRandom(List<entry> l) {
		float max = 0;
		for(entry e : l)
			max += e.e;
		if(max == 0)
			return l.get(Main.random.nextInt(l.size()));
		float r = Main.random.nextFloat(max);
		int index = 0;
		float v = 0;
		while((v+=l.get(index).e) < r) index++;
		return l.get(index);
	}

}
