package org.onebusaway.webapp.actions.admin.problems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordQueryBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.webapp.actions.bundles.ProblemReportStatus;
import org.onebusaway.webapp.actions.bundles.ResourceBundleSupport;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

@Results({
    @Result(type = "redirectAction", name = "list", params = {
        "actionName", "trip-problem-reports", "tripId", "${model.tripId}",
        "parse", "true"}),
    @Result(type = "redirectAction", name = "update", params = {
        "actionName", "trip-problem-reports!edit", "tripId", "${model.tripId}",
        "id", "${model.id}", "parse", "true"})})
public class TripProblemReportsAction extends ActionSupport implements
    ModelDriven<TripProblemReportBean> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private List<TripProblemReportBean> _reports;

  private TripProblemReportBean _model = new TripProblemReportBean();

  private List<VehicleLocationRecordBean> _vehicleLocationRecords;

  private int _minutesBefore = 20;

  private int _minutesAfter = 10;

  private List<String> _labels;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public TripProblemReportBean getModel() {
    return _model;
  }

  public void setMinutesBefore(int minutesBefore) {
    _minutesBefore = minutesBefore;
  }

  public void setMinutesAfter(int minutesAfter) {
    _minutesAfter = minutesAfter;
  }

  public List<TripProblemReportBean> getReports() {
    return _reports;
  }

  public List<VehicleLocationRecordBean> getVehicleLocationRecords() {
    return _vehicleLocationRecords;
  }

  @Override
  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "model.tripId", message = "missing tripId")})
  public String execute() {
    _reports = _transitDataService.getAllTripProblemReportsForTripId(_model.getTripId());
    return SUCCESS;
  }

  @Validations(requiredFields = {@RequiredFieldValidator(fieldName = "model.id", message = "missing id")}, requiredStrings = {@RequiredStringValidator(fieldName = "model.tripId", message = "missing tripId")})
  public String edit() {
    _model = _transitDataService.getTripProblemReportForTripIdAndId(
        _model.getTripId(), _model.getId());
    if (_model == null)
      return ERROR;

    long time = _model.getTime();
    long timeFrom = time - _minutesBefore * 60 * 1000;
    long timeTo = time + _minutesAfter * 60 * 1000;

    TripBean trip = _model.getTrip();

    VehicleLocationRecordQueryBean query = new VehicleLocationRecordQueryBean();
    query.setServiceDate(_model.getServiceDate());
    if (trip != null)
      query.setBlockId(trip.getBlockId());
    query.setVehicleId(_model.getVehicleId());
    query.setFromTime(timeFrom);
    query.setToTime(timeTo);

    ListBean<VehicleLocationRecordBean> records = _transitDataService.getVehicleLocationRecords(query);
    _vehicleLocationRecords = records.getList();

    _labels = _transitDataService.getAllTripProblemReportLabels();

    // Deduplicate labels
    _labels = new ArrayList<String>(new HashSet<String>(_labels));
    Collections.sort(_labels);

    return "edit";
  }

  @Validations(requiredFields = {@RequiredFieldValidator(fieldName = "model.id", message = "missing id")}, requiredStrings = {@RequiredStringValidator(fieldName = "model.tripId", message = "missing tripId")})
  public String update() {
    _transitDataService.updateTripProblemReport(_model);
    return "update";
  }

  @Validations(requiredFields = {@RequiredFieldValidator(fieldName = "model.id", message = "missing id")}, requiredStrings = {@RequiredStringValidator(fieldName = "model.tripId", message = "missing tripId")})
  public String delete() {
    _transitDataService.deleteTripProblemReportForTripIdAndId(
        _model.getTripId(), _model.getId());

    return "list";
  }

  public Map<String, String> getStatusValues() {
    return ResourceBundleSupport.getLocaleMap(this, ProblemReportStatus.class);
  }
  
  public List<String> getLabels() {
    return _labels;
  }
}
