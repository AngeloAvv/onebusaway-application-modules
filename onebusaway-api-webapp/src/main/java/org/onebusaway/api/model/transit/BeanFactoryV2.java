package org.onebusaway.api.model.transit;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.api.impl.MaxCountSupport;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopCalendarDayBean;
import org.onebusaway.transit_data.model.StopCalendarDaysBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopRouteDirectionScheduleBean;
import org.onebusaway.transit_data.model.StopRouteScheduleBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopTimeInstanceBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.TripStopTimesBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;

public class BeanFactoryV2 {

  private boolean _includeReferences = true;

  private ReferencesBean _references = new ReferencesBean();

  private MaxCountSupport _maxCount;

  public BeanFactoryV2(boolean includeReferences) {
    _includeReferences = includeReferences;
  }

  public void setMaxCount(MaxCountSupport maxCount) {
    _maxCount = maxCount;
  }

  /****
   * Response Methods
   ****/

  public EntryWithReferencesBean<AgencyV2Bean> getResponse(AgencyBean agency) {
    return entry(getAgency(agency));
  }

  public EntryWithReferencesBean<RouteV2Bean> getResponse(RouteBean route) {
    return entry(getRoute(route));
  }

  public EntryWithReferencesBean<EncodedPolylineBean> getResponse(
      EncodedPolylineBean bean) {
    return entry(bean);
  }

  public Object getResponse(StopBean stop) {
    return entry(getStop(stop));
  }

  public EntryWithReferencesBean<TripV2Bean> getResponse(TripBean trip) {
    return entry(getTrip(trip));
  }

  public EntryWithReferencesBean<TripDetailsV2Bean> getResponse(
      TripDetailsBean tripDetails) {
    return entry(getTripDetails(tripDetails));
  }

  public EntryWithReferencesBean<StopWithArrivalsAndDeparturesV2Bean> getResponse(
      StopWithArrivalsAndDeparturesBean result) {
    return entry(getStopWithArrivalAndDepartures(result));
  }

  public EntryWithReferencesBean<StopScheduleV2Bean> getResponse(
      StopScheduleBean stopSchedule) {
    return entry(getStopSchedule(stopSchedule));
  }

  public EntryWithReferencesBean<StopsForRouteV2Bean> getResponse(
      StopsForRouteBean result, boolean includePolylines) {
    return entry(getStopsForRoute(result, includePolylines));
  }

  public ListWithReferencesBean<AgencyWithCoverageV2Bean> getResponse(
      List<AgencyWithCoverageBean> beans) {
    List<AgencyWithCoverageV2Bean> list = new ArrayList<AgencyWithCoverageV2Bean>();
    for (AgencyWithCoverageBean bean : filter(beans))
      list.add(getAgencyWithCoverage(bean));
    return list(list, list.size() < beans.size());
  }

  public ListWithReferencesBean<RouteV2Bean> getResponse(RoutesBean result) {
    List<RouteV2Bean> beans = new ArrayList<RouteV2Bean>();
    for (RouteBean route : result.getRoutes())
      beans.add(getRoute(route));
    return list(beans, result.isLimitExceeded(),false);
  }

  public ListWithReferencesBean<StopV2Bean> getResponse(StopsBean result) {
    List<StopV2Bean> beans = new ArrayList<StopV2Bean>();
    for (StopBean stop : result.getStops())
      beans.add(getStop(stop));
    return list(beans, result.isLimitExceeded(),false);
  }

  public ListWithReferencesBean<TripDetailsV2Bean> getTripDetailsResponse(
      ListBean<TripDetailsBean> trips) {

    List<TripDetailsV2Bean> beans = new ArrayList<TripDetailsV2Bean>();
    for (TripDetailsBean trip : trips.getList())
      beans.add(getTripDetails(trip));
    return list(beans, trips.isLimitExceeded(),false);
  }

  public ListWithReferencesBean<String> getEntityIdsResponse(
      ListBean<String> ids) {
    return list(ids.getList(), ids.isLimitExceeded());
  }
  
  public <T> ListWithReferencesBean<T> getEmptyList(Class<T> type, boolean outOfRange) {
    return list(new ArrayList<T>(),false,outOfRange);
  }

  /****
   * 
   ***/

  public AgencyV2Bean getAgency(AgencyBean agency) {
    AgencyV2Bean bean = new AgencyV2Bean();
    bean.setDisclaimer(agency.getDisclaimer());
    bean.setId(agency.getId());
    bean.setLang(agency.getLang());
    bean.setName(agency.getName());
    bean.setPhone(agency.getPhone());
    bean.setTimezone(agency.getTimezone());
    bean.setUrl(agency.getUrl());
    return bean;
  }

