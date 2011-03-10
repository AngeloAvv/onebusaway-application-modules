package org.onebusaway.api.actions.api.where;

import java.io.IOException;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.impl.MaxCountSupport;
import org.onebusaway.api.impl.SearchBoundsFactory;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.TripDetailsV2Bean;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

public class TripsForLocationController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  private static final double MAX_BOUNDS_RADIUS = 20000.0;

  @Autowired
  private TransitDataService _service;
  
  private SearchBoundsFactory _searchBoundsFactory = new SearchBoundsFactory(MAX_BOUNDS_RADIUS);

  private long _time = 0;

  private MaxCountSupport _maxCount = new MaxCountSupport();

  private boolean _includeTrips = false;

  private boolean _includeSchedules = false;

  public TripsForLocationController() {
    super(V2);
  }

  public void setLat(double lat) {
    _searchBoundsFactory.setLat(lat);
  }

  public void setLon(double lon) {
    _searchBoundsFactory.setLon(lon);
  }
  
  public void setRadius(double radius) {
    _searchBoundsFactory.setRadius(radius);
  }

  public void setLatSpan(double latSpan) {
    _searchBoundsFactory.setLatSpan(latSpan);
  }

  public void setLonSpan(double lonSpan) {
    _searchBoundsFactory.setLonSpan(lonSpan);
  }

  public void setTime(long time) {
    _time = time;
  }

  public void setMaxCount(int maxCount) {
    _maxCount.setMaxCount(maxCount);
  }

  public void setIncludeTrips(boolean includeTrips) {
    _includeTrips = includeTrips;
  }

  public void setIncludeSchedules(boolean includeSchedules) {
    _includeSchedules = includeSchedules;
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (!isVersion(V2))
      return setUnknownVersionResponse();

    if (hasErrors())
      return setValidationErrorsResponse();

    CoordinateBounds bounds = _searchBoundsFactory.createBounds();

    long time = System.currentTimeMillis();
    if (_time != 0)
      time = _time;

    TripsForBoundsQueryBean query = new TripsForBoundsQueryBean();
    query.setBounds(bounds);
    query.setTime(time);
    query.setMaxCount(_maxCount.getMaxCount());

    TripDetailsInclusionBean inclusion = query.getInclusion();
    inclusion.setIncludeTripBean(_includeTrips);
    inclusion.setIncludeTripSchedule(_includeSchedules);
    inclusion.setIncludeTripStatus(true);

    BeanFactoryV2 factory = getBeanFactoryV2();

    try {
      ListBean<TripDetailsBean> trips = _service.getTripsForBounds(query);
      return setOkResponse(factory.getTripDetailsResponse(trips));
    } catch (OutOfServiceAreaServiceException ex) {
      return setOkResponse(factory.getEmptyList(TripDetailsV2Bean.class, true));
    }
  }
}
