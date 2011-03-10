package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.exceptions.InvalidArgumentServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data_federation.model.SearchResult;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;
import org.onebusaway.transit_data_federation.services.StopSearchService;
import org.onebusaway.transit_data_federation.services.beans.GeospatialBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopsBeanService;

import edu.washington.cs.rse.collections.stats.Min;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

import org.apache.lucene.queryParser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
class StopsBeanServiceImpl implements StopsBeanService {

  private static Logger _log = LoggerFactory.getLogger(StopsBeanServiceImpl.class);

  private static final double MIN_SCORE = 1.0;

  @Autowired
  private StopSearchService _searchService;

  @Autowired
  private StopBeanService _stopBeanService;

  @Autowired
  private GeospatialBeanService _geospatialBeanService;

  @Autowired
  private ExtendedGtfsRelationalDao _dao;

  @Override
  public StopsBean getStops(SearchQueryBean queryBean) throws ServiceException {
    String query = queryBean.getQuery();
    if (query == null)
      return getStopsByBounds(queryBean);
    else
      return getStopsByBoundsAndQuery(queryBean);
  }

  private StopsBean getStopsByBounds(SearchQueryBean queryBean)
      throws ServiceException {

    CoordinateBounds bounds = queryBean.getBounds();

    List<AgencyAndId> stopIds = _geospatialBeanService.getStopsByBounds(bounds);

    boolean limitExceeded = BeanServiceSupport.checkLimitExceeded(stopIds,
        queryBean.getMaxCount());
    List<StopBean> stopBeans = new ArrayList<StopBean>();

    for (AgencyAndId stopId : stopIds) {
      StopBean stopBean = _stopBeanService.getStopForId(stopId);
      if (stopBean == null)
        throw new ServiceException();

      /**
       * If the stop doesn't have any routes actively serving it, don't include
       * it in the results
       */
      if (stopBean.getRoutes().isEmpty())
        continue;

      stopBeans.add(stopBean);
    }

    return constructResult(stopBeans, limitExceeded);
  }

  private StopsBean getStopsByBoundsAndQuery(SearchQueryBean queryBean)
      throws ServiceException {

    CoordinateBounds b = queryBean.getBounds();
    String query = queryBean.getQuery();
    int maxCount = queryBean.getMaxCount();

    CoordinateRectangle bounds = new CoordinateRectangle(b.getMinLat(),
        b.getMinLon(), b.getMaxLat(), b.getMaxLon());
    CoordinatePoint center = bounds.getCenter();

    SearchResult<AgencyAndId> stops;
    try {
      stops = _searchService.searchForStopsByCode(query, 10, MIN_SCORE);
    } catch (ParseException e) {
      throw new InvalidArgumentServiceException("query", "queryParseError");
    } catch (IOException e) {
      _log.error("error executing stop search: query=" + query, e);
      e.printStackTrace();
      throw new ServiceException();
    }

    Min<StopBean> closest = new Min<StopBean>();
    List<StopBean> stopBeans = new ArrayList<StopBean>();

    for (AgencyAndId aid : stops.getResults()) {
      StopBean stopBean = _stopBeanService.getStopForId(aid);
      if (bounds.contains(stopBean.getLat(), stopBean.getLon()))
        stopBeans.add(stopBean);
      double distance = SphericalGeometryLibrary.distance(center.getLat(),
          center.getLon(), stopBean.getLat(), stopBean.getLon());
      closest.add(distance, stopBean);
    }

    boolean limitExceeded = BeanServiceSupport.checkLimitExceeded(stopBeans,
        maxCount);

    // If nothing was found in range, add the closest result
    if (stopBeans.isEmpty() && !closest.isEmpty())
      stopBeans.add(closest.getMinElement());

    return constructResult(stopBeans, limitExceeded);
  }

  @Override
  public ListBean<String> getStopsIdsForAgencyId(String agencyId) {
    List<AgencyAndId> stopIds = _dao.getStopIdsForAgencyId(agencyId);
    List<String> ids = new ArrayList<String>();
    for (AgencyAndId id : stopIds)
      ids.add(AgencyAndIdLibrary.convertToString(id));
    return new ListBean<String>(ids, false);
  }

  private StopsBean constructResult(List<StopBean> stopBeans,
      boolean limitExceeded) {

    Collections.sort(stopBeans, new StopBeanIdComparator());

    StopsBean result = new StopsBean();
    result.setStopBeans(stopBeans);
    result.setLimitExceeded(limitExceeded);
    return result;
  }

}