  public RouteV2Bean getRoute(RouteBean route) {
    RouteV2Bean bean = new RouteV2Bean();

    bean.setAgencyId(route.getAgency().getId());
    addToReferences(route.getAgency());

    bean.setColor(route.getColor());
    bean.setDescription(route.getDescription());
    bean.setId(route.getId());
    bean.setLongName(route.getLongName());
    bean.setShortName(route.getShortName());
    bean.setTextColor(route.getTextColor());
    bean.setType(route.getType());
    bean.setUrl(route.getUrl());

    return bean;
  }

  public StopV2Bean getStop(StopBean stop) {
    StopV2Bean bean = new StopV2Bean();
    bean.setCode(stop.getCode());
    bean.setDirection(stop.getDirection());
    bean.setId(stop.getId());
    bean.setLat(stop.getLat());
    bean.setLon(stop.getLon());
    bean.setLocationType(stop.getLocationType());
    bean.setName(stop.getName());

    List<String> routeIds = new ArrayList<String>();
    for (RouteBean route : stop.getRoutes()) {
      routeIds.add(route.getId());
      addToReferences(route);
    }
    bean.setRouteIds(routeIds);

    return bean;
  }

  public TripV2Bean getTrip(TripBean trip) {

    TripV2Bean bean = new TripV2Bean();

    bean.setId(trip.getId());

    bean.setRouteId(trip.getRoute().getId());
    addToReferences(trip.getRoute());

    bean.setTripHeadsign(trip.getTripHeadsign());
    bean.setTripShortName(trip.getTripShortName());

    bean.setDirectionId(trip.getDirectionId());
    bean.setServiceId(trip.getServiceId());
    bean.setShapeId(trip.getShapeId());

    return bean;
  }

  public TripStatusV2Bean getTripStatus(TripStatusBean tripStatus) {

    TripStatusV2Bean bean = new TripStatusV2Bean();

    bean.setPosition(tripStatus.getPosition());
    bean.setPredicted(tripStatus.isPredicted());
    bean.setScheduleDeviation(tripStatus.getScheduleDeviation());
    bean.setServiceDate(tripStatus.getServiceDate());
    bean.setVehicleId(tripStatus.getVehicleId());

    StopBean stop = tripStatus.getClosestStop();
    if( stop != null) {
      bean.setClosestStop(stop.getId());
      addToReferences(stop);
      bean.setClosestStopTimeOffset(tripStatus.getClosestStopTimeOffset());
    }
    
    return bean;
  }

  public TripStopTimesV2Bean getTripStopTimes(TripStopTimesBean tripStopTimes) {

    TripStopTimesV2Bean bean = new TripStopTimesV2Bean();

    bean.setTimeZone(tripStopTimes.getTimeZone());

    List<TripStopTimeV2Bean> instances = new ArrayList<TripStopTimeV2Bean>();
    for (TripStopTimeBean sti : tripStopTimes.getStopTimes()) {

      TripStopTimeV2Bean stiBean = new TripStopTimeV2Bean();
      stiBean.setArrivalTime(sti.getArrivalTime());
      stiBean.setDepartureTime(sti.getDepartureTime());
      stiBean.setStopHeadsign(sti.getStopHeadsign());

      stiBean.setStopId(sti.getStop().getId());
      addToReferences(sti.getStop());

      instances.add(stiBean);
    }

    bean.setStopTimes(instances);

    TripBean nextTrip = tripStopTimes.getNextTrip();
    if (nextTrip != null) {
      bean.setNextTripId(nextTrip.getId());
      addToReferences(nextTrip);
    }

    TripBean prevTrip = tripStopTimes.getPreviousTrip();
    if (prevTrip != null) {
      bean.setPreviousTripId(prevTrip.getId());
      addToReferences(prevTrip);
    }

    return bean;
  }

  public TripDetailsV2Bean getTripDetails(TripDetailsBean tripDetails) {

    TripDetailsV2Bean bean = new TripDetailsV2Bean();

    bean.setTripId(tripDetails.getTripId());

    TripBean trip = tripDetails.getTrip();
    if (trip != null)
      addToReferences(trip);

    TripStopTimesBean stopTimes = tripDetails.getSchedule();
    if (stopTimes != null)
      bean.setSchedule(getTripStopTimes(stopTimes));

    TripStatusBean status = tripDetails.getStatus();
    if (status != null)
      bean.setStatus(getTripStatus(status));

    return bean;
  }

