package org.onebusaway.utility;

import static org.onebusaway.utility.InterpolationLibrary.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;

public class InterpolationLibraryTest {

  @Test
  public void testSimple() {

    SortedMap<Double, Double> values = new TreeMap<Double, Double>();
    values.put(0.0, 0.0);
    values.put(1.0, 2.0);
    values.put(2.0, 6.0);

    assertEquals(-1.5, interpolate(values, -0.75), 0.0);
    assertEquals(-1.0, interpolate(values, -0.5), 0.0);
    assertEquals(-0.5, interpolate(values, -0.25), 0.0);

    assertEquals(0.0, interpolate(values, 0.0), 0.0);

    assertEquals(0.5, interpolate(values, 0.25), 0.0);
    assertEquals(1.0, interpolate(values, 0.5), 0.0);
    assertEquals(1.5, interpolate(values, 0.75), 0.0);

    assertEquals(2.0, interpolate(values, 1.0), 0.0);

    assertEquals(3.0, interpolate(values, 1.25), 0.0);
    assertEquals(4.0, interpolate(values, 1.5), 0.0);
    assertEquals(5.0, interpolate(values, 1.75), 0.0);

    assertEquals(6.0, interpolate(values, 2.0), 0.0);

    assertEquals(7.0, interpolate(values, 2.25), 0.0);
    assertEquals(8.0, interpolate(values, 2.5), 0.0);
    assertEquals(9.0, interpolate(values, 2.75), 0.0);
  }

  @Test
  public void testExceptionOnOutOfRange() {

    SortedMap<Double, Double> values = new TreeMap<Double, Double>();
    values.put(0.0, 0.0);
    values.put(1.0, 2.0);
    values.put(2.0, 6.0);

    try {
      interpolate(values, -0.25, EOutOfRangeStrategy.EXCEPTION);
      fail();
    } catch (IndexOutOfBoundsException ex) {

    }

    assertEquals(0.0, interpolate(values, 0.0), 0.0);
    assertEquals(4.0, interpolate(values, 1.5), 0.0);
    assertEquals(6.0, interpolate(values, 2.0), 0.0);

    try {
      interpolate(values, 2.25, EOutOfRangeStrategy.EXCEPTION);
      fail();
    } catch (IndexOutOfBoundsException ex) {

    }
  }

  @Test
  public void testLastValueOnOutOfRange() {

    SortedMap<Double, Double> values = new TreeMap<Double, Double>();
    values.put(0.0, 0.0);
    values.put(1.0, 2.0);
    values.put(2.0, 6.0);

    assertEquals(0.0,
        interpolate(values, -0.25, EOutOfRangeStrategy.LAST_VALUE), 0.0);
    assertEquals(0.0, interpolate(values, 0.0, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    assertEquals(4.0, interpolate(values, 1.5, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    assertEquals(6.0, interpolate(values, 2.0, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    assertEquals(6.0,
        interpolate(values, 2.25, EOutOfRangeStrategy.LAST_VALUE), 0.0);
  }

  @Test
  public void testInterpolateOnOutOfRange() {

    SortedMap<Double, Double> values = new TreeMap<Double, Double>();
    values.put(0.0, 0.0);
    values.put(1.0, 2.0);
    values.put(2.0, 6.0);

    assertEquals(-1.5,
        interpolate(values, -0.75, EOutOfRangeStrategy.INTERPOLATE), 0.0);
    assertEquals(0.0, interpolate(values, 0.0, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    assertEquals(4.0, interpolate(values, 1.5, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    assertEquals(6.0, interpolate(values, 2.0, EOutOfRangeStrategy.LAST_VALUE),
        0.0);
    assertEquals(7.0,
        interpolate(values, 2.25, EOutOfRangeStrategy.INTERPOLATE), 0.0);
  }

  @Test
  public void testInterpolatePair() {
    assertEquals(10.5, interpolatePair(0.0, 10.0, 4.0, 8.0, -1.0), 0.0);
    assertEquals(10.0, interpolatePair(0.0, 10.0, 4.0, 8.0, 0.0), 0.0);
    assertEquals(9.5, interpolatePair(0.0, 10.0, 4.0, 8.0, 1.0), 0.0);
    assertEquals(9.0, interpolatePair(0.0, 10.0, 4.0, 8.0, 2.0), 0.0);
    assertEquals(8.5, interpolatePair(0.0, 10.0, 4.0, 8.0, 3.0), 0.0);
    assertEquals(8.0, interpolatePair(0.0, 10.0, 4.0, 8.0, 4.0), 0.0);
    assertEquals(7.5, interpolatePair(0.0, 10.0, 4.0, 8.0, 5.0), 0.0);

  }
}
