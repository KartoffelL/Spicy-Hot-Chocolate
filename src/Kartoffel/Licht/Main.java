package Kartoffel.Licht;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.joml.SimplexNoise;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GLCapabilities;

import Kartoffel.Licht.JGL.JGLComponent;
import Kartoffel.Licht.JGL.JGLFrame;
import Kartoffel.Licht.JGL.Components.JGLBackground;
import Kartoffel.Licht.JGL.Components.JGLButton;
import Kartoffel.Licht.JGL.Components.JGLSquare;
import Kartoffel.Licht.JGL.Components.JGLTextField;
import Kartoffel.Licht.Java.Color;
import Kartoffel.Licht.Media.Audio;
import Kartoffel.Licht.Media.Source;
import Kartoffel.Licht.Rendering.GraphicWindow;
import Kartoffel.Licht.Rendering.Text.GlobalFont;

public class Main {
	
	public static Random random = new Random();
	
	public static final int VIEW_DISTANCE = 13;
	
	
	public static volatile boolean RUNNING = true;
	public static volatile boolean PAUSED = true;
	public static volatile boolean STARTED = false;
	public static volatile int exit = 2;
	public static Thread tick;
	public static Thread render;
	public static GraphicWindow window;
	public static EntityManager[] entityManager;
	public static TileManager manager;
	public static List<Entity> entities = new CopyOnWriteArrayList<Entity>();
	
	public static Entity player;
	public static Entity angus;
	public static Entity demon;
	
	public static JGLFrame frame;
	
	public static Source main;
	public static int beebSound;
	
	public static int[] ITEMS = new int[5];
	
	public static int currentItem = -1;
	public static float SANITY = 100;
	
	public static JGLTextField text;
	public static JGLSquare square;
	
	
	public static long cooldown = 0;
	public static int COOLDOWNDUR = 200;
	
