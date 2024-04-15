package Kartoffel.Licht;

import java.util.List;

public class Projectile extends Entity{
	
	List<Entity> pt;
	int anl;
	int tileX;
	int tileY;
	float motionX, motionY;

	public Projectile(List<Entity> particleTarget, EntityManager manager, float x, float y, float w, float h, int tileX, int tileY, int panimationLength, int pTileX, int pTileY, float motionX, float motionY) {
		super(manager, x, y, w, h, tileX, tileY);
		this.pt = particleTarget;
		this.anl = panimationLength;
		this.tileX = tileX;
		this.tileY = tileY;
		this.physics = true;
		this.motionX = motionX;
		this.motionY = motionY;
	}
	
	public long delay = 0;
	@Override
	public void update(double delta) {
		if(System.currentTimeMillis() > delay+60) {
			delay = System.currentTimeMillis()+60;
			Entity e = new Entity(entityManager, pos.x, pos.y, 1, 1, 0, 0) {
				long time = System.currentTimeMillis()+anl*animationDelay;//one second
				@Override
				public void update(double delta) {
					super.update(delta);
					if(System.currentTimeMillis() > time)
						pt.remove(this);
				}
			};
			e.setAnimationState(tileX, tileY, anl);
			pt.add(e);
		}
		boolean hit = false;
		List<Entity> a = List.copyOf(pt);
		for(Entity e : a)
			if(collides(e))
				if(e.hit(anl))
					hit = true;
		
		if(!entityManager.manager.validPosition(pos.x, pos.y, psize.x, psize.y) || hit)
			pt.remove(this);
		this.pos.add((float)(motionX*delta), (float)(motionY*delta));
		super.update(delta);
	}

}