  public StopScheduleV2Bean getStopSchedule(StopScheduleBean stopSchedule) {

    StopScheduleV2Bean bean = new StopScheduleV2Bean();

    StopV2Bean stop = getStop(stopSchedule.getStop());
    bean.setStop(stop);

    bean.setDate(stopSchedule.getDate().getTime());

    List<StopRouteScheduleV2Bean> stopRouteScheduleBeans = new ArrayList<StopRouteScheduleV2Bean>();

    for (StopRouteScheduleBean stopRouteSchedule : stopSchedule.getRoutes()) {
      StopRouteScheduleV2Bean stopRouteScheduleBean = getStopRouteSchedule(stopRouteSchedule);
      stopRouteScheduleBeans.add(stopRouteScheduleBean);
    }
    bean.setStopRouteSchedules(stopRouteScheduleBeans);

    StopCalendarDaysBean days = stopSchedule.getCalendarDays();
    bean.setTimeZone(days.getTimeZone());

    List<StopCalendarDayV2Bean> dayBeans = new ArrayList<StopCalendarDayV2Bean>();
    for (StopCalendarDayBean day : days.getDays()) {
      StopCalendarDayV2Bean dayBean = getStopCalendarDay(day);
      dayBeans.add(dayBean);
    }
    bean.setStopCalendarDays(dayBeans);

    return bean;
  }

  public StopRouteScheduleV2Bean getStopRouteSchedule(
      StopRouteScheduleBean stopRouteSchedule) {

    StopRouteScheduleV2Bean bean = new StopRouteScheduleV2Bean();

    bean.setRouteId(stopRouteSchedule.getRoute().getId());
    addToReferences(stopRouteSchedule.getRoute());

    List<StopRouteDirectionScheduleV2Bean> directions = bean.getStopRouteDirectionSchedules();
    for (StopRouteDirectionScheduleBean direction : stopRouteSchedule.getDirections())
      directions.add(getStopRouteDirectionSchedule(direction));

    return bean;
  }

  public StopRouteDirectionScheduleV2Bean getStopRouteDirectionSchedule(
      StopRouteDirectionScheduleBean direction) {

    StopRouteDirectionScheduleV2Bean bean = new StopRouteDirectionScheduleV2Bean();
    bean.setTripHeadsign(direction.getTripHeadsign());

    List<ScheduleStopTimeInstanceV2Bean> stopTimes = bean.getScheduleStopTimes();
    for (StopTimeInstanceBean sti : direction.getStopTimes()) {
      ScheduleStopTimeInstanceV2Bean stiBean = new ScheduleStopTimeInstanceV2Bean();
      stiBean.setArrivalTime(sti.getArrivalTime());
      stiBean.setDepartureTime(sti.getDepartureTime());
      stiBean.setServiceId(sti.getServiceId());
      stiBean.setTripId(sti.getTripId());
      stiBean.setStopHeadsign(stiBean.getStopHeadsign());
      stopTimes.add(stiBean);
    }

    return bean;
  }

  public StopCalendarDayV2Bean getStopCalendarDay(StopCalendarDayBean day) {
    StopCalendarDayV2Bean bean = new StopCalendarDayV2Bean();
    bean.setDate(day.getDate().getTime());
    bean.setGroup(day.getGroup());
    return bean;
  }

  public StopsForRouteV2Bean getStopsForRoute(StopsForRouteBean stopsForRoute,
      boolean includePolylines) {
    StopsForRouteV2Bean bean = new StopsForRouteV2Bean();
    List<String> stopIds = new ArrayList<String>();
    for (StopBean stop : stopsForRoute.getStops()) {
      stopIds.add(stop.getId());
      addToReferences(stop);
    }
    bean.setStopIds(stopIds);
    bean.setStopGroupings(stopsForRoute.getStopGroupings());
    if (!includePolylines) {
      for (StopGroupingBean grouping : stopsForRoute.getStopGroupings()) {
        for (StopGroupBean group : grouping.getStopGroups())
          group.setPolylines(null);
      }
    }
    if (includePolylines)
      bean.setPolylines(stopsForRoute.getPolylines());
    return bean;
  }

