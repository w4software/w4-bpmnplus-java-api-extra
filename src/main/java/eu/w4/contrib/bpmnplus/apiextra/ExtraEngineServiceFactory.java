package eu.w4.contrib.bpmnplus.apiextra;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import eu.w4.common.configuration.ConfigurationParameter;
import eu.w4.common.exception.CheckedException;
import eu.w4.engine.client.eci.service.EciObjectFactory;
import eu.w4.engine.client.service.EngineService;
import eu.w4.engine.client.service.EngineServiceFactory;
import eu.w4.engine.client.service.ObjectFactory;
import static eu.w4.contrib.bpmnplus.apiextra.ExtraConfigurationParameter.*;

public class ExtraEngineServiceFactory
{

  public static EngineService getEngineService(final Map<ConfigurationParameter, String> configurationParameters)
    throws CheckedException, RemoteException
  {
    final EngineService baseEngineService = EngineServiceFactory.getEngineService(configurationParameters);
    final ClassLoader w4ClassLoader = baseEngineService.getClass().getClassLoader();
    final Class<?>[] engineServiceInterfaces = new Class[] { EngineService.class };

    EngineService engineService = baseEngineService;

    final boolean autoRecoveryEnabled = Boolean.parseBoolean(AUTO_RECOVERY.getValue(configurationParameters));
    final boolean factoryCacheEnabled = Boolean.parseBoolean(FACTORY_CACHE.getValue(configurationParameters));

    if (autoRecoveryEnabled)
    {
      final AutoRecoveryProxyHandler.Factory recoveryFactory = new AutoRecoveryProxyHandler.Factory()
      {
        @Override
        public Object build() throws Throwable
        {
          return EngineServiceFactory.getEngineService(configurationParameters);
        }
      };
      final AutoRecoveryProxyHandler handler = new AutoRecoveryProxyHandler(recoveryFactory, baseEngineService);
      engineService = (EngineService) Proxy.newProxyInstance(w4ClassLoader,
                                                             engineServiceInterfaces,
                                                             handler);
    }

    if (factoryCacheEnabled)
    {
      final Map<Method, InvocationHandler> handlerMap = new HashMap<Method, InvocationHandler>();
      try
      {
        final ObjectFactory objectFactory = engineService.getObjectFactory();
        final InvocationHandler objectFactoryProxyHandler = new ResultCachingProxyHandler<ObjectFactory>(objectFactory);
        final ObjectFactory objectFactoryProxy = (ObjectFactory)
            Proxy.newProxyInstance(objectFactory.getClass().getClassLoader(),
                                   new Class[] { ObjectFactory.class },
                                   objectFactoryProxyHandler);
        handlerMap.put(EngineService.class.getMethod("getObjectFactory"),
                       new ConstantProxyHandler(objectFactoryProxy));
      }
      catch (final NoSuchMethodException e)
      {
        // if method does not exist, ignore it
      }
      try
      {
        final EciObjectFactory objectFactory = engineService.getEciObjectFactory();
        final InvocationHandler objectFactoryProxyHandler = new ResultCachingProxyHandler<EciObjectFactory>(objectFactory);
        final EciObjectFactory objectFactoryProxy = (EciObjectFactory)
            Proxy.newProxyInstance(objectFactory.getClass().getClassLoader(),
                                   new Class[] { EciObjectFactory.class },
                                   objectFactoryProxyHandler);
        handlerMap.put(EngineService.class.getMethod("getEciObjectFactory"),
                       new ConstantProxyHandler(objectFactoryProxy));
      }
      catch (final NoSuchMethodException e)
      {
        // if method does not exist, ignore it
      }
      final InvocationHandler rootHandler = new SelectiveProxyHandler(handlerMap, engineService);
      engineService = (EngineService) Proxy.newProxyInstance(w4ClassLoader,
                                                               engineServiceInterfaces,
                                                               rootHandler);
    }

    return engineService;
  }
}
