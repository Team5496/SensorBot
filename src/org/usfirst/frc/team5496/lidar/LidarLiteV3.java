package org.usfirst.frc.team5496.lidar;

import java.util.TimerTask;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.I2C.Port;

public class LidarLiteV3 implements ILidarLiteV3 {
	
	private I2C i2c;
	private byte[] distance;
	private byte[] velocity;
	private java.util.Timer updater;
	
	private final int LIDAR_ADDR = 0x62;
	
	private final int LIDAR_CONFIG_REGISTER = 0x00;
	private final int LIDAR_VELOCITY_REGISTER = 0x04;
	private final int LIDAR_DISTANCE_REGISTER = 0x8f;

	public LidarLiteV3() {
		i2c = new I2C(Port.kMXP, LIDAR_ADDR);
		distance = new byte[2]; // The distance is a 16-bit (2 byte) number
		velocity = new byte[1]; // The velocity is an 8-bit (1 byte) number
		updater = new java.util.Timer();
	}

	/**
	 * Return distance in centimeters
	 * 
	 * @return distance in centimeters
	 */
	public int getDistanceCentimeters() {
		/* Take the first distance byte and shift it 8 bits to the right so the two bytes line up like this:
		 * 16            8             0
		 * | Distance[0] | <<<<<<<<<<< |
		 * |             | Distance[1] |
		 * 
		 * Then OR them together so you get the full 16-bit distance like this:
		 * 16            8             0
		 * | Distance[0] | Distance[1] |
		*/
		return ((distance[0] << 8) | distance[1]);
	}

	/**
	 * Return distance in inches
	 * 
	 * @return distance in inches
	 */
	public double getDistanceInches() {
		return (double) getDistanceCentimeters() * 0.393701;
	}

	/**
	 * Return the current velocity of the LIDAR
	 * 
	 * @return the current velocity of the LIDAR
	 */
	public int getVelocity() {
		return velocity[0];
	}
	
	/**
	 * Start 10Hz polling of LIDAR sensor, in a background task. Only allow 10 Hz.
	 * polling at the moment.
	 */
	public void start() {
		updater.scheduleAtFixedRate(new LIDARUpdater(), 0, 100); // Start updates every 100 milliseconds
	}

	/**
	 * Stop the background sensor-polling task.
	 */
	public void stop() {
		// Stop and reinitialize the update timer
		updater.cancel();
		updater = new java.util.Timer();
	}

	/**
	 * Read from the sensor and update the internal "distance" variable with the
	 * result.
	 */
	private void update() {
		i2c.write(LIDAR_CONFIG_REGISTER, 0x04); // Read. 0x04 = WITH receiver bias correction, 0x05 = WITHOUT
		Timer.delay(0.04); // Who knew measurements actually took time to make?
		
		// Read the distance and velocity from their respective registers
		i2c.read(LIDAR_DISTANCE_REGISTER, 2, distance);
		i2c.read(LIDAR_VELOCITY_REGISTER, 1, velocity);
		
		Timer.delay(0.005); // Lets not give the LIDAR a mental breakdown
	}

	/**
	 * Timer task to keep distance updated
	 *
	 */
	private class LIDARUpdater extends TimerTask {
		
		// This runs asynchronously from the rest of the code and calls update every 100 milliseconds
		public void run() {
			while (true) {
				update();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace(); // If for some reason this loop is interrupted then print the stack trace
				}
			}
		}
	}
}
