package Kartoffel.Licht;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import Kartoffel.Licht.Rendering.Model;
import Kartoffel.Licht.Rendering.Shaders.Shader;
import Kartoffel.Licht.Rendering.Shapes.SBox2D;
import Kartoffel.Licht.Rendering.Texture.Texture;

public class EntityManager {
	
	public Vector2f SETTILESIZE = new Vector2f();
	public Shader shader;
	public Texture tileSet;
	public Vector2f offse = new Vector2f(0, 0);
	public Vector2f scale = new Vector2f(100);
	public Matrix4f mat = new Matrix4f();
	
	public Model fullsq;
	public TileManager manager;
	
	public EntityManager(TileManager manager, String image, int stsX, int stsY) {
		this.shader = new Shader(FileLoader.loadText("ent.vs"), FileLoader.loadText("ent.fs"), "Entity Shader");
		tileSet = new Texture(FileLoader.loadImage(image));
		fullsq = new Model(new SBox2D(1, 1));
		fullsq.setFaceCulling(false);
		tileSet.setFiltering(true, true);
		SETTILESIZE.set(stsX, stsY);
		this.offse = manager.offse;
		this.scale = manager.scale;
		this.manager = manager;
	}
	
	
	public void draw(int wwidth, int wheight, Entity e) {
		
		this.shader.bind();
		
		mat.scale(scale.x/wwidth, scale.y/wheight, 1);
		mat.translate(-offse.x*2+e.pos.x*2, offse.y*2+e.pos.y*2, 0);
		int anim = e.animation%e.animationSize;
		this.shader.setUniformVec2("setSize", SETTILESIZE);
		this.shader.setUniformVec2("scale", e.size);
		this.shader.setUniformVec2("tileOffset", e.tileX+(anim % SETTILESIZE.x), e.tileY+(anim/(int)SETTILESIZE.x));
		this.shader.setUniformMatrix4f("matrix", mat);
		this.shader.setUniformVec4("tint", manager.tint);
		this.shader.render(fullsq, tileSet);
		mat.identity();
	}
	
	
	public void free() {
		shader.free();
		fullsq.free();
		tileSet.free();
	}

}
