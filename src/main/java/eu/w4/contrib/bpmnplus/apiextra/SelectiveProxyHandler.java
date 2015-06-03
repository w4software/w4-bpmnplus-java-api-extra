package eu.w4.contrib.bpmnplus.apiextra;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

class SelectiveProxyHandler implements InvocationHandler
{

  private final Map<Method, InvocationHandler> _handlers;
  private final InvocationHandler _defaultHandler;
  private final Object _delegate;

  public SelectiveProxyHandler(final Map<Method, InvocationHandler> handlers)
  {
    this(handlers, null);
  }

  public SelectiveProxyHandler(final Map<Method, InvocationHandler> handlers,
                               final Object delegate)
  {
    _handlers = handlers;
    _defaultHandler = handlers.get(null);
    _delegate = delegate;
  }

  @Override
  public Object invoke(final Object proxy,
                       final Method method,
                       final Object[] args)
    throws Throwable
  {
    final InvocationHandler handler = _handlers.get(method);
    if (handler != null)
    {
      return handler.invoke(proxy, method, args);
    }
    else
    {
      if (_defaultHandler == null)
      {
        return method.invoke(_delegate, args);
      }
      else
      {
        return _defaultHandler.invoke(proxy, method, args);
      }
    }
  }
}
