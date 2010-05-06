package org.onebusaway.webapp.gwt.oba_application.model;

import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.webapp.gwt.common.model.ModelEventSink;

import com.google.gwt.maps.client.geom.LatLng;

public class LocationQueryModel {

  private ModelEventSink<LocationQueryModel> _events;

  private LatLng _location;

  private String _locationQuery;

  private OneBusAwayConstraintsBean _constraints;

  public void setEventSink(ModelEventSink<LocationQueryModel> events) {
    _events = events;
  }

  public String getLocationQuery() {
    return _locationQuery;
  }

  public boolean hasLocation() {
    return _location != null;
  }

  public LatLng getLocation() {
    return _location;
  }

  public OneBusAwayConstraintsBean getConstraints() {
    return _constraints;
  }

  public void setQuery(String locationQuery, LatLng location,
      OneBusAwayConstraintsBean constraints) {
    _locationQuery = locationQuery;
    _location = location;
    _constraints = constraints;
    fireModelChanged();
  }

  public void setQueryLocation(LatLng location) {
    _location = location;
    fireModelChanged();
  }

  protected void fireModelChanged() {
    if (_events != null)
      _events.fireModelChange(this);
  }
}
