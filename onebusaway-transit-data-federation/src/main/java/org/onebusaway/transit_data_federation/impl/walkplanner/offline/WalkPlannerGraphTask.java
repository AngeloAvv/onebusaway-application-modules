package org.onebusaway.transit_data_federation.impl.walkplanner.offline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.transit_data_federation.impl.ProjectedPointFactory;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.services.RunnableWithOutputPath;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerGraph;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.opentripplanner.graph_builder.model.osm.OSMNode;
import org.opentripplanner.graph_builder.model.osm.OSMRelation;
import org.opentripplanner.graph_builder.model.osm.OSMWay;
import org.opentripplanner.graph_builder.services.osm.OpenStreetMapContentHandler;
import org.opentripplanner.graph_builder.services.osm.OpenStreetMapProvider;
import org.springframework.beans.factory.annotation.Autowired;

import edu.washington.cs.rse.collections.combinations.Combinations;
import edu.washington.cs.rse.collections.tuple.Pair;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

public class WalkPlannerGraphTask implements RunnableWithOutputPath {

  private OpenStreetMapProvider _provider;

  private File _outputPath;

  private boolean _createEmptyGraph = false;

  @Autowired
  public void setOpenStreetMapProvider(OpenStreetMapProvider provider) {
    _provider = provider;
  }

  public void setOutputPath(File path) {
    _outputPath = path;
  }

  public void setCreateEmptyGraph(boolean createEmptyGraph) {
    _createEmptyGraph = createEmptyGraph;
  }

  public void run() {

    System.out.println("======== WalkPlannerGraphFactory =>");

    try {

      Handler handler = new Handler();

      if (!_createEmptyGraph)
        populateGraph(handler);

      WalkPlannerGraph graph = handler.getGraph();
      ObjectSerializationLibrary.writeObject(_outputPath, graph);
    } catch (Exception ex) {
      throw new IllegalStateException("error constructing WalkPlannerGraph", ex);
    }
  }

  private void populateGraph(Handler handler) throws IOException {
    _provider.readOSM(handler);
  }

  private class Handler implements OpenStreetMapContentHandler {

    private Map<Integer, NodeInfo> _nodes = new HashMap<Integer, NodeInfo>();

    private List<Pair<Integer>> _edges = new ArrayList<Pair<Integer>>();

    private int _nodeCount;

    private int _wayCount;

    public WalkPlannerGraph getGraph() {

      Set<Integer> nodesWithNeighbors = new HashSet<Integer>();

      for (Pair<Integer> edges : _edges) {
        if (_nodes.containsKey(edges.getFirst())
            && _nodes.containsKey(edges.getSecond())) {
          nodesWithNeighbors.add(edges.getFirst());
          nodesWithNeighbors.add(edges.getSecond());
        }
      }

      // Remove all island
      _nodes.keySet().retainAll(nodesWithNeighbors);

      WalkPlannerGraphImpl graph = new WalkPlannerGraphImpl(_nodes.size());
      Map<Integer, WalkNodeEntryImpl> idMapping = new HashMap<Integer, WalkNodeEntryImpl>();

      for (Map.Entry<Integer, NodeInfo> entry : _nodes.entrySet()) {
        Integer currentId = entry.getKey();
        NodeInfo info = entry.getValue();
        ProjectedPoint location = ProjectedPointFactory.forward(info.getLocationLatLng());
        WalkNodeEntryImpl walkNodeEntry = graph.addNode(currentId, location);
        idMapping.put(currentId, walkNodeEntry);
      }

      for (Pair<Integer> edge : _edges) {
        WalkNodeEntryImpl nodeA = idMapping.get(edge.getFirst());
        WalkNodeEntryImpl nodeB = idMapping.get(edge.getSecond());
        if (nodeA != null && nodeB != null) {
          double distance = nodeA.getLocation().distance(nodeB.getLocation());
          nodeA.addEdge(nodeB, distance);
          nodeB.addEdge(nodeA, distance);
        }
      }

      return graph;
    }

    @Override
    public void addNode(OSMNode node) {

      if (_nodeCount % 1000 == 0)
        System.out.println("nodes=" + _nodeCount);
      _nodeCount++;

      CoordinatePoint cp = new CoordinatePoint(node.getLat(), node.getLon());
      _nodes.put(node.getId(), new NodeInfo(cp));
    }

    @Override
    public void addRelation(OSMRelation relation) {

    }

    @Override
    public void addWay(OSMWay way) {

      if (_wayCount % 1000 == 0)
        System.out.println("ways=" + _wayCount);
      _wayCount++;

      Map<String, String> tags = way.getTags();
      String value = tags.get("highway");
      if (value == null || value.equals("motorway")
          || value.equals("motorway_link"))
        return;

      for (Pair<Integer> pair : Combinations.getSequentialPairs(way.getNodeRefs()))
        _edges.add(pair);
    }
  }

  private static class NodeInfo {

    private CoordinatePoint _locationLatLng;

    public NodeInfo(CoordinatePoint cp) {
      _locationLatLng = cp;
    }

    public CoordinatePoint getLocationLatLng() {
      return _locationLatLng;
    }
  }

}
