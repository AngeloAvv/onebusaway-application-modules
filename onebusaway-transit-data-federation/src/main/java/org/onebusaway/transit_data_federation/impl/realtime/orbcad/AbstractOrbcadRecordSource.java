package org.onebusaway.transit_data_federation.impl.realtime.orbcad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.onebusaway.container.model.HasListeners;
import org.onebusaway.container.model.Listeners;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.csv.EntityHandler;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.calendar.BlockCalendarService;
import org.onebusaway.transit_data_federation.impl.realtime.BlockToTripScheduleAdherenceInterpolation;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.realtime.VehiclePositionListener;
import org.onebusaway.transit_data_federation.services.realtime.VehiclePositionRecord;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource("org.onebusaway.transit_data_federation.impl.realtime.orbcad:name=OrbcadRecordFtpSource")
public abstract class AbstractOrbcadRecordSource implements
    HasListeners<VehiclePositionListener> {

  private static final int REFRESH_INTERVAL_IN_SECONDS = 30;

  private static Logger _log = LoggerFactory.getLogger(AbstractOrbcadRecordSource.class);

  protected ScheduledExecutorService _executor = Executors.newSingleThreadScheduledExecutor();

  private List<VehiclePositionRecord> _records = new ArrayList<VehiclePositionRecord>();

  private int _refreshInterval = REFRESH_INTERVAL_IN_SECONDS;

  private long _lastRefresh = 0;

  private Listeners<VehiclePositionListener> _listeners = new Listeners<VehiclePositionListener>();

  private Map<String, String> _blockIdMapping = new HashMap<String, String>();

  private BlockCalendarService _blockCalendarService;

  private TransitGraphDao _transitGraph;

  protected List<String> _agencyIds;

  private File _blockIdMappingFile;

  /****
   * Statistics
   ****/

  private transient int _recordsTotal = 0;

  private transient int _recordsWithoutScheduleDeviation = 0;

  private transient int _recordsWithoutBlockId = 0;

  private transient int _recordsWithoutBlockIdInGraph = 0;

  private transient int _recordsWithoutServiceDate = 0;

  private transient int _recordsValid = 0;

  private BlockToTripScheduleAdherenceInterpolation _interpolation;

  public void setRefreshInterval(int refreshIntervalInSeconds) {
    _refreshInterval = refreshIntervalInSeconds;
  }

  public void setAgencyId(String agencyId) {
    _agencyIds = Arrays.asList(agencyId);
  }

  public void setAgencyIds(List<String> agencyIds) {
    _agencyIds = agencyIds;
  }

  public void setBlockIdMappingFile(File blockIdMappingFile) {
    _blockIdMappingFile = blockIdMappingFile;
  }

  @Override
  public void addListener(VehiclePositionListener listener) {
    _listeners.addListener(listener);
  }

  @Override
  public void removeListener(VehiclePositionListener listener) {
    _listeners.removeListener(listener);
  }

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraph) {
    _transitGraph = transitGraph;
  }

  @Autowired
  public void setBlockToTripScheduleAdherenceInterpolation(
      BlockToTripScheduleAdherenceInterpolation interpolation) {
    _interpolation = interpolation;
  }

  /****
   * JMX Attributes
   ***/

  @ManagedAttribute
  public int getRecordsTotal() {
    return _recordsTotal;
  }

  @ManagedAttribute
  public int getRecordWithoutScheduleDeviation() {
    return _recordsWithoutScheduleDeviation;
  }

  @ManagedAttribute
  public int getRecordsWithoutBlockId() {
    return _recordsWithoutBlockId;
  }

  @ManagedAttribute
  public int getRecordsWithoutBlockIdInGraph() {
    return _recordsWithoutBlockIdInGraph;
  }

  @ManagedAttribute
  public int getRecordsWithoutServiceDate() {
    return _recordsWithoutServiceDate;
  }

  @ManagedAttribute
  public int getRecordsValid() {
    return _recordsValid;
  }

  /****
   * Setup and Teardown
   ****/

  protected void start() throws SocketException, IOException {

    loadBlockIdMapping();

    setup();

    _executor.scheduleAtFixedRate(new AvlRefreshTask(), 5,
        _refreshInterval / 2, TimeUnit.SECONDS);
  }

  protected void stop() throws IOException {
    _executor.shutdown();
  }

  /****
   * Protected Methods
   ****/

  protected void setup() {

  }

  protected abstract void handleRefresh() throws IOException;

  /****
   * Private Methods
   ****/

  private void loadBlockIdMapping() throws FileNotFoundException, IOException {

    try {

      if (_blockIdMappingFile == null)
        return;

      BufferedReader reader = new BufferedReader(new FileReader(
          _blockIdMappingFile));
      String line = null;

      while ((line = reader.readLine()) != null) {
        int index = line.indexOf(',');
        String from = line.substring(0, index);
        String to = line.substring(index + 1);
        _blockIdMapping.put(from, to);
      }

      reader.close();

    } catch (Throwable ex) {
      _log.warn("error loading block id mapping from file " + _blockIdMapping,
          ex);
    }
  }

  private Date getServiceDateForRecord(OrbcadRecord record, AgencyAndId blockId) {

    long recordTime = record.getTime() * 1000;
    Date from = new Date(recordTime - 30 * 60 * 1000);
    Date to = new Date(recordTime + 30 * 60 * 1000);

    List<Date> serviceDates = _blockCalendarService.getServiceDatesWithinRangeForBlockId(
        blockId, from, to);

    if (serviceDates.size() != 1)
      return null;

    return serviceDates.get(0);
  }

  private AgencyAndId getBlockIdForRecord(OrbcadRecord record) {
    String blockId = Integer.toString(record.getBlock());
    String translated = _blockIdMapping.get(blockId);
    if (translated != null)
      blockId = translated;

    for (String agencyId : _agencyIds) {
      AgencyAndId aid = new AgencyAndId(agencyId, blockId);
      List<TripEntry> trips = _transitGraph.getTripsForBlockId(aid);
      if (trips == null || trips.isEmpty())
        continue;
      return aid;
    }
    return null;
  }

  protected class AvlRefreshTask implements Runnable {

    public void run() {
      try {

        _log.debug("checking if we need to refresh");

        synchronized (this) {
          long t = System.currentTimeMillis();
          if (_lastRefresh + _refreshInterval * 1000 > t)
            return;
          _lastRefresh = t;
        }

        _log.debug("refresh requested");

        handleRefresh();

        for (VehiclePositionListener listener : _listeners) {
          try {
            listener.handleVehiclePositionRecords(_records);
          } catch (Throwable ex) {
            _log.warn("error passing schedule adherence records to listener",
                ex);
          }
        }

        _records.clear();

        _log.debug("refresh complete");

      } catch (Throwable ex) {
        _log.warn("error refreshing data", ex);
      }
    }

  }

  protected class RecordHandler implements EntityHandler {

    @Override
    public void handleEntity(Object bean) {

      OrbcadRecord record = (OrbcadRecord) bean;

      _recordsTotal++;

      if (!record.hasScheduleDeviation()) {
        _recordsWithoutScheduleDeviation++;
        return;
      }

      if (record.getBlock() == 0) {
        _recordsWithoutBlockId++;
        return;
      }

      AgencyAndId blockId = getBlockIdForRecord(record);
      if (blockId == null) {
        _recordsWithoutBlockIdInGraph++;
        return;
      }

      Date serviceDate = getServiceDateForRecord(record, blockId);
      if (serviceDate == null) {
        _recordsWithoutServiceDate++;
        return;
      }

      VehiclePositionRecord message = new VehiclePositionRecord();

      message.setBlockId(blockId);

      message.setServiceDate(serviceDate.getTime());
      message.setCurrentTime(record.getTime() * 1000);
      // In Orbcad, +scheduleDeviation means the bus is early and -schedule
      // deviation means bus is late, which is opposite the
      // ScheduleAdherenceRecord convention
      message.setScheduleDeviation(-record.getScheduleDeviation());

      message.setVehicleId(new AgencyAndId(blockId.getAgencyId(),
          Integer.toString(record.getVehicleId())));

      if (record.hasLat() && record.hasLon())
        message.setCurrentLocation(new CoordinatePoint(record.getLat(),
            record.getLon()));
      
      List<VehiclePositionRecord> messages = _interpolation.interpolate(message);

      _records.addAll(messages);
      _recordsValid++;
    }
  }
}
