package eu.w4.contrib.bpmnplus.apiextra;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import eu.w4.engine.client.ConnectionFailedException;

class AutoRecoveryProxyHandler implements InvocationHandler
{

  public interface Factory
  {
    Object build() throws Throwable;
  }

  private volatile Object _delegate;
  private Factory _factory;
  private Map<Method, Object> _resultCache;

  public AutoRecoveryProxyHandler(final Factory factory,
                                  final Object delegate)
  {
    _delegate = delegate;
    _factory = factory;
    _resultCache = new ConcurrentHashMap<Method, Object>();
  }

  private Object doInvoke(final Method method,
                          final Object[] args)
    throws Throwable
  {
    Object delegate = _delegate;
    final boolean canRetry = (delegate != null);
    if (delegate == null)
    {
      synchronized (_factory)
      {
        if (_delegate == null)
        {
          _delegate = _factory.build();
        }
        delegate = _delegate;
      }
    }
    try
    {
      return method.invoke(delegate, args);
    }
    catch (final InvocationTargetException invocationTargetException)
    {
      final Throwable targetException = invocationTargetException.getCause();
      try
      {
        throw targetException;
      }
      catch (final java.net.ConnectException e)
      {
        _delegate = null;
        if (canRetry)
        {
          return doInvoke(method, args);
        }
        else
        {
          throw e;
        }
      }
      catch (final java.rmi.ConnectException e)
      {
        _delegate = null;
        if (canRetry)
        {
          return doInvoke(method, args);
        }
        else
        {
          throw e;
        }
      }
      catch (final ConnectionFailedException e)
      {
        _delegate = null;
        if (canRetry)
        {
          return doInvoke(method, args);
        }
        else
        {
          throw e;
        }
      }
    }
  }

  @Override
  public Object invoke(final Object proxy,
                       final Method method,
                       final Object[] args)
    throws Throwable
  {
    final Object cachedResult = _resultCache.get(method);
    if (cachedResult != null)
    {
      return cachedResult;
    }

    final Object result = doInvoke(method, args);

    if (result != null && (args == null || args.length == 0) && (result instanceof Remote))
    {
      final Factory factory = new Factory()
      {
        @Override
        public synchronized Object build() throws Throwable
        {
          return doInvoke(method, args);
        }
      };

      final AutoRecoveryProxyHandler childHandler = new AutoRecoveryProxyHandler(factory,
                                                                                 result);
      Object proxyResult = Proxy.newProxyInstance(result.getClass().getClassLoader(),
                                                  new Class[] { method.getReturnType() },
                                                  childHandler);
      _resultCache.put(method, proxyResult);
      return proxyResult;
    }
    else
    {
      return result;
    }
  }
}
