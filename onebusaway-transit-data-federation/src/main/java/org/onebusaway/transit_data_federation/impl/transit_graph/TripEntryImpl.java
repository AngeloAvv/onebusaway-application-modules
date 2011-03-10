package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public class TripEntryImpl implements TripEntry, Serializable {

  private static final long serialVersionUID = 5L;

  private AgencyAndId _id;

  private AgencyAndId _routeId;

  private AgencyAndId _routeCollectionId;
  
  private String _directionId;

  private BlockEntryImpl _block;

  private LocalizedServiceId _serviceId;

  private AgencyAndId _shapeId;

  private List<StopTimeEntry> _stopTimes;

  private double _totalTripDistance;

  public void setId(AgencyAndId id) {
    _id = id;
  }

  public void setRouteId(AgencyAndId routeId) {
    _routeId = routeId;
  }

  public void setRouteCollectionId(AgencyAndId routeCollectionId) {
    _routeCollectionId = routeCollectionId;
  }
  
  public void setDirectionId(String directionId) {
    _directionId = directionId;
  }

  public void setBlock(BlockEntryImpl block) {
    _block = block;
  }

  public void setServiceId(LocalizedServiceId serviceId) {
    _serviceId = serviceId;
  }

  public void setShapeId(AgencyAndId shapeId) {
    _shapeId = shapeId;
  }

  public void setStopTimes(List<StopTimeEntry> stopTimes) {
    _stopTimes = stopTimes;
  }

  public void setTotalTripDistance(double totalTripDistance) {
    _totalTripDistance = totalTripDistance;
  }

  /****
   * {@link TripEntry} Interface
   ****/

  @Override
  public AgencyAndId getId() {
    return _id;
  }

  @Override
  public AgencyAndId getRouteId() {
    return _routeId;
  }

  @Override
  public AgencyAndId getRouteCollectionId() {
    return _routeCollectionId;
  }
  
  @Override
  public String getDirectionId() {
    return _directionId;
  }

  @Override
  public BlockEntryImpl getBlock() {
    return _block;
  }

  @Override
  public LocalizedServiceId getServiceId() {
    return _serviceId;
  }

  @Override
  public AgencyAndId getShapeId() {
    return _shapeId;
  }

  @Override
  public List<StopTimeEntry> getStopTimes() {
    return _stopTimes;
  }

  @Override
  public double getTotalTripDistance() {
    return _totalTripDistance;
  }

  @Override
  public String toString() {
    return "Trip(" + _id + ")";
  }

  /****
   * Serialization
   ****/

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    TransitGraphImpl.handleTripEntryRead(this);
  }
}
