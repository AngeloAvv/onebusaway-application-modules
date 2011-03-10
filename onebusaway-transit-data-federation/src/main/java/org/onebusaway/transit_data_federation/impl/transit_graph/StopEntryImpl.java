package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.tripplanner.StopTransferList;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class StopEntryImpl implements StopEntry, Serializable {

  private static final long serialVersionUID = 2L;

  private final AgencyAndId _id;

  private final double _lat;

  private final double _lon;

  private transient List<BlockStopTimeIndex> _stopTimeIndices = null;

  private transient List<FrequencyBlockStopTimeIndex> _frequencyStopTimeIndices = null;

  private transient StopTransferList _transfers = null;

  private StopIdsWithValuesImpl _prevStopsWithMinTravelTime = new StopIdsWithValuesImpl();

  private StopIdsWithValuesImpl _nextStopsWithMinTravelTime = new StopIdsWithValuesImpl();

  public StopEntryImpl(AgencyAndId id, double lat, double lon) {
    if (id == null)
      throw new IllegalArgumentException("id must not be null");
    _id = id;
    _lat = lat;
    _lon = lon;
  }

  public void addStopTimeIndex(BlockStopTimeIndex stopTimeIndex) {
    if (_stopTimeIndices == null)
      _stopTimeIndices = new ArrayList<BlockStopTimeIndex>();
    _stopTimeIndices.add(stopTimeIndex);
  }

  public List<BlockStopTimeIndex> getStopTimeIndices() {
    if (_stopTimeIndices == null)
      return Collections.emptyList();
    return _stopTimeIndices;
  }

  public void addFrequencyStopTimeIndex(
      FrequencyBlockStopTimeIndex stopTimeIndex) {
    if (_frequencyStopTimeIndices == null)
      _frequencyStopTimeIndices = new ArrayList<FrequencyBlockStopTimeIndex>();
    _frequencyStopTimeIndices.add(stopTimeIndex);
  }

  public List<FrequencyBlockStopTimeIndex> getFrequencyStopTimeIndices() {
    if (_frequencyStopTimeIndices == null)
      return Collections.emptyList();
    return _frequencyStopTimeIndices;
  }

  public StopTransferList getTransfers() {
    return _transfers;
  }

  public void setTransfers(StopTransferList transfers) {
    _transfers = transfers;
  }

  public void addPreviousStopWithMinTravelTime(StopEntry stop, int travelTime) {
    _prevStopsWithMinTravelTime.setMinValue(stop, travelTime);
  }

  public void addNextStopWithMinTravelTime(StopEntry stop, int travelTime) {
    _nextStopsWithMinTravelTime.setMinValue(stop, travelTime);
  }

  /****
   * {@link StopEntry} Interface
   ****/

  @Override
  public AgencyAndId getId() {
    return _id;
  }

  @Override
  public double getStopLat() {
    return _lat;
  }

  @Override
  public double getStopLon() {
    return _lon;
  }

  @Override
  public CoordinatePoint getStopLocation() {
    return new CoordinatePoint(_lat, _lon);
  }

  @Override
  public StopIdsWithValuesImpl getPreviousStopsWithMinTimes() {
    return _prevStopsWithMinTravelTime;
  }

  @Override
  public StopIdsWithValuesImpl getNextStopsWithMinTimes() {
    return _nextStopsWithMinTravelTime;
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopEntryImpl))
      return false;
    StopEntryImpl stop = (StopEntryImpl) obj;
    return _id.equals(stop.getId());
  }

  @Override
  public int hashCode() {
    return _id.hashCode();
  }

  @Override
  public String toString() {
    return "StopEntry(id=" + _id + ")";
  }

  /*****************************************************************************
   * Serialization Support
   ****************************************************************************/

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    TransitGraphImpl.handleStopEntryRead(this);
  }

}
