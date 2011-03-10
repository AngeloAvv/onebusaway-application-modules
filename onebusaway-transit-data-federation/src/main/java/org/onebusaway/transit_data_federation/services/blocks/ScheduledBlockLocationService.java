package org.onebusaway.transit_data_federation.services.blocks;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;

/**
 * Methods for retrieving the scheduled location of a vehicle traveling along a
 * block of trips.
 * 
 * @author bdferris
 * @see ScheduledBlockLocation
 */
public interface ScheduledBlockLocationService {

  /**
   * If you request a schedule time that is less than the first arrival time of
   * the block, we will still return a {@link ScheduledBlockLocation}. If the
   * distance along the block of the first stop is greater than zero, we attempt
   * to interpolate where the bus currently is along the block based on the
   * relative velocity between the first two stops in the block.
   * 
   * We return a null location when requesting a schedule time that is beyond
   * the last scheduled stop time for the block.
   * 
   * @param stopTimes
   * @param scheduleTime
   * @return the schedule block position
   */
  public ScheduledBlockLocation getScheduledBlockLocationFromScheduledTime(
      BlockConfigurationEntry blockConfig, int scheduleTime);

  /**
   * Same behavior as
   * {@link #getScheduledBlockLocationFromDistanceAlongBlock(BlockConfigurationEntry, double)}
   * except we take advantage of the fact that we might already have a
   * ScheduledBlockLocation that comes just a bit before the target
   * scheduleTime, which should make lookup faster.
   * 
   * @param previousLocation a scheduled block location that comes right before
   *          the target schedule time
   * @param scheduleTime
   * @return the schedule block position
   */
  public ScheduledBlockLocation getScheduledBlockLocationFromScheduledTime(
      ScheduledBlockLocation previousLocation, int scheduleTime);

  /**
   * 
   * @param stopTimes
   * @param distanceAlongBlock in meters
   * @return the schedule block position, or null if the distance is outside the
   *         range of the block
   */
  public ScheduledBlockLocation getScheduledBlockLocationFromDistanceAlongBlock(
      BlockConfigurationEntry blockConfig, double distanceAlongBlock);

  /**
   * Same behavior as
   * {@link #getScheduledBlockLocationFromDistanceAlongBlock(BlockConfigurationEntry, double)}
   * except we take advantage of the fact that we might already have a
   * ScheduledBlockLocation that comes just a bit before the target
   * scheduleTime, which should make lookup faster.
   * 
   * @param previousLocation a scheduled block location that comes right before
   *          the target schedule time
   * @param scheduleTime
   * @return the schedule block position
   */
  public ScheduledBlockLocation getScheduledBlockLocationFromDistanceAlongBlock(
      ScheduledBlockLocation previousLocation, double distanceAlongBlock);
}