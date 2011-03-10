package org.onebusaway.api.impl;

import org.apache.struts2.rest.handler.XStreamHandler;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.api.model.TimeBean;
import org.onebusaway.api.model.transit.AgencyV2Bean;
import org.onebusaway.api.model.transit.AgencyWithCoverageV2Bean;
import org.onebusaway.api.model.transit.ArrivalAndDepartureV2Bean;
import org.onebusaway.api.model.transit.EntryWithReferencesBean;
import org.onebusaway.api.model.transit.ListWithRangeAndReferencesBean;
import org.onebusaway.api.model.transit.ListWithReferencesBean;
import org.onebusaway.api.model.transit.ReferencesBean;
import org.onebusaway.api.model.transit.RouteV2Bean;
import org.onebusaway.api.model.transit.ScheduleStopTimeInstanceV2Bean;
import org.onebusaway.api.model.transit.StopCalendarDayV2Bean;
import org.onebusaway.api.model.transit.StopRouteDirectionScheduleV2Bean;
import org.onebusaway.api.model.transit.StopRouteScheduleV2Bean;
import org.onebusaway.api.model.transit.StopScheduleV2Bean;
import org.onebusaway.api.model.transit.StopV2Bean;
import org.onebusaway.api.model.transit.StopWithArrivalsAndDeparturesV2Bean;
import org.onebusaway.api.model.transit.StopsForRouteV2Bean;
import org.onebusaway.api.model.transit.TripDetailsV2Bean;
import org.onebusaway.api.model.transit.TripStopTimeV2Bean;
import org.onebusaway.api.model.transit.TripV2Bean;
import org.onebusaway.api.model.where.ArrivalAndDepartureBeanV1;
import org.onebusaway.geospatial.model.EncodedPolygonBean;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.siri.model.VehicleLocation;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopCalendarDaysBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;

import com.thoughtworks.xstream.XStream;

public class CustomXStreamHandler extends XStreamHandler {

  @Override
  protected XStream createXStream() {
    XStream xstream = super.createXStream();
    xstream.setMode(XStream.NO_REFERENCES);
    xstream.alias("response", ResponseBean.class);
    xstream.alias("time", TimeBean.class);
    xstream.alias("stop", StopBean.class);
    xstream.alias("route", RouteBean.class);
    xstream.alias("arrivalAndDeparture", ArrivalAndDepartureBean.class);
    xstream.alias("arrivalAndDeparture", ArrivalAndDepartureBeanV1.class);
    xstream.alias("encodedPolyline", EncodedPolylineBean.class);
    xstream.alias("encodedPolygon", EncodedPolygonBean.class);
    xstream.alias("stopGrouping", StopGroupingBean.class);
    xstream.alias("stopGroup", StopGroupBean.class);
    xstream.alias("agency-with-coverage", AgencyWithCoverageBean.class);
    xstream.alias("calendar-days", StopCalendarDaysBean.class);

    xstream.alias("entryWithReferences", EntryWithReferencesBean.class);
    xstream.alias("listWithReferences", ListWithReferencesBean.class);
    xstream.alias("listWithRangeAndReferences", ListWithRangeAndReferencesBean.class);
    xstream.alias("references", ReferencesBean.class);

    xstream.alias("agency", AgencyV2Bean.class);
    xstream.alias("route", RouteV2Bean.class);
    xstream.alias("stop", StopV2Bean.class);
    xstream.alias("trip", TripV2Bean.class);
    xstream.alias("tripDetails", TripDetailsV2Bean.class);
    xstream.alias("tripStopTime", TripStopTimeV2Bean.class);
    xstream.alias("stopSchedule", StopScheduleV2Bean.class);
    xstream.alias("stopRouteSchedule", StopRouteScheduleV2Bean.class);
    xstream.alias("stopRouteDirectionSchedule",
        StopRouteDirectionScheduleV2Bean.class);
    xstream.alias("scheduleStopTime", ScheduleStopTimeInstanceV2Bean.class);
    xstream.alias("stopCalendarDay", StopCalendarDayV2Bean.class);
    xstream.alias("stopWithArrivalsAndDepartures",
        StopWithArrivalsAndDeparturesV2Bean.class);
    xstream.alias("arrivalAndDeparture", ArrivalAndDepartureV2Bean.class);
    xstream.alias("agencyWithCoverage", AgencyWithCoverageV2Bean.class);
    xstream.alias("stopsForRoute",StopsForRouteV2Bean.class);

    xstream.alias("VehicleLocation",VehicleLocation.class);
    
    return xstream;
  }

}
