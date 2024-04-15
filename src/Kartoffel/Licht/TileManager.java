package Kartoffel.Licht;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.SimplexNoise;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import Kartoffel.Licht.WaveFunctionCollapse.TileRules;
import Kartoffel.Licht.WaveFunctionCollapse.TileRules.entry;
import Kartoffel.Licht.Java.BufferedImage;
import Kartoffel.Licht.Rendering.Model;
import Kartoffel.Licht.Rendering.Shaders.Shader;
import Kartoffel.Licht.Rendering.Shapes.SBox2D;
import Kartoffel.Licht.Rendering.Texture.Texture;

public class TileManager {
	
	public int MAP_WIDTH = 100;
	public int MAP_HEIGHT = 100;
	
	public Shader shader;
	public Texture[] tileSet;
	public BufferedImage height;
	public Texture tileMap;
	public ByteBuffer data;
	public Model fullsq;
	public Vector2f offse = new Vector2f(0, 0);
	public Vector2f scale = new Vector2f(100);
	public Vector2f SETTILESIZE = new Vector2f(8, 8);
	
	public Vector4f tint = new Vector4f();
	
	public int CURRENTSET = 0;
	
	public final int MAX_CORRECTIONS = 100;
	public final int CORRECTION_SIZE = 2;
	public int[] ANGEL_POS = new int[2];
	public int[] DEMON_POS = new int[2];
	
