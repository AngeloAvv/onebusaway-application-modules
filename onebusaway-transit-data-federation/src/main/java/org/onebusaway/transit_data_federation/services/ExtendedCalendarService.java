package org.onebusaway.transit_data_federation.services;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

public interface ExtendedCalendarService {

  public Set<ServiceDate> getServiceDatesForServiceIds(
      ServiceIdActivation serviceIds);

  public Set<Date> getDatesForServiceIds(ServiceIdActivation serviceIds);
  
  public List<Date> getDatesForServiceIdsAsOrderedList(ServiceIdActivation serviceIds);

  public Collection<Date> getServiceDatesWithinRange(
      ServiceIdActivation serviceIds, ServiceInterval interval, Date from,
      Date to);
}