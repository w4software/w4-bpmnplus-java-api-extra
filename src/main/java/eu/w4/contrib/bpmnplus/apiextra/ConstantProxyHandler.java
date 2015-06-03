package eu.w4.contrib.bpmnplus.apiextra;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class ConstantProxyHandler implements InvocationHandler
{
  private Object _constantResult;

  public ConstantProxyHandler(final Object constantResult)
  {
    _constantResult = constantResult;
  }

  @Override
  public Object invoke(final Object proxy,
                       final Method method,
                       final Object[] args)
    throws Throwable
  {
    return _constantResult;
  }
}
