package Kartoffel.Licht;

import org.joml.Vector2f;

public class Entity {

	public Vector2f pos = new Vector2f();
	public Vector2f size = new Vector2f();
	public Vector2f psize = new Vector2f(0.1f);
	
	public int tileX = 0;
	public int tileY = 0;
	public int animation = 0;
	public int animationSize = 1;
	public int animationDelay = 50;
	
	public long animationTicker = System.currentTimeMillis()+Main.random.nextInt(1000);
	public EntityManager entityManager;
	
	boolean physics = false;
	
	
	public Entity(EntityManager manager, float x, float y, float w, float h, int tileX, int tileY) {
		super();
		this.pos.set(x, y);
		this.size.set(w, h);
		this.tileX = tileX;
		this.tileY = tileY;
		this.entityManager = manager;
	}
	
	public void setAnimationState(int tileX, int tileY, int animationSize) {
		this.tileX = tileX;
		this.tileY = tileY;
		this.animationSize = animationSize;
	}
	
	public void update(double delta) {
		if(animationTicker+animationDelay < System.currentTimeMillis()) {
			animationTicker = System.currentTimeMillis()+animationDelay;
			animation = (animation+1)%animationSize;
		}
		
	}
	
	public boolean hit(int level) {
		return false;
	}
	

	public boolean collides(Entity e) {
		boolean xIntersect = Math.max(pos.x-size.x, e.pos.x-e.size.x) <= Math.min(pos.x+size.x, e.pos.x+e.size.x);
        boolean yIntersect = Math.max(pos.y-size.y, e.pos.y-e.size.y) <= Math.min(pos.y+size.y, e.pos.y+e.size.y);
        return xIntersect && yIntersect;
	}
	
	public void moveSepAxis(float deltaX, float deltaY) {
		if(physics) {
			if(entityManager.manager.validPosition(pos.x, pos.y, psize.x, psize.y)) {
				move(deltaX, 0);
				move(0, deltaY);
			}
			else
				pos.add(deltaX, deltaY);
		}
	}
	
	public void move(float deltaX, float deltaY) {
		boolean p = true;
		pos.add(deltaX, deltaY);
		p = entityManager.manager.validPosition(pos.x, pos.y, psize.x, psize.y);
		pos.sub(deltaX, deltaY);
		if(p)
			pos.add(deltaX, deltaY);
	}
	
	
	

}
