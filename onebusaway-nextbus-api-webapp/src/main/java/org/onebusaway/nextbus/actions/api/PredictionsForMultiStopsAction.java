/**
 * Copyright (C) 2015 Cambridge Systematics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.nextbus.actions.api;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.model.transiTime.Predictions;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.opensymphony.xwork2.ModelDriven;

public class PredictionsForMultiStopsAction extends NextBusApiBase implements
    ModelDriven<Body<List<Predictions>>> {

  private String agencyId;

  private Set<String> stops;

  private String routeTag;

  public String getA() {
    return agencyId;
  }

  public void setA(String agencyId) {
    this.agencyId = getMappedAgency(agencyId);
  }

  public Set<String> getStops() {
    return stops;
  }

  public void setStops(Set<String> stops) {
    this.stops = stops;
  }

  public String getRouteTag() {
    return routeTag;
  }

  public void setRouteTag(String routeTag) {
    this.routeTag = _tdsMappingService.getRouteIdFromShortName(routeTag);
  }

  public DefaultHttpHeaders index() {
    return new DefaultHttpHeaders("success");
  }

  public Body<List<Predictions>> getModel() {

    Body<List<Predictions>> body = new Body<List<Predictions>>();

    if (isValid(body)) {
      String serviceUrl = getServiceUrl() + agencyId + PREDICTIONS_COMMAND
          + "?";
      String routeStopIds = getStopParams();
      String uri = serviceUrl + routeStopIds + "format=" + REQUEST_TYPE;

      try {
        JsonArray predictionsJson = getJsonObject(uri).getAsJsonArray(
            "predictions");
        Type listType = new TypeToken<List<Predictions>>() {
        }.getType();
        List<List<Predictions>> predictions = new Gson().fromJson(
            predictionsJson, listType);

        body.getResponse().addAll(predictions);
      } catch (Exception e) {

        body.getErrors().add(new BodyError("No valid results found."));
      }
    }

    return body;

  }

  private String getStopIdParams() {
    StringBuilder sb = new StringBuilder();
    for (String stopId : stops) {
      StopBean stopBean = _transitDataService.getStop(stopId);
      for (RouteBean routeBean : stopBean.getRoutes()) {
        sb.append("rs=");
        sb.append(routeBean.getId());
        sb.append("|");
        sb.append(stopId);
        sb.append("&");
      }
    }
    return sb.toString();
  }

  private String getStopParams() {
    StringBuilder sb = new StringBuilder();
    for (String stop : stops) {
      sb.append("rs=");
      sb.append(stop);
      sb.append("&");
    }
    return sb.toString();
  }

  private boolean isValid(Body body) {

    if (!isValidAgency(body, agencyId)) {
      return false;
    }
    for (String stop : stops) {
      String[] stopArray = stop.split("\\|");
      if (stopArray.length < 2) {
        String error = "The stop "
            + stop
            + "was invalid because it did not contain a route, optional dir, and stop tag";
        body.getErrors().add(new BodyError(error));
        return false;
      }

      try {
        StopBean stopBean = _transitDataService.getStop(_tdsMappingService.getStopIdFromStopCode(stopArray[1]));
        boolean routeExists = false;
        for (RouteBean routeBean : stopBean.getRoutes()) {
          if (routeBean.getId().equals(
              _tdsMappingService.getRouteIdFromShortName(stopArray[0]))) {
            routeExists = true;
            break;
          }
        }
        if (!routeExists) {
          String error = "For agency=" + getA() + " route r=" + stopArray[1]
              + " is not currently available. It might be initializing still.";
          body.getErrors().add(new BodyError(error));
          return false;
        }

      } catch (ServiceException se) {
        String error = "For agency=" + getA() + " stop s=" + stopArray[1]
            + " is on none of the directions for r=" + stopArray[0]
            + " so cannot determine which stop to provide data for.";

        body.getErrors().add(new BodyError(error));
        return false;
      }
    }
    return true;
  }
}
