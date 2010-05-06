package org.onebusaway.transit_data.model.oba;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.EncodedPolygonBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MinTransitTimeResult implements Serializable {

  private static final long serialVersionUID = 1L;

  private String _resultId;

  private boolean _complete = false;

  private List<EncodedPolygonBean> _polygons = new ArrayList<EncodedPolygonBean>();

  private List<Integer> _times = new ArrayList<Integer>();

  private List<CoordinateBounds> _searchGrid = new ArrayList<CoordinateBounds>();

  public String getResultId() {
    return _resultId;
  }

  public void setResultId(String resultId) {
    _resultId = resultId;
  }

  public boolean isComplete() {
    return _complete;
  }

  public void setComplete(boolean complete) {
    _complete = complete;
  }

  public List<EncodedPolygonBean> getTimePolygons() {
    return _polygons;
  }

  /**
   * 
   * @return times, in minutes
   */
  public List<Integer> getTimes() {
    return _times;
  }

  public List<CoordinateBounds> getSearchGrid() {
    return _searchGrid;
  }

  public void setSearchGrid(List<CoordinateBounds> searchGrid) {
    _searchGrid = searchGrid;
  }
}