	public TileManager(int width, int height) {
		this.MAP_WIDTH = width;
		this.MAP_HEIGHT = height;
		this.shader = new Shader(FileLoader.loadText("tile.vs"), FileLoader.loadText("tile.fs"), "Tile Shader");
		tileSet = new Texture[] {new Texture(FileLoader.loadImage("setlv1.png")), new Texture(FileLoader.loadImage("setlv2.png")), new Texture(FileLoader.loadImage("setlv3.png")), new Texture(FileLoader.loadImage("setlv4.png")), new Texture(FileLoader.loadImage("setlv5.png"))};
		this.height = FileLoader.loadImage("height.png");
		fullsq = new Model(new SBox2D(1, 1));
		data = MemoryUtil.memCalloc(MAP_WIDTH*MAP_HEIGHT*4);
		tileMap = new Texture(MAP_WIDTH, MAP_HEIGHT, GL33.GL_RGBA);
		tileMap.setFiltering(true, true);
		TileRules collapseR = new TileRules(FileLoader.loadText("rules.txt"));
//		try {
//			String path = new File(TileManager.class.getProtectionDomain().getCodeSource().getLocation()
//				    .toURI()).getParentFile().getPath()+"/rules.txt";
//			System.out.println(path);
//			FileInputStream fis = new FileInputStream(new File(path));
//			collapseR = new TileRules(new String(fis.readAllBytes(), Charset.forName("UTF-8")));
//			fis.close();
//		} catch (URISyntaxException | IOException e) {
//			e.printStackTrace();
//		}
		//Map generation//////////////////////////////////
		@SuppressWarnings("unchecked")
		List<entry>[][] entropy = new ArrayList[MAP_WIDTH][MAP_HEIGHT];
		for(int i = 0; i < MAP_WIDTH; i++) {
			for(int l = 0; l < MAP_HEIGHT; l++) {
				entropy[i][l] = new ArrayList<entry>();
				entropy[i][l].addAll(collapseR.rules);
			}
		}
		boolean[][] collapsed = new boolean[MAP_WIDTH][MAP_HEIGHT];
		double rot = Main.random.nextDouble(Math.PI);
		ANGEL_POS[0] = (int) (Math.cos(rot)*MAP_WIDTH/3)+MAP_WIDTH/2;
		ANGEL_POS[1] = (int) (Math.sin(rot)*MAP_WIDTH/3)+MAP_HEIGHT/2;
		DEMON_POS[0] = MAP_WIDTH-ANGEL_POS[0];
		DEMON_POS[1] =  MAP_HEIGHT-ANGEL_POS[1];
		{
			int antX = (int) (ANGEL_POS[0]+Math.signum(DEMON_POS[0]-ANGEL_POS[0])*2);
			int antY = (int) (ANGEL_POS[1]+Math.signum(DEMON_POS[1]-ANGEL_POS[1])*2);
			for(int i = 0; i < 250; i++) {
//				float dst = (float) Math.sqrt((antX-MAP_WIDTH)*(antX-MAP_WIDTH)+(antY-MAP_HEIGHT)*(antY-MAP_HEIGHT));
				int dx = (int) Math.signum(Math.signum(DEMON_POS[0]-antX)+SimplexNoise.noise(antY, i/50f)*2);
				int dy = (int) Math.signum(Math.signum(DEMON_POS[1]-antY)+SimplexNoise.noise(antX, i/50f)*2);
				collapsed[antX][antY] = true;
				entropy[antX][antY].clear();
				entry e = collapseR.rules.get(17);
				entropy[antX][antY].add(e);
				putTile(antX, antY, e.x, e.y, 0, 0);
				antX = Math.max(Math.min(antX+dx, MAP_WIDTH-1), 0);
				antY = Math.max(Math.min(antY+dy, MAP_HEIGHT-1), 0);
//				System.out.println(Math.sqrt((antX-DEMON_POS[0])*(antX-DEMON_POS[0])+(antY-DEMON_POS[1])*(antY-DEMON_POS[1])));
				if((antX-DEMON_POS[0])*(antX-DEMON_POS[0])+(antY-DEMON_POS[1])*(antY-DEMON_POS[1]) < 25)
					break;
			}
		}
		for(int i = -1+ANGEL_POS[0]; i < 1+ANGEL_POS[0]; i++) {
			for(int l = -1+ANGEL_POS[1]; l < 1+ANGEL_POS[1]; l++) {
				collapsed[i][l] = true;
				entropy[i][l].clear();
				entry e = collapseR.rules.get(47);
				entropy[i][l].add(e);
				putTile(i, l, e.x, e.y, 0, 0);
			}
		}
		for(int i = -1+DEMON_POS[0]; i < 1+DEMON_POS[0]; i++) {
			for(int l = -1+DEMON_POS[1]; l < 1+DEMON_POS[1]; l++) {
				collapsed[i][l] = true;
				entropy[i][l].clear();
				entry e = collapseR.rules.get(17);
				entropy[i][l].add(e);
				putTile(i, l, e.x, e.y, 0, 0);
			}
		}
		//WFC
		{
			int lx = 0;
			int ly = 0;
			int c = 0;
			for(int a = 0; a < MAP_WIDTH*MAP_HEIGHT; a++) {
				List<int[]> possible = new ArrayList<int[]>();
				int e = Integer.MAX_VALUE;
				for(int i = 0; i < MAP_WIDTH; i++) {
					for(int l = 0; l < MAP_HEIGHT; l++) {
						if(!collapsed[i][l]) {
							if(entropy[i][l].size() == e)
								possible.add(new int[] {i, l});
							else if(entropy[i][l].size() < e) {
								possible.clear();
								possible.add(new int[] {i, l});
								e = entropy[i][l].size();
							}
						}
					}
				}
				if(possible.size() == 0)
					break;
				int[] u = possible.get(Main.random.nextInt(possible.size()));
				List<entry> valid = entropy[u[0]][u[1]];
				lx = u[0];
				ly = u[1];
				collapsed[lx][ly] = true;
				boolean corrected = false;
				if(valid.size() != 0) {
					entry sel = WaveFunctionCollapse.pickRandom(entropy[lx][ly]);
					entropy[lx][ly].clear();
					entropy[lx][ly].add(sel);
					putTile(lx, ly, sel.x, sel.y, 0, 0);
				}
				else {
					if(c < MAX_CORRECTIONS) {
						for(int i = lx-CORRECTION_SIZE; i < lx+CORRECTION_SIZE; i++) {
							for(int l = ly-CORRECTION_SIZE; l < ly+CORRECTION_SIZE; l++) {
								entropy[a(i, MAP_WIDTH)][a(l, MAP_HEIGHT)] = new ArrayList<entry>();
								entropy[a(i, MAP_WIDTH)][a(l, MAP_HEIGHT)].addAll(collapseR.rules);
								collapsed[a(i, MAP_WIDTH)][a(l, MAP_HEIGHT)] = false;
								a -= CORRECTION_SIZE*CORRECTION_SIZE;
								int ax = a(i, MAP_WIDTH);
								int ay = a(l-1, MAP_HEIGHT);
								int bx = a(i-1, MAP_WIDTH);
								int by = a(l, MAP_HEIGHT);
								int cx = a(i, MAP_WIDTH);
								int cy = a(l+1, MAP_HEIGHT);
								int dx = a(i+1, MAP_WIDTH);
								int dy = a(l, MAP_HEIGHT);
								collapseR.getValid(
										!collapsed[ax][ay] || entropy[ax][ay].size() != 1 ? null : entropy[ax][ay].get(0),
										!collapsed[bx][by] || entropy[bx][by].size() != 1 ? null : entropy[bx][by].get(0),
										!collapsed[cx][cy] || entropy[cx][cy].size() != 1 ? null : entropy[cx][cy].get(0),
										!collapsed[dx][dy] || entropy[dx][dy].size() != 1 ? null : entropy[dx][dy].get(0),
										entropy[a(i, MAP_WIDTH)][a(l, MAP_HEIGHT)]);
							}
						}
						c++;
						corrected = true;
					}
				}
				if(!corrected)
					for(int i = lx-2; i < lx+2; i++) {
						for(int l = ly-2; l < ly+2; l++) {
							if(collapsed[a(i, MAP_WIDTH)][a(l, MAP_HEIGHT)])
								continue;
							int ax = a(i, MAP_WIDTH);
							int ay = a(l-1, MAP_HEIGHT);
							int bx = a(i-1, MAP_WIDTH);
							int by = a(l, MAP_HEIGHT);
							int cx = a(i, MAP_WIDTH);
							int cy = a(l+1, MAP_HEIGHT);
							int dx = a(i+1, MAP_WIDTH);
							int dy = a(l, MAP_HEIGHT);
							collapseR.getValid(
									!collapsed[ax][ay] || entropy[ax][ay].size() != 1 ? null : entropy[ax][ay].get(0),
									!collapsed[bx][by] || entropy[bx][by].size() != 1 ? null : entropy[bx][by].get(0),
									!collapsed[cx][cy] || entropy[cx][cy].size() != 1 ? null : entropy[cx][cy].get(0),
									!collapsed[dx][dy] || entropy[dx][dy].size() != 1 ? null : entropy[dx][dy].get(0),
									entropy[a(i, MAP_WIDTH)][a(l, MAP_HEIGHT)]);
						}
					}
			}
		}
		updateMap();
	}
	public boolean validPosition(float x, float y, float w, float h) {
		for(float a = x-Math.abs(w/2); a < x+Math.abs(w/2); a+= 1/32f) {
			for(float b = y-Math.abs(h/2); b < y+Math.abs(h/2); b+= 1/32f) {
				if(!validPoint(a, b))
					return false;
			}
		}
		return true;
	}
	public boolean validPoint(float x, float y) {
		if(a(x, MAP_WIDTH) != x || a(-y, MAP_HEIGHT) != -y)
			return false;
		int tileX = (int) x;
		int tileY = (int) -y;
		int[] tileM = getTile(tileX, tileY);
		int tileTX = (int) ((tileM[0]+x%1)*32);
		int tileTY = (int) ((tileM[1]+(-y)%1)*32);
		int r = height.getData()[tileTX][tileTY][0];
		return r == 127;
	}
	static int a(int a, int b) {
		return a < 0 ? a+b : (a%b);
	}
	static float a(float a, float b) {
		return a < 0 ? a+b : (a%b);
	}
	
