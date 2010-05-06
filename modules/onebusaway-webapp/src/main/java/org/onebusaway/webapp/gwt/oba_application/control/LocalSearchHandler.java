package org.onebusaway.webapp.gwt.oba_application.control;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.webapp.gwt.common.model.ModelEventSink;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchCompleteState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchProgressState;
import org.onebusaway.webapp.gwt.oba_application.model.ResultsModel;
import org.onebusaway.webapp.gwt.oba_application.model.TimedLocalSearchResult;
import org.onebusaway.webapp.gwt.oba_application.search.LocalSearchCallback;
import org.onebusaway.webapp.gwt.oba_application.search.LocalSearchProvider;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalSearchHandler implements LocalSearchCallback {

  private Map<String, LocalSearchResult> _placeResults = new HashMap<String, LocalSearchResult>();

  private PlaceHandler _placeHandler = new PlaceHandler();

  private LocalSearchProvider _searchProvider;

  private ModelEventSink<StateEvent> _events;

  private ResultsModel _model;

  private String _resultId;

  private List<CoordinateBounds> _searchGrid;

  private List<String> _queries = new ArrayList<String>();

  private List<String> _categories = new ArrayList<String>();

  private int _queryIndex = 0;

  private int _gridIndex = 0;

  private int _searchCount = 0;

  private boolean _complete = false;

  public LocalSearchHandler(String resultId, List<CoordinateBounds> searchGrid) {
    _resultId = resultId;
    _searchGrid = searchGrid;
  }

  public void setEventSink(ModelEventSink<StateEvent> events) {
    _events = events;
  }

  public void setLocalSearchProvider(LocalSearchProvider searchProvider) {
    _searchProvider = searchProvider;
  }

  public void setModel(ResultsModel model) {
    _model = model;
  }

  public void addQuery(String query, String category) {
    _queries.add(query);
    _categories.add(category);

  }

  public void run() {
    for (int i = 0; i < 2; i++)
      nextGrid();
  }

  public void onSuccess(List<LocalSearchResult> results) {

    if (results.isEmpty()) {

      _searchCount++;
      nextGrid();

    } else {

      for (LocalSearchResult result : results)
        _placeResults.put(result.getId(), result);

      WebappServiceAsync service = WebappServiceAsync.SERVICE;
      service.getLocalPathsToStops(_resultId, results, _placeHandler);
    }
  }

  public void onFailure(Throwable ex) {
    ex.printStackTrace();
  }

  private void nextGrid() {

    if (_gridIndex == _searchGrid.size()) {
      _queryIndex++;
      _gridIndex = 0;
    }

    if (checkCompletion())
      return;

    CoordinateBounds bounds = _searchGrid.get(_gridIndex++);
    String query = _queries.get(_queryIndex);
    String category = _categories.get(_queryIndex);
    _searchProvider.search(bounds, query, category, this);
  }

  private boolean checkCompletion() {

    double total = _searchGrid.size() * _queries.size();

    if (_gridIndex < _searchGrid.size() && _queryIndex < _queries.size()) {
      double processed = _queryIndex * _searchGrid.size() + _gridIndex;
      _events.fireModelChange(new StateEvent(new SearchProgressState(processed
          / total)));
      return false;
    }

    if (!_complete && _searchCount == total) {
      _complete = true;
      _events.fireModelChange(new StateEvent(new SearchCompleteState()));
    }

    return true;
  }

  private class PlaceHandler implements AsyncCallback<List<TimedPlaceBean>> {

    public void onSuccess(List<TimedPlaceBean> beans) {

      List<TimedLocalSearchResult> results = new ArrayList<TimedLocalSearchResult>(
          beans.size());

      for (final TimedPlaceBean bean : beans) {
        final LocalSearchResult result = _placeResults.get(bean.getPlaceId());
        TimedLocalSearchResult r = new TimedLocalSearchResult(_resultId,
            result, bean);
        results.add(r);
      }

      _model.addEntries(results);
      _searchCount++;
      nextGrid();
    }

    public void onFailure(Throwable ex) {
      ex.printStackTrace();
      _searchCount++;
      nextGrid();
    }
  }

}
