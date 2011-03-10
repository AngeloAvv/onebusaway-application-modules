package org.onebusaway.transit_data_federation.impl.walkplanner;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.onebusaway.collections.Min;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.geospatial.HierarchicalSTRtree;
import org.onebusaway.geospatial.HierarchicalSTRtreeFactory;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.ProjectedPointFactory;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.tripplanner.AStarSearch;
import org.onebusaway.transit_data_federation.impl.tripplanner.AStarSearch.NoPathToGoalException;
import org.onebusaway.transit_data_federation.impl.walkplanner.offline.WalkEdgeEntryImpl;
import org.onebusaway.transit_data_federation.impl.walkplanner.offline.WalkPlannerGraphImpl;
import org.onebusaway.transit_data_federation.impl.walkplanner.states.AtNodeWalkState;
import org.onebusaway.transit_data_federation.impl.walkplanner.states.NearEdgeWalkState;
import org.onebusaway.transit_data_federation.impl.walkplanner.states.NearNodeWalkState;
import org.onebusaway.transit_data_federation.impl.walkplanner.states.WalkState;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkNode;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlannerConstraints;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkEdgeEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkNodeEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerGraph;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerService;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WalkPlannerServiceImpl implements WalkPlannerService {

  private FederatedTransitDataBundle _bundle;

  private TripPlannerConstants _constants;

  private WalkPlannerGraph _graph;

  /**
   * In meters
   */
  private double _treeGridLength = 10000;

  private HierarchicalSTRtree<WalkNodeEntry> _tree = null;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setTripPlannerConstants(TripPlannerConstants constants) {
    _constants = constants;
  }

  public void setWalkPlannerGraph(WalkPlannerGraph graph) {
    _graph = graph;
    refreshTree();
  }

  public void setTreeGridLength(double treeGridLength) {
    _treeGridLength = treeGridLength;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.WALK_PLANNER_GRAPH)
  public void setup() throws IOException, ClassNotFoundException {
    File path = _bundle.getWalkPlannerGraphPath();
    if (path.exists()) {
      _graph = ObjectSerializationLibrary.readObject(path);
    } else {
      _graph = new WalkPlannerGraphImpl();
    }

    refreshTree();
  }

  /****
   * {@link WalkPlannerService} Service
   ****/

  @Override
  public WalkPlannerGraph getWalkPlannerGraph() {
    return _graph;
  }

  public WalkPlan getWalkPlan(CoordinatePoint latLonFrom,
      CoordinatePoint latLonTo) throws NoPathException {
    return getWalkPlan(latLonFrom, latLonTo, new WalkPlannerConstraints());
  }

  public WalkPlan getWalkPlan(CoordinatePoint latLonFrom,
      CoordinatePoint latLonTo, WalkPlannerConstraints constraints)
      throws NoPathException {

    ProjectedPoint pFrom = ProjectedPointFactory.forward(latLonFrom);
    ProjectedPoint pTo = ProjectedPointFactory.forward(latLonTo);

    WalkState fromSegment = getClosestWalkSegment(pFrom, true);
    WalkState toSegment = getClosestWalkSegment(pTo, false);

    try {
      WalkProblem walkProblem = new WalkProblem(constraints, toSegment);
      Map<WalkState, WalkState> cameFrom = AStarSearch.search(walkProblem,
          fromSegment, toSegment);

      LinkedList<WalkNode> path = new LinkedList<WalkNode>();
      WalkState node = toSegment;
      while (node != null) {
        exportStateToPath(node, path);
        node = cameFrom.get(node);
      }

      return new WalkPlan(path);

    } catch (NoPathToGoalException e) {
      throw new NoPathException();
    }

  }

  /****
   * 
   ****/

  private void refreshTree() {

    HierarchicalSTRtreeFactory<WalkNodeEntry> factory = new HierarchicalSTRtreeFactory<WalkNodeEntry>();
    _tree = null;

    Iterable<WalkNodeEntry> nodes = _graph.getNodes();

    Iterator<WalkNodeEntry> it = nodes.iterator();
    if (it.hasNext()) {
      WalkNodeEntry first = it.next();
      ProjectedPoint p = first.getLocation();
      factory.setLatAndLonStep(p.getLat(), p.getLon(), _treeGridLength);

      for (WalkNodeEntry node : nodes) {
        ProjectedPoint pNode = node.getLocation();
        factory.add(pNode.getLat(), pNode.getLon(), node);
      }
      _tree = factory.create();
    }
  }

  private WalkState getClosestWalkSegment(ProjectedPoint point, boolean forward)
      throws NoPathException {

    Collection<WalkNodeEntry> nodes = getNodesNearLocation(point.getLat(),
        point.getLon(), _constants.getInitialMaxDistanceToWalkNode(),
        _constants.getMaxDistanceToWalkNode());

    Set<WalkEdgeEntry> edges = getEdgesForNodes(nodes);

    if (edges.isEmpty())
      throw new NoPathException();

    Min<WalkState> min = new Min<WalkState>();
    for (WalkEdgeEntry edge : edges) {
      WalkState state = getStateForEdge(point, forward, edge);
      min.add(state.getDistanceToLocation(), state);
    }

    return min.getMinElement();
  }

  private Collection<WalkNodeEntry> getNodesNearLocation(double lat,
      double lon, double initialSearchRadius, double maxRadius) {

    if (_tree == null)
      return Collections.emptyList();

    double radius = initialSearchRadius;

    while (true) {
      CoordinateBounds bounds = SphericalGeometryLibrary.bounds(lat, lon,
          radius);
      List<WalkNodeEntry> nodes = _tree.query(bounds);
      if (!nodes.isEmpty())
        return nodes;
      if (radius == maxRadius)
        return Collections.emptyList();
      radius = Math.min(radius * 2, maxRadius);
    }
  }

  private Set<WalkEdgeEntry> getEdgesForNodes(Collection<WalkNodeEntry> nodes)
      throws NoPathException {

    Set<WalkEdgeEntry> edges = new HashSet<WalkEdgeEntry>();

    for (WalkNodeEntry node : nodes) {

      for (WalkEdgeEntry edge : node.getEdges()) {

        WalkNodeEntry from = edge.getNodeFrom();
        WalkNodeEntry to = edge.getNodeTo();

        if (from.getId() > to.getId()) {
          from = edge.getNodeTo();
          to = edge.getNodeFrom();
        }
        edge = new WalkEdgeEntryImpl(from, to, edge.getDistance());
        edges.add(edge);
      }
    }

    return edges;
  }

  private WalkState getStateForEdge(ProjectedPoint point, boolean forward,
      WalkEdgeEntry edge) {

    WalkNodeEntry nodeFrom = edge.getNodeFrom();
    WalkNodeEntry nodeTo = edge.getNodeTo();

    ProjectedPoint pointFrom = nodeFrom.getLocation();
    ProjectedPoint pointTo = nodeTo.getLocation();

    ProjectedPoint pointOnEdge = StreetGraphLibrary.computeClosestPointOnEdge(
        edge, point);

    if (pointFrom.equals(pointOnEdge))
      return new NearNodeWalkState(nodeFrom, point, forward);
    else if (pointTo.equals(pointOnEdge))
      return new NearNodeWalkState(nodeTo, point, forward);
    else
      return new NearEdgeWalkState(edge, point, pointOnEdge, forward);
  }

  private void exportStateToPath(WalkState state, LinkedList<WalkNode> path) {
    if (state instanceof AtNodeWalkState) {
      AtNodeWalkState atNode = (AtNodeWalkState) state;
      path.addFirst(new WalkNode(atNode.getLocation()));
    } else if (state instanceof NearNodeWalkState) {
      NearNodeWalkState nearNode = (NearNodeWalkState) state;
      path.addFirst(new WalkNode(nearNode.getLocation()));
    } else if (state instanceof NearEdgeWalkState) {
      NearEdgeWalkState nearEdge = (NearEdgeWalkState) state;
      if (nearEdge.isForward()) {
        path.addFirst(new WalkNode(nearEdge.getLocationOnEdge()));
        path.addFirst(new WalkNode(nearEdge.getLocation()));
      } else {
        path.addFirst(new WalkNode(nearEdge.getLocation()));
        path.addFirst(new WalkNode(nearEdge.getLocationOnEdge()));
      }
    }
  }
}