	public void putTile(int x, int y, int a, int b, int c, int d) {
		int off = (x+y*MAP_WIDTH)*4;
		data.position(0);
		data.limit(data.capacity());
		data.put(off+0, (byte) a);
		data.put(off+1, (byte) b);
		data.put(off+2, (byte) c);
		data.put(off+3, (byte) d);
	}
	public int[] getTile(int x, int y) {
		int off = (x+y*MAP_WIDTH)*4;
		data.position(0);
		data.limit(data.capacity());
		return new int[] {data.get(off+0), data.get(off+1), data.get(off+2), data.get(off+3)};
	}
	
	public void fillTile(int sx, int sy, int width, int height, int a, int b, int c, int d) {
		for(int x = sx; x < sx+width; x++) {
			for(int y = sy; y < sy+height; y++) {
				putTile(x, y, a, b, c, d);
			}
		}
	}
	
	public void updateMap() {
		tileMap.upload(data, MAP_WIDTH, MAP_HEIGHT, GL33.GL_RGBA);
	}
	
	public void draw(int width, int height) {
		
		
		
		this.shader.bind();
		this.shader.setUniformInt("sampler1", 1);
		this.shader.setUniformVec2("offset", offse);
		this.shader.setUniformVec2("scale", scale.x/width*MAP_WIDTH, scale.y/height*MAP_HEIGHT);
		this.shader.setUniformVec2("setSize", SETTILESIZE);
		this.shader.setUniformVec2("mapSize", MAP_WIDTH, MAP_HEIGHT);
		this.shader.setUniformVec2("background", 4, 0);
		this.shader.setUniformVec4("tint", tint);
		tileMap.bind(1);
		this.shader.render(fullsq, tileSet[CURRENTSET]);
	}
	
	public void free() {
		shader.free();
		fullsq.free();
		tileMap.free();
		for(Texture t : tileSet)
			t.free();
		MemoryUtil.memFree(data);
	}

	public void set(BufferedImage map, BufferedImage set) {
		if(set != null)
			this.tileSet[0].upload(set);
		
		MemoryUtil.memFree(data);
		data = MemoryUtil.memCalloc(map.getWidth()*map.getHeight()*4);
		byte[] b = map.toByteArray(4);
		data.put(0, b);
		this.MAP_WIDTH = map.getWidth();
		this.MAP_HEIGHT = map.getHeight();
		SETTILESIZE.set(set.getWidth()/32, set.getHeight()/32);
		updateMap();
	}

}
