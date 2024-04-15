package Kartoffel.Licht;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.opengl.GL33;

import Kartoffel.Licht.Java.BufferedImage;
import Kartoffel.Licht.Java.ImageIO;
import Kartoffel.Licht.Rendering.GraphicWindow;
import Kartoffel.Licht.Tools.FileDialog;
import Kartoffel.Licht.Tools.ImGuiTools;
import imgui.ImGui;

public class TileEditor {
	
	static TileManager manager;
	static GraphicWindow w;
	static Vector2f current = new Vector2f();

	public static void main(String[] args) {
		w = new GraphicWindow("Tile Editor");
		w.setWidth(500).setHeight(500);
		w.setVisible(true);
		ImGuiTools.ImGui_Init(w);
		w.listeners.add(new GLFWWindowRefreshCallback() {
			
			@Override
			public void invoke(long window) {
				paint();
			}
		});
		manager = new TileManager(32, 32);
		while(!w.WindowShouldClose()) {
			paint();
			GraphicWindow.doPollEvents();
			FileDialog.update();
			if(!ImGuiTools.wantsCapture()) {
				if(w.getCallback_Key().isMouseButtonDown(1)) {
					manager.offse.add((float)w.getCallback_Cursor().getMotionX()/(manager.scale.x), (float)w.getCallback_Cursor().getMotionY()/(manager.scale.y));
				}
				if(w.getCallback_Key().isMouseButtonDown(0)) {
					float x = (float)w.getCallback_Cursor().getX()/manager.scale.x+manager.offse.x;
					float y = (float)w.getCallback_Cursor().getY()/manager.scale.y+manager.offse.y;
					manager.putTile((int)x, (int)y, (int)current.x, (int)current.y, 0, 1);
					manager.updateMap();
				}
				
				@SuppressWarnings("resource")
				float m = (float) w.getCallback_CursorScroll().MY;
				manager.scale.add(-m*5, -m*5);
			}
		}
		System.exit(0);
		
	}
	
	public static void paint() {
		GL33.glViewport(0, 0, w.getWidth(), w.getHeight());
		manager.draw(w.getWidth(), w.getHeight());
		ImGuiTools.ImGui_startDraw();
		ImGui.beginMainMenuBar();
		if(ImGui.menuItem("save")) {
			FileDialog.openSave(new Consumer<String>() {
				
				@Override
				public void accept(String ta) {
					if(!ta.startsWith("O"))
						return;
					String t = ta.split("\\*")[1];
					File f = new File(t);
					FileOutputStream fos = null;
					try {
						f.createNewFile();
						fos = new FileOutputStream(f);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ImageIO.writePNG(fos, manager.data, manager.MAP_WIDTH, manager.MAP_HEIGHT, 4);
				}
			}, "", "map.png", new String[][] {{""}});
		}
		if(ImGui.menuItem("load")) {
			FileDialog.openFolder(new Consumer<String>() {
				
				@Override
				public void accept(String ta) {
					if(!ta.startsWith("O"))
						return;
					String t = ta.split("\\*")[1];
					BufferedImage map = null;
					if(new File(t+"/map.png").exists())
						map = ImageIO.read(new File(t+"/map.png"));
					else
						map = new BufferedImage(8, 8, 4);
					BufferedImage set = ImageIO.read(new File(t+"/set.png"));
					manager.set(map, set);
					System.out.println("Loaded " + set + " " + map);
				}
			}, "C:\\Users\\Anwender\\eclipse-workspace\\Games\\LDJam55Game\\ressource");
		}
		ImGui.endMainMenuBar();
		ImGui.begin("a");
		ImGuiTools.ImGuiVector("Tile", current);
		current.round();
		ImGui.image(manager.tileSet[0].getID(0), 50, 50, current.x/manager.SETTILESIZE.x, current.y/manager.SETTILESIZE.y, (current.x+1)/manager.SETTILESIZE.x, (current.y+1)/manager.SETTILESIZE.y, 1, 1, 1, 1);
		ImGui.image(manager.tileSet[0].getID(0), 500, 500);
		ImGui.end();
		ImGuiTools.ImGui_endDraw();
		w.updateWindow(true);
	}
}
