package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.Comparator;
import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class BlockTripLooseComparator implements Comparator<BlockTripEntry> {
  @Override
  public int compare(BlockTripEntry o1, BlockTripEntry o2) {

    List<BlockStopTimeEntry> stopTimes1 = o1.getStopTimes();
    List<BlockStopTimeEntry> stopTimes2 = o2.getStopTimes();

    if (stopTimes1.isEmpty())
      throw new IllegalStateException("block trip has no stop times: " + o1);
    if (stopTimes2.isEmpty())
      throw new IllegalStateException("block trip has no stop times: " + o2);

    BlockStopTimeEntry bst1 = stopTimes1.get(0);
    BlockStopTimeEntry bst2 = stopTimes2.get(0);
    
    StopTimeEntry st1 = bst1.getStopTime();
    StopTimeEntry st2 = bst2.getStopTime();

    return st1.getDepartureTime() - st2.getDepartureTime();
  }
}