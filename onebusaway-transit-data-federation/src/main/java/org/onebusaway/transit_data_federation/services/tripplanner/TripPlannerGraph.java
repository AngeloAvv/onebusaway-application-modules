package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public interface TripPlannerGraph {
  
  public Iterable<StopEntry> getAllStops();
 
  public Iterable<TripEntry> getAllTrips();
  
  public Iterable<BlockEntry> getAllBlocks();
  
  public List<StopEntry> getStopsByLocation(CoordinateBounds bounds);
  
  public BlockEntry getBlockEntryForId(AgencyAndId blockId);
  
  public TripEntry getTripEntryForId(AgencyAndId id);
  
  public StopEntry getStopEntryForId(AgencyAndId id);
}