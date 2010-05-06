package org.onebusaway.transit_data_federation.impl.offline;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.TransitDataFederationMutableDao;

import edu.washington.cs.rse.collections.stats.Counter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GenerateRouteCollectionsTask implements Runnable {

  private GtfsRelationalDao _gtfsDao;

  private TransitDataFederationMutableDao _whereMutableDao;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  @Autowired
  public void setWhereMutableDao(TransitDataFederationMutableDao whereMutableDao) {
    _whereMutableDao = whereMutableDao;
  }

  @Transactional
  public void run() {

    Collection<Route> routes = _gtfsDao.getAllRoutes();

    Map<AgencyAndId, List<Route>> routesByKey = new HashMap<AgencyAndId, List<Route>>();
    Counter<Route> tripCounts = new Counter<Route>();

    for (Route route : routes) {
      String id = trim(route.getShortName());
      if (id == null || id.length() == 0)
        id = trim(route.getLongName());
      if (id == null || id.length() == 0)
        throw new IllegalStateException("no short or long name for route "
            + route.getId());
      AgencyAndId key = new AgencyAndId(route.getAgency().getId(), id);
      List<Route> forKey = routesByKey.get(key);
      if (forKey == null) {
        forKey = new ArrayList<Route>();
        routesByKey.put(key, forKey);
      }
      forKey.add(route);

      List<Trip> trips = _gtfsDao.getTripsForRoute(route);
      tripCounts.increment(route, trips.size());
    }

    for (Map.Entry<AgencyAndId, List<Route>> entry : routesByKey.entrySet()) {
      AgencyAndId key = entry.getKey();
      List<Route> routesForKey = entry.getValue();

      RouteCollection routeCollection = new RouteCollection();
      routeCollection.setId(key);

      setPropertiesOfRouteCollectionFromRoutes(routesForKey, tripCounts,
          routeCollection);

      routeCollection.setRoutes(routesForKey);
      _whereMutableDao.save(routeCollection);
    }

    // _whereMutableDao.flush();
  }

  private void setPropertiesOfRouteCollectionFromRoutes(List<Route> routes,
      Counter<Route> tripCounts, RouteCollection target) {

    Counter<String> shortNames = new Counter<String>();
    Counter<String> longNames = new Counter<String>();
    Counter<String> descriptions = new Counter<String>();
    Counter<String> colors = new Counter<String>();
    Counter<String> textColors = new Counter<String>();
    Counter<String> urls = new Counter<String>();
    Counter<Integer> types = new Counter<Integer>();

    for (Route route : routes) {

      int count = tripCounts.getCount(route);

      addValueToCounterIfValid(route.getShortName(), shortNames, count);
      addValueToCounterIfValid(route.getLongName(), longNames, count);
      addValueToCounterIfValid(route.getDesc(), descriptions, count);
      addValueToCounterIfValid(route.getColor(), colors, count);
      addValueToCounterIfValid(route.getTextColor(), textColors, count);
      addValueToCounterIfValid(route.getUrl(), urls, count);

      types.increment(route.getType(), count);
    }

    if (shortNames.size() > 0)
      target.setShortName(shortNames.getMax());

    if (longNames.size() > 0)
      target.setLongName(longNames.getMax());

    if (descriptions.size() > 0)
      target.setDescription(descriptions.getMax());

    if (colors.size() > 0)
      target.setColor(colors.getMax());

    if (textColors.size() > 0)
      target.setTextColor(textColors.getMax());

    if (urls.size() > 0)
      target.setUrl(urls.getMax());

    target.setType(types.getMax());
  }

  private <T> void addValueToCounterIfValid(String value,
      Counter<String> counts, int count) {
    value = trim(value);
    if (value != null && value.length() > 0)
      counts.increment(value, count);
  }

  private String trim(String value) {
    if (value == null)
      return value;
    return value.trim();
  }
}
