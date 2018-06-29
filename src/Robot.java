import java.util.Random;

public class Robot {

	private float angle = 0.0f;
	
	private float speed = 0f;
	
	private float elapsed_angle = 0.0f; // synonymous with elapsed distance
	
	private boolean clockwise;
	
	public Robot(float angle, float speed) {
		this.angle = angle;
		this.speed = speed;
		this.randomizeDirection();
	}
	
	public float getAngle() {
		return this.angle;
	}
	
	public void tick() {
		this.angle += clockwise ? speed : -speed;
		elapsed_angle += Math.abs(speed);
		if(this.angle < 0) {
			this.angle = 360;
		} else if(this.angle >= 360) {
			this.angle = 0;
		}
	}
	
	public float getDistance() {
		return this.elapsed_angle;
	}
	
	public boolean isClockwise() {
		return this.clockwise;
	}
	
	public void randomizeDirection() {
		this.elapsed_angle = 0; // reset elapsed distance
		this.clockwise = (new Random()).nextBoolean();
	}
	
	public boolean collided(Robot robot) {
		return Math.abs(angle-robot.angle) <= Math.max(Math.abs(robot.speed), Math.abs(speed));	
	}
	
}
