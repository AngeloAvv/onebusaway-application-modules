package org.onebusaway.federations.annotations;

import static org.junit.Assert.assertEquals;

import org.onebusaway.federations.CoordinateBoundsTestBean;
import org.onebusaway.federations.SimpleFederatedService;
import org.onebusaway.geospatial.model.CoordinateBounds;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Set;

public class FederatedServiceMethodInvocationHandlerFactoryTest {

  private FederatedServiceMethodInvocationHandlerFactory _factory = new FederatedServiceMethodInvocationHandlerFactory();

  @Test
  public void testGetValuesAsList() throws SecurityException, NoSuchMethodException {
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValuesAsList");
    FederatedServiceMethodInvocationHandler handler = _factory.getHandlerForMethod(method);
    FederatedByAggregateMethodInvocationHandlerImpl impl = (FederatedByAggregateMethodInvocationHandlerImpl) handler;
    assertEquals(EMethodAggregationType.LIST, impl.getAggregationType());
  }

  @Test
  public void testGetValuesAsMap() throws SecurityException, NoSuchMethodException {
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValuesAsMap");
    FederatedServiceMethodInvocationHandler handler = _factory.getHandlerForMethod(method);
    FederatedByAggregateMethodInvocationHandlerImpl impl = (FederatedByAggregateMethodInvocationHandlerImpl) handler;
    assertEquals(EMethodAggregationType.MAP, impl.getAggregationType());
  }

  @Test
  public void testGetValueForId() throws SecurityException, NoSuchMethodException {
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForId", String.class);
    FederatedServiceMethodInvocationHandler handler = _factory.getHandlerForMethod(method);
    FederatedByAgencyIdMethodInvocationHandlerImpl impl = (FederatedByAgencyIdMethodInvocationHandlerImpl) handler;
    assertEquals(0, impl.getArgumentIndex());
  }

  @Test
  public void testGetValueForValueAndId() throws SecurityException, NoSuchMethodException {
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForValueAndId", String.class, String.class);
    FederatedServiceMethodInvocationHandler handler = _factory.getHandlerForMethod(method);
    FederatedByAgencyIdMethodInvocationHandlerImpl impl = (FederatedByAgencyIdMethodInvocationHandlerImpl) handler;
    assertEquals(1, impl.getArgumentIndex());
  }

  @Test
  public void testGetValueForIds() throws SecurityException, NoSuchMethodException {
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForIds", Set.class);
    FederatedServiceMethodInvocationHandler handler = _factory.getHandlerForMethod(method);
    FederatedByAgencyIdsMethodInvocationHandlerImpl impl = (FederatedByAgencyIdsMethodInvocationHandlerImpl) handler;
    assertEquals(0, impl.getArgumentIndex());
  }

  @Test
  public void testGetValueForValueAndIds() throws SecurityException, NoSuchMethodException {
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForValueAndIds", String.class, Set.class);
    FederatedServiceMethodInvocationHandler handler = _factory.getHandlerForMethod(method);
    FederatedByAgencyIdsMethodInvocationHandlerImpl impl = (FederatedByAgencyIdsMethodInvocationHandlerImpl) handler;
    assertEquals(1, impl.getArgumentIndex());
  }

  @Test
  public void testGetValueForBounds() throws SecurityException, NoSuchMethodException {
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForBounds", Double.TYPE, Double.TYPE,
        Double.TYPE, Double.TYPE);
    FederatedServiceMethodInvocationHandler handler = _factory.getHandlerForMethod(method);
    FederatedByBoundsMethodInvocationHandlerImpl impl = (FederatedByBoundsMethodInvocationHandlerImpl) handler;
    assertEquals(0, impl.getLat1ArgumentIndex());
    assertEquals(1, impl.getLon1ArgumentIndex());
    assertEquals(2, impl.getLat2ArgumentIndex());
    assertEquals(3, impl.getLon2ArgumentIndex());
  }
  
  @Test
  public void testGetValueForCoordinateBounds() throws SecurityException, NoSuchMethodException {
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForCoordinateBounds", CoordinateBounds.class);
    FederatedServiceMethodInvocationHandler handler = _factory.getHandlerForMethod(method);
    FederatedByCoordinateBoundsMethodInvocationHandlerImpl impl = (FederatedByCoordinateBoundsMethodInvocationHandlerImpl) handler;
    assertEquals(0, impl.getArgumentIndex());
    assertEquals(0,impl.getPropertyReaders().length);
  }
  
  @Test
  public void testGetValueForCoordinateBoundsTestBean() throws SecurityException, NoSuchMethodException {
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForCoordinateBoundsTestBean", CoordinateBoundsTestBean.class);
    FederatedServiceMethodInvocationHandler handler = _factory.getHandlerForMethod(method);
    FederatedByCoordinateBoundsMethodInvocationHandlerImpl impl = (FederatedByCoordinateBoundsMethodInvocationHandlerImpl) handler;
    assertEquals(0, impl.getArgumentIndex());
    Method[] propertyReaders = impl.getPropertyReaders();
    assertEquals(1,propertyReaders.length);
    assertEquals("getBounds",propertyReaders[0].getName());
  }
  

  
  @Test
  public void testGetValueForLocation() throws SecurityException, NoSuchMethodException {
    Method method = SimpleFederatedService.class.getDeclaredMethod("getValueForLocation", Double.TYPE, Double.TYPE);
    FederatedServiceMethodInvocationHandler handler = _factory.getHandlerForMethod(method);
    FederatedByLocationMethodInvocationHandlerImpl impl = (FederatedByLocationMethodInvocationHandlerImpl) handler;
    assertEquals(0, impl.getLatArgumentIndex());
    assertEquals(1, impl.getLonArgumentIndex());
  }
}
