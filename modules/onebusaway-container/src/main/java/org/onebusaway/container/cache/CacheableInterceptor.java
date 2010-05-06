/*
 * Copyright 2009 Brian Ferris
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
package org.onebusaway.container.cache;

import edu.washington.cs.rse.collections.tuple.T2;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
public class CacheableInterceptor {

  private CacheManager _cacheManager;

  private ConcurrentHashMap<String, CacheEntry> _entries = new ConcurrentHashMap<String, CacheEntry>();

  private Map<Class<?>, CacheableObjectKeyFactory> _keyFactories = new HashMap<Class<?>, CacheableObjectKeyFactory>();

  public void setCacheManager(CacheManager cacheManager) {
    _cacheManager = cacheManager;
  }

  public void setCacheKeyFactories(Map<Object, Object> keyFactories) {
    for (Map.Entry<Object, Object> entry : keyFactories.entrySet()) {
      Class<?> className = getObjectAsClass(entry.getKey());
      CacheableObjectKeyFactory keyFactory = getObjectAsObjectKeyFactory(entry.getValue());
      _keyFactories.put(className, keyFactory);
    }
  }

  @Around("@annotation(org.onebusaway.container.cache.Cacheable)")
  public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {

    CacheEntry entry = getCache(pjp);
    CacheableMethodKeyFactory keyFactory = entry.getKeyFactory();
    Cache cache = entry.getCache();
    Serializable key = keyFactory.createKey(pjp);
    Element element = cache.get(key);

    if (element == null) {
      Object retVal = pjp.proceed();
      element = new Element(key, retVal);
      cache.put(element);
    }

    return element.getValue();
  }

  /***************************************************************************
   * Private Methods
   **************************************************************************/

  private CacheEntry getCache(ProceedingJoinPoint pjp) {

    String name = getCacheName(pjp);

    CacheEntry entry = _entries.get(name);

    if (entry == null) {
      CacheableMethodKeyFactory keyFactory = getKeyFactory(pjp);
      Cache cache = _cacheManager.getCache(name);
      if (cache == null) {
        _cacheManager.addCache(name);
        cache = _cacheManager.getCache(name);
      }
      entry = new CacheEntry(keyFactory, cache);
      _entries.put(name, entry);
    }
    return entry;
  }

  private CacheableMethodKeyFactory getKeyFactory(ProceedingJoinPoint pjp) {

    T2<Method, Cacheable> tuple = getCacheableMethodAndAnnotation(pjp);
    if (tuple == null)
      throw new IllegalStateException("no @Cacheable annotation: "
          + pjp.getSignature().toLongString());

    Method m = tuple.getFirst();
    Cacheable c = tuple.getSecond();

    Class<? extends CacheableMethodKeyFactory> keyFactoryType = c.keyFactory();

    if (keyFactoryType.equals(CacheableMethodKeyFactory.class)) {
      Class<?>[] parameters = m.getParameterTypes();
      CacheableObjectKeyFactory[] keyFactories = new CacheableObjectKeyFactory[parameters.length];
      for (int i = 0; i < parameters.length; i++)
        keyFactories[i] = getKeyFactoryForParameterType(parameters[i]);
      return new DefaultCacheableKeyFactory(keyFactories);

    } else {
      try {
        return keyFactoryType.newInstance();
      } catch (Exception ex) {
        throw new IllegalStateException(
            "error instantiating CacheableKeyFactory: "
                + keyFactoryType.getName(), ex);
      }
    }
  }

  private CacheableObjectKeyFactory getKeyFactoryForParameterType(Class<?> type) {

    if (_keyFactories.containsKey(type))
      return _keyFactories.get(type);

    for (Map.Entry<Class<?>, CacheableObjectKeyFactory> entry : _keyFactories.entrySet()) {
      Class<?> argumentType = entry.getKey();
      if (argumentType.isAssignableFrom(type))
        return entry.getValue();
    }

    Class<?> checkType = type;

    while (checkType != null && !checkType.equals(Object.class)) {
      CacheableKey annotation = checkType.getAnnotation(CacheableKey.class);

      if (annotation != null) {
        Class<? extends CacheableObjectKeyFactory> keyFactoryType = annotation.keyFactory();
        try {
          return keyFactoryType.newInstance();
        } catch (Exception ex) {
          throw new IllegalStateException(
              "error instantiating CacheableObjectKeyFactory [type="
                  + keyFactoryType.getName() + "] from CacheableKey [type="
                  + checkType.getName() + "]", ex);
        }
      }
      checkType = type.getSuperclass();
    }

    return new DefaultCacheableObjectKeyFactory();
  }

  private T2<Method, Cacheable> getCacheableMethodAndAnnotation(
      ProceedingJoinPoint pjp) {

    Signature sig = pjp.getSignature();

    Object target = pjp.getTarget();
    Class<?> type = target.getClass();

    for (Method m : type.getDeclaredMethods()) {
      if (!m.getName().equals(sig.getName()))
        continue;

      // if (m.getModifiers() != sig.getModifiers())
      // continue;
      Object[] args = pjp.getArgs();
      Class<?>[] types = m.getParameterTypes();
      if (args.length != types.length)
        continue;
      boolean miss = false;
      for (int i = 0; i < args.length; i++) {
        Object arg = args[i];
        Class<?> argType = types[i];
        if (argType.isPrimitive()) {
          if (argType.equals(Double.TYPE)
              && !arg.getClass().equals(Double.class))
            miss = true;
        } else {
          if (!argType.isInstance(arg))
            miss = true;
        }
      }
      if (miss)
        continue;
      Cacheable c = m.getAnnotation(Cacheable.class);
      if (c != null)
        return T2.create(m, c);
    }
    return null;
  }

  private String getCacheName(ProceedingJoinPoint pjp) {
    Signature sig = pjp.getSignature();
    return sig.getDeclaringTypeName() + "." + sig.getName();
  }

  private Class<?> getObjectAsClass(Object object) {
    if (object instanceof Class)
      return (Class<?>) object;
    if (object instanceof String) {
      try {
        return Class.forName((String) object);
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException(e);
      }
    }
    throw new IllegalArgumentException("unable to convert object to class: "
        + object);
  }

  private CacheableObjectKeyFactory getObjectAsObjectKeyFactory(Object value) {
    if (value instanceof CacheableObjectKeyFactory)
      return (CacheableObjectKeyFactory) value;
    Class<?> classType = getObjectAsClass(value);
    if (!CacheableObjectKeyFactory.class.isAssignableFrom(classType))
      throw new IllegalArgumentException(classType + " is not assignable to "
          + CacheableObjectKeyFactory.class);
    try {
      return (CacheableObjectKeyFactory) classType.newInstance();
    } catch (Exception ex) {
      throw new IllegalStateException("error instantiating " + classType, ex);
    }
  }

  private static class CacheEntry {

    private CacheableMethodKeyFactory _keyFactory;

    private Cache _cache;

    public CacheEntry(CacheableMethodKeyFactory keyFactory, Cache cache) {
      _keyFactory = keyFactory;
      _cache = cache;
    }

    public CacheableMethodKeyFactory getKeyFactory() {
      return _keyFactory;
    }

    public Cache getCache() {
      return _cache;
    }
  }
}