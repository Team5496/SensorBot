package org.usfirst.frc.team5496.lidar;

public interface ILidarLiteV3 {
	public int getDistanceCentimeters();
	public double getDistanceInches();
	public int getVelocity();
	public void start();
	public void stop();
}