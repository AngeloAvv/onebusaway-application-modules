/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.phone.actions.search;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.phone.actions.AbstractAction;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.RoutesQueryBean;
import org.onebusaway.transit_data.model.RoutesQueryBean.EQueryType;

import java.util.List;

public class RouteSelectionAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private static final double SEARCH_RADIUS = 10000;

  private String _routeName;

  private RouteBean _route;

  private List<RouteBean> _routes;

  public void setRouteName(String routeName) {
    _routeName = routeName;
  }

  public String getRouteName() {
    return _routeName;
  }

  public RouteBean getRoute() {
    return _route;
  }

  public List<RouteBean> getRoutes() {
    return _routes;
  }

  @Override
  public String execute() throws Exception {

    if (_currentUser.getDefaultLocationName() == null)
      return NEEDS_DEFAULT_SEARCH_LOCATION;

    double lat = _currentUser.getDefautLocationLat();
    double lon = _currentUser.getDefaultLocationLon();

    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(lat, lon,
        SEARCH_RADIUS);

    RoutesQueryBean routesQuery = new RoutesQueryBean();
    routesQuery.setBounds(bounds);
    routesQuery.setMaxCount(10);
    routesQuery.setQuery(_routeName);
    routesQuery.setType(EQueryType.BOUNDS_OR_CLOSEST);
    
    RoutesBean routesBean = _transitDataService.getRoutes(routesQuery);
    List<RouteBean> routes = routesBean.getRoutes();

    if (routes.size() == 0) {
      return INPUT;
    } else if (routes.size() == 1) {
      _route = routes.get(0);
      return "route";
    } else {
      _routes = routes;
      return "routes";
    }
  }
}