	public static long DAYTIMEI = 0;
	public static double DAYLENGTH = 60*2;
	public static long getTime() {
		return System.currentTimeMillis()-DAYTIMEI;
	}
	public static double getTimeOfDay() {
		return Math.cos((System.currentTimeMillis()-DAYTIMEI)/1000.0/DAYLENGTH*Math.PI*2);
	}
	
	
	public static void main(String[] args) {
		//Sound
		Audio.init();
		int music1 = Audio.loadSound(Main.class.getClassLoader().getResourceAsStream("mainMenu.wav"));
		beebSound = Audio.loadSound(Main.class.getClassLoader().getResourceAsStream("beeb.wav"));
		main = new Source();
		
		
		//
		window = new GraphicWindow("SHC");
		window.listeners.add(new GLFWKeyCallback() {
			
			@Override
			public void invoke(long w, int key, int scancode, int action, int mods) {
				if(key == GLFW.GLFW_KEY_F11 && action == 1)
					window.toggleFullscreen();
			}
		});
		window.setAspectRatio(4, 3);
		window.setIcon(FileLoader.loadImage("icon.png"));
		manager = new TileManager(100, 100);
		entityManager = new EntityManager[] {
				new EntityManager(manager, "mage.png", 8, 2),
				new EntityManager(manager, "angus.png", 4, 1),
				new EntityManager(manager, "snummonerer.png", 1, 1),
				new EntityManager(manager, "Particles.png", 8, 13),
				new EntityManager(manager, "entitiees.png", 16, 16),
				new EntityManager(manager, "items.png", 10, 7),
				new EntityManager(manager, "holysaurs.png", 7, 2)};
		
		frame = new JGLFrame(window);
		GlobalFont.init();
		{
			frame.getManager().createUI("MainMenu");
			List<JGLComponent> c = frame.getManager().getUIList("MainMenu");
			c.add(new JGLBackground(FileLoader.loadImage("pause.png")));
			JGLButton button = new JGLButton("START") {
				@Override
				protected void onActionEvent() {
					frame.getManager().loadUI("MainMenu2");
					main.stop();
				}
			};
			c.add(button);
			button.setBindings(0, 0);
			button.setBounds(-250, -50, 500, 100);
			button.getText().getText().setSize(0.5f);
		}
		{
			frame.getManager().createUI("MainMenu2");
			List<JGLComponent> c = frame.getManager().getUIList("MainMenu2");
			c.add(new JGLBackground(FileLoader.loadImage("tutor.png")));
			JGLButton button = new JGLButton("START") {
				@Override
				protected void onActionEvent() {
					frame.getManager().loadDefaultUI();
					PAUSED = false;
					STARTED = true;
					DAYTIMEI = System.currentTimeMillis();
				}
			};
			c.add(button);
			button.setBindings(-1, 1);
			button.setBounds(0, -40, 250, 40);
			button.getText().getText().setSize(0.5f);
		}
		{
			frame.getManager().createUI("Death");
			List<JGLComponent> c = frame.getManager().getUIList("Death");
			c.add(new JGLBackground(FileLoader.loadImage("death"+Main.random.nextInt(1)+".png")));
			text = new JGLTextField("");
			text.setBindings(0, 0);
			text.setBounds(-500, -200, 1000, 400);
			text.getText().setSize(0.15f);
			square = new JGLSquare(Color.BLACK);
			c.add(square);
			c.add(text);
		}
		{
			frame.getManager().createUI("transition");
			List<JGLComponent> c = frame.getManager().getUIList("transition");
			c.add(new JGLBackground(FileLoader.loadImage("pause.png")));
		}
		{
			frame.getManager().createUI("Win");
			List<JGLComponent> c = frame.getManager().getUIList("Win");
			c.add(new JGLBackground(FileLoader.loadImage("endingA.png")));
		}
		{
			frame.getManager().createUI("Win2");
			List<JGLComponent> c = frame.getManager().getUIList("Win2");
			c.add(new JGLBackground(FileLoader.loadImage("endingB.png")));
		}
		{
			frame.getManager().createUI("PAUSED");
			List<JGLComponent> c = frame.getManager().getUIList("PAUSED");
			c.add(new JGLBackground(FileLoader.loadImage("pause.png")));
			JGLButton button = new JGLButton("RESUME") {
				@Override
				protected void onActionEvent() {
					frame.getManager().loadDefaultUI();
					PAUSED = false;
				}
			};
			c.add(button);
			button.setBindings(0, 0);
			button.setBounds(-250, -150, 500, 100);
			button.getText().getText().setSize(0.5f);
			JGLButton button2 = new JGLButton("QUIT") {
				@Override
				protected void onActionEvent() {
					window.requestClose();
				}
			};
			c.add(button2);
			button2.setBindings(0, 0);
			button2.setBounds(-250, 0, 500, 100);
			button2.getText().getText().setSize(0.5f);
		}
		//Entities
		entities.add(angus = new Entity(entityManager[1], manager.ANGEL_POS[0], -manager.ANGEL_POS[1], 2, 2, 0, 0) {
			@Override
			public void update(double delta) {
				if(player.pos.distance(pos) < 3)
					SANITY = (float) Math.min(100, delta*5+SANITY);
				super.update(delta);
			}
		});
		angus.setAnimationState(0, 0, 4);
		angus.animationDelay = 100;
		entities.add(demon = new Entity(entityManager[2], manager.DEMON_POS[0], -manager.DEMON_POS[1], 2, 2, 0, 0) {
			@Override
			public void update(double delta) {
				if(player.pos.distance(pos) < 3) {
					if(currentItem != -1) {
						if(manager.CURRENTSET == 4) {
							try {
								frame.getManager().loadUI("Win");
								Thread.sleep(1350);
								frame.getManager().loadUI("Win2");
								Thread.sleep(500);
								frame.getManager().loadUI("Death");
								square.setBounds(0, 0, window.getWidth(), window.getHeight());
								Thread.sleep(750);
								text.setColor(Color.WHITE);
								main.play(beebSound);
								String t = "You met your friend.";
								for(int i = 0; i < t.length(); i++) {
									text.setText(t.substring(0, i));
									Thread.sleep(60+Main.random.nextInt(30));
								}
								Thread.sleep(666);
								t = "You wanted it. He knew it.";
								for(int i = 0; i < t.length(); i++) {
									text.setText(t.substring(0, i));
									Thread.sleep(60+Main.random.nextInt(30));
								}
								Thread.sleep(666);
								t = "You both had an pleasant evening, ";
								for(int i = 0; i < t.length(); i++) {
									text.setText(t.substring(0, i));
									Thread.sleep(60+Main.random.nextInt(30));
								}
								Thread.sleep(100);
								t = "sipping away on his famous";
								for(int i = 0; i < t.length(); i++) {
									text.setText(t.substring(0, i));
									Thread.sleep(60+Main.random.nextInt(30));
								}
								t = "Spicy Hot Chocolate";
								for(int i = 0; i < t.length(); i++) {
									text.setText(t.substring(0, i));
									Thread.sleep(150+Main.random.nextInt(70));
								}
								Thread.sleep(666);
								text.setVisible(false);
								square.setVisible(false);
								Thread.sleep(600);
								
							} catch (InterruptedException e) {}
							System.exit(0);
						}
						main.play(beebSound);
						currentItem = -1;
						COOLDOWNDUR -= 100;
						frame.getManager().loadUI("transition");
						manager.CURRENTSET++;
						try {
							Thread.sleep(750);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						frame.getManager().loadDefaultUI();
						player.pos.set(manager.ANGEL_POS[0], -manager.ANGEL_POS[1]);
						SANITY += 25;
					}
				}
				super.update(delta);
			}
		});
		
		//On top of everything
		entities.add(player = new Entity(entityManager[0], 0, 0, 2, 2, 0, 0));
		player.pos.set(manager.ANGEL_POS[0], -manager.ANGEL_POS[1]);
		player.physics = true;
		
		entities.add(new Entity(entityManager[6], 0, 0, 1, 1, 0, 1) {
			@Override
			public void update(double delta) {
				pos.set(player.pos);
				pos.y += 1;
				pos.x -= .5f;
				tileX = (int) (SANITY/20);
				super.update(delta);
			}
		});
		entities.add(new Entity(entityManager[5], 0, 0, 1, 1, 0, 1) {
			@Override
			public void update(double delta) {
				pos.set(player.pos);
				pos.y += 2;
				pos.x += .5f;
				tileY = currentItem;
				if(currentItem == 0)
					animationSize = 9;
				else
					animationSize = 6;
				if(currentItem == -1)
					pos.x = -1000;
				super.update(delta);
			}
		});
		
		frame.getManager().loadUI("MainMenu");
		////////////////////////////
		GLFW.glfwMakeContextCurrent(0);
		GLCapabilities cap = GL.getCapabilities();
		GL.setCapabilities(null);
		
		render = new Thread(()->render(cap));
		tick = new Thread(()->tick());
		tick.start();
		render.start();
		
		window.setVisible(true);
		main.play(music1);
		main.setLooping(true);
		float delta = 1;
		long tim = System.nanoTime();
		while(!window.WindowShouldClose()) {
			GraphicWindow.doPollEvents();
			
			
			player.setAnimationState(0, 1, 8);
			if(STARTED) {
				if(!PAUSED) {
					if(window.getCallback_Key().isKeyDown("W")) {
						player.moveSepAxis(0, delta*5);
					}
					if(window.getCallback_Key().isKeyDown("A")) {
						player.moveSepAxis(-delta*5, 0);
						player.setAnimationState(0, 0, 7);
						player.size.x = -2;
					}
					if(window.getCallback_Key().isKeyDown("S")) {
						player.moveSepAxis(0, -delta*5);
					}
					if(window.getCallback_Key().isKeyDown("D")) {
						player.moveSepAxis(delta*5, 0);
						player.setAnimationState(0, 0, 7);
						player.size.x = 2;
					}
					if(window.getCallback_Key().isMouseButtonDown(0) && System.currentTimeMillis() > cooldown) {
						float dx = ((window.getCallback_Cursor().getX()-window.getWidth()/2)*2-1);
						float dy = ((window.getCallback_Cursor().getY()-window.getHeight()/2)*2-1);
						float a = (float) Math.sqrt(dx*dx+dy*dy)/24;
						entities.add(new Projectile(entities, entityManager[3], player.pos.x, player.pos.y, 1, 1, 0, 2, 8, 0, 2, dx/a, -dy/a));
						cooldown = System.currentTimeMillis()+100;
					}
					
					{
						@SuppressWarnings("resource")
						float m = (float) window.getCallback_CursorScroll().MY;
						manager.scale.mul((float)Math.exp(-m/20));
					}
				}
				if(window.getCallback_Key().isKeyDown("ESCAPE")) {
					PAUSED = true;
					frame.getManager().loadUI("PAUSED");
				}
				{
					delta = (System.nanoTime()-tim)/1000000000.0f;
					tim = System.nanoTime();
				}
			}
		}
		RUNNING = false;
		while(render.isInterrupted() && tick.isInterrupted())
			if(exit == 0)
				break;
		window.free();
	}

	public static void render(GLCapabilities cap) {
		GL.setCapabilities(cap);
		window.bindGLFW();
		while(RUNNING) {
			GL33.glViewport(0, 0, window.getWidth(), window.getHeight());
			GL33.glDisable(GL33.GL_DEPTH_TEST);
			GL33.glBlendFuncSeparate(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA, GL33.GL_ONE, GL33.GL_ONE);
			manager.draw(window.getWidth(), window.getHeight());
			try {
				for(int i = 0; i < entities.size(); i++) {
					Entity e = entities.get(i);
					if(e != null)
						e.entityManager.draw(window.getWidth(), window.getHeight(), e);
				}
			} catch (Exception e) {}
			frame.repaint();
			window.updateWindow(true);
		}
		manager.free();
		exit--; 
	}
	
	public static void tick() {
		long nt = System.nanoTime();
		double delta = 1;
		while(RUNNING) {
			delta = (System.nanoTime()-nt)/1000000000.0;
			nt = System.nanoTime();
			manager.scale.set((window.getWidth()/2.0+window.getHeight()/2.0)/VIEW_DISTANCE);
			manager.offse.set(player.pos.x-window.getWidth()/manager.scale.x/2, -player.pos.y-window.getHeight()/manager.scale.y/2);
			try {
				for(int i = 0; i < entities.size(); i++) {
					Entity e = entities.get(i);
					if(e != null)
						e.update(delta);
				}
			} catch(Exception e) {};
			
			double brigh = Math.max(getTimeOfDay(), 0);
			manager.tint.set(Math.sqrt(brigh)*(brigh*.2+.7)*.9+.1, brigh*.9+.1, brigh*.85+.15, 1);
			if(STARTED && !PAUSED) {
				if(getTimeOfDay() < -0.2 || true) { //Night
					if(Main.random.nextDouble(2.0/(1+manager.CURRENTSET)) < delta) {
						Entity e = new Entity(entityManager[4], player.pos.x, player.pos.y+5, 1, 1, 0, 11) {
							Vector2f dir = new Vector2f();
							@Override
							public void update(double delta) {
								this.animationSize = 2;
								animationDelay = 100;
	//							this.
		//						if(getTimeOfDay() > 0.2)
		//							entities.remove(this);
		//						if(getTimeOfDay() > -0.2)
		//							size.sub((float)(delta*0.2), (float)(delta*0.2));
								
								if(this.pos.distance(player.pos) < 1) {
									this.animationSize = 6;
									if(animation == 5) {
										entities.remove(this);
										SANITY -= 16;
									}
								}
								else {
									player.pos.sub(pos, dir).normalize((float)delta*5);
									dir.mul(SimplexNoise.noise(pos.x, pos.y)*.3f+.7f, SimplexNoise.noise(pos.y, pos.x)*.3f+.7f);
									pos.add(dir);
								}
								super.update(delta);
							}
							@Override
							public boolean hit(int level) {
								if(Main.random.nextInt(12) != 0)
									return true;
								entities.remove(this);
								Entity item = new Entity(Main.entityManager[5], pos.x, pos.y, 0.5f, 0.5f, 0, Main.random.nextInt(7));
								if(item.tileY == 0)
									item.animationSize = 9;
								else
									item.animationSize = 6;
								entities.add(item);
								return true;
							}
						};
						do {
							e.pos.x = player.pos.x+Main.random.nextFloat(10, 15)*Math.signum(Main.random.nextFloat(2)-1);
							e.pos.y = player.pos.y+Main.random.nextFloat(10, 15)*Math.signum(Main.random.nextFloat(2)-1);
						} while(!manager.validPosition(e.pos.x, e.pos.y, e.size.x, e.size.y));
						entities.add(e);
					}
					
				}
				if(getTimeOfDay() < -0.2 || true) { //Night
					if(Main.random.nextDouble(2.0/(1+manager.CURRENTSET)) < delta) {
						Entity e = new Entity(entityManager[4], player.pos.x, player.pos.y+5, 1, 1, 1, 2) {
							Vector2f dir = new Vector2f();
							@Override
							public void update(double delta) {
								this.animationSize = 2;
								animationDelay = 150;
		//						if(getTimeOfDay() > 0.2)
		//							entities.remove(this);
		//						if(getTimeOfDay() > -0.2)
		//							size.sub((float)(delta*0.2), (float)(delta*0.2));
								
								if(this.pos.distance(player.pos) < 4.4) {
									this.animationSize = 4;
									if(animation == 3) {
										animationDelay = Integer.MAX_VALUE;
										SANITY -= delta*2;
									}
								}
								else {
									player.pos.sub(pos, dir).normalize((float)delta*5);
									dir.mul(SimplexNoise.noise(pos.x, pos.y)*.3f+.7f, SimplexNoise.noise(pos.y, pos.x)*.3f+.7f);
									pos.add(dir);
								}
								super.update(delta);
							}
							@Override
							public boolean hit(int level) {
								if(Main.random.nextInt(12) != 0)
									return true;
								entities.remove(this);
								Entity item = new Entity(Main.entityManager[5], pos.x, pos.y, 0.5f, 0.5f, 0, Main.random.nextInt(7)) {
									@Override
									public void update(double delta) {
										if(player.pos.distance(pos) < 0.75) {
											entities.remove(this);
											currentItem = tileY;
										}
										super.update(delta);
									}
								};
								if(item.tileY == 0)
									item.animationSize = 9;
								else
									item.animationSize = 6;
								entities.add(item);
								return true;
							}
						};
						do {
							e.pos.x = player.pos.x+Main.random.nextFloat(10, 15)*Math.signum(Main.random.nextFloat(2)-1);
							e.pos.y = player.pos.y+Main.random.nextFloat(10, 15)*Math.signum(Main.random.nextFloat(2)-1);
						} while(!manager.validPosition(e.pos.x, e.pos.y, e.size.x, e.size.y));
						entities.add(e);
					}
					
				}
				if(Main.random.nextDouble(2) < delta) {
					Entity bird = new Entity(entityManager[4], player.pos.x+25, player.pos.y+Main.random.nextFloat(10)-5, 1, 1, 3, 0) {
						long life = System.currentTimeMillis()+10000;
						@Override
						public void update(double delta) {
							if(System.currentTimeMillis() > life)
								entities.remove(this);
							animationSize = 3;
							pos.x -= delta*10;
							super.update(delta);
						}
					};
					entities.add(bird);
				}
				if(SANITY < 0) {
					try {
						frame.getManager().loadUI("Death");
						square.setBounds(0, 0, window.getWidth(), window.getHeight());
						Thread.sleep(1500);
						text.setColor(Color.WHITE);
						main.play(beebSound);
						
						String t = "You succumbed.";
						for(int i = 0; i < t.length(); i++) {
							text.setText(t.substring(0, i));
							Thread.sleep(60+Main.random.nextInt(30));
						}
						Thread.sleep(666);
						t = "No spicy hot chocolate for you then";
						for(int i = 0; i < t.length(); i++) {
							text.setText(t.substring(0, i));
							Thread.sleep(60+Main.random.nextInt(30));
						}
						Thread.sleep(300);
						text.setVisible(false);
						Thread.sleep(500);
						square.setVisible(false);
						Thread.sleep(2700);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.exit(1);
				}
			}
		}
		
		exit--;
	}
	
	
	
}
