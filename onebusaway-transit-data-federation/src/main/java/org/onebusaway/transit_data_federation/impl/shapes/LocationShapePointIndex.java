/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.collections.Min;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public class LocationShapePointIndex extends AbstractShapePointIndex {

  private double _lat;

  private double _lon;

  public LocationShapePointIndex(double lat, double lon) {
    _lat = lat;
    _lon = lon;
  }

  @Override
  public int getIndex(ShapePoints points) {
    Min<Integer> m = new Min<Integer>();
    int n = points.getSize();
    double[] lats = points.getLats();
    double[] lons = points.getLons();
    for (int i = 0; i < n; i++) {
      double d = distance(_lat, _lon, lats[i], lons[i]);
      m.add(d, i);
    }
    return m.getMinElement();
  }

  @Override
  public CoordinatePoint getPoint(ShapePoints points) {
    return new CoordinatePoint(_lat, _lon);
  }

  @Override
  public PointAndOrientation getPointAndOrientation(ShapePoints points) {
    return new PointAndOrientation(_lat, _lon, 0);
  }

  private static double distance(double lat1, double lon1, double lat2,
      double lon2) {
    double dLat = lat1 - lat2;
    double dLon = lon1 - lon2;
    return Math.sqrt(dLat * dLat + dLon * dLon);
  }

}