  public StopWithArrivalsAndDeparturesV2Bean getStopWithArrivalAndDepartures(
      StopWithArrivalsAndDeparturesBean sad) {
    StopWithArrivalsAndDeparturesV2Bean bean = new StopWithArrivalsAndDeparturesV2Bean();

    bean.setStopId(sad.getStop().getId());
    addToReferences(sad.getStop());

    List<ArrivalAndDepartureV2Bean> ads = new ArrayList<ArrivalAndDepartureV2Bean>();
    for (ArrivalAndDepartureBean ad : sad.getArrivalsAndDepartures())
      ads.add(getArrivalAndDeparture(ad));
    bean.setArrivalsAndDepartures(ads);

    List<String> nearbyStopIds = new ArrayList<String>();
    for (StopBean nearbyStop : sad.getNearbyStops()) {
      nearbyStopIds.add(nearbyStop.getId());
      addToReferences(nearbyStop);
    }
    bean.setNearbyStopIds(nearbyStopIds);

    return bean;
  }

  public ArrivalAndDepartureV2Bean getArrivalAndDeparture(
      ArrivalAndDepartureBean ad) {

    ArrivalAndDepartureV2Bean bean = new ArrivalAndDepartureV2Bean();

    bean.setPredictedArrivalTime(ad.getPredictedArrivalTime());
    bean.setPredictedDepartureTime(ad.getPredictedDepartureTime());
    bean.setRouteId(ad.getTrip().getRoute().getId());
    bean.setRouteShortName(ad.getTrip().getRoute().getShortName());
    bean.setRouteLongName(ad.getTrip().getRoute().getLongName());
    bean.setTripHeadsign(ad.getTrip().getTripHeadsign());
    bean.setScheduledArrivalTime(ad.getScheduledArrivalTime());
    bean.setScheduledDepartureTime(ad.getScheduledDepartureTime());
    bean.setStopId(ad.getStopId());
    bean.setStatus(ad.getStatus());
    bean.setTripId(ad.getTrip().getId());

    return bean;
  }

  public AgencyWithCoverageV2Bean getAgencyWithCoverage(
      AgencyWithCoverageBean awc) {

    AgencyWithCoverageV2Bean bean = new AgencyWithCoverageV2Bean();

    bean.setAgencyId(awc.getAgency().getId());
    bean.setLat(awc.getLat());
    bean.setLon(awc.getLon());
    bean.setLatSpan(awc.getLatSpan());
    bean.setLonSpan(awc.getLonSpan());

    addToReferences(awc.getAgency());

    return bean;
  }

  /****
   * References Methods
   ****/

  public void addToReferences(AgencyBean agency) {
    if (!shouldAddReferenceWithId(_references.getAgencies(), agency.getId()))
      return;
    AgencyV2Bean bean = getAgency(agency);
    _references.addAgency(bean);
  }

  public void addToReferences(RouteBean route) {
    if (!shouldAddReferenceWithId(_references.getRoutes(), route.getId()))
      return;
    RouteV2Bean bean = getRoute(route);
    _references.addRoute(bean);
  }

  public void addToReferences(StopBean stop) {
    if (!shouldAddReferenceWithId(_references.getStops(), stop.getId()))
      return;
    StopV2Bean bean = getStop(stop);
    _references.addStop(bean);
  }

  public void addToReferences(TripBean trip) {
    if (!shouldAddReferenceWithId(_references.getStops(), trip.getId()))
      return;
    TripV2Bean bean = getTrip(trip);
    _references.addTrip(bean);
  }

  /****
   * Private Methods
   ****/

  private <T> EntryWithReferencesBean<T> entry(T entry) {
    return new EntryWithReferencesBean<T>(entry, _references);
  }

  private <T> ListWithReferencesBean<T> list(List<T> list, boolean limitExceeded) {
    return new ListWithReferencesBean<T>(list, limitExceeded, _references);
  }
  
  private <T> ListWithReferencesBean<T> list(List<T> list, boolean limitExceeded, boolean outOfRange) {
    return new ListWithRangeAndReferencesBean<T>(list, limitExceeded, outOfRange, _references);
  }

  private <T> List<T> filter(List<T> beans) {
    if (_maxCount == null)
      return beans;
    return _maxCount.filter(beans, false);
  }

  private <T extends HasId> boolean shouldAddReferenceWithId(
      Iterable<T> entities, String id) {

    if (!_includeReferences)
      return false;

    if (entities == null)
      return true;

    for (T entity : entities) {
      if (entity.getId().equals(id))
        return false;
    }

    return true;
  }

}
