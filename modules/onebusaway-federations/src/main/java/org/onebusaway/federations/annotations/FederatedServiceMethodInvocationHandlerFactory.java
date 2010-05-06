package org.onebusaway.federations.annotations;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class FederatedServiceMethodInvocationHandlerFactory {

  public FederatedServiceMethodInvocationHandler getHandlerForMethod(
      Method method) {

    FederatedByAgencyIdMethod ann0 = method.getAnnotation(FederatedByAgencyIdMethod.class);
    if (ann0 != null)
      return new FederatedByAgencyIdMethodInvocationHandlerImpl(ann0.argument());

    FederatedByAgencyIdsMethod ann1 = method.getAnnotation(FederatedByAgencyIdsMethod.class);
    if (ann1 != null)
      return new FederatedByAgencyIdsMethodInvocationHandlerImpl(
          ann1.argument());

    FederatedByBoundsMethod ann2 = method.getAnnotation(FederatedByBoundsMethod.class);
    if (ann2 != null)
      return new FederatedByBoundsMethodInvocationHandlerImpl(
          ann2.lat1Argument(), ann2.lon1Argument(), ann2.lat2Argument(),
          ann2.lon2Argument());

    FederatedByLocationMethod ann3 = method.getAnnotation(FederatedByLocationMethod.class);
    if (ann3 != null)
      return new FederatedByLocationMethodInvocationHandlerImpl(
          ann3.latArgument(), ann3.lonArgument());

    FederatedByAggregateMethod ann4 = method.getAnnotation(FederatedByAggregateMethod.class);
    if (ann4 != null) {
      EMethodAggregationType aggregationType = getAggregationTypeForMethod(method);
      return new FederatedByAggregateMethodInvocationHandlerImpl(
          aggregationType);
    }

    FederatedByCoordinateBoundsMethod ann5 = method.getAnnotation(FederatedByCoordinateBoundsMethod.class);
    if (ann5 != null)
      return new FederatedByCoordinateBoundsMethodInvocationHandlerImpl(method,
          ann5.argument(), ann5.propertyExpression());

    throw new IllegalArgumentException(
        "No FederatedService method annotation for method: " + method);
  }

  private EMethodAggregationType getAggregationTypeForMethod(Method method) {
    Class<?> returnType = method.getReturnType();
    if (List.class.isAssignableFrom(returnType))
      return EMethodAggregationType.LIST;
    if (Map.class.isAssignableFrom(returnType))
      return EMethodAggregationType.MAP;
    throw new IllegalArgumentException("unsupported aggregation type: "
        + returnType.getName());
  }

}
