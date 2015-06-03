package eu.w4.contrib.bpmnplus.apiextra;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ResultCachingProxyHandler<E> implements InvocationHandler
{
  public class ContextObjectInputStream extends ObjectInputStream
  {
    private ClassLoader _classloader;

    public ContextObjectInputStream(final InputStream in,
                                    final ClassLoader classLoader)
      throws IOException
    {
        super(in);
        this._classloader = classLoader;
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass desc)
      throws IOException, ClassNotFoundException
    {
      try
      {
        return Class.forName(desc.getName(), false, _classloader);
      }
      catch (ClassNotFoundException ex)
      {
        return super.resolveClass(desc);
      }
    }
  }

  private E _delegate;
  private Map<String, byte[]> _cache = new ConcurrentHashMap<String, byte[]>();

  public ResultCachingProxyHandler(E delegate)
  {
    _delegate = delegate;
  }

  private void save(final String key, final Object object)
    throws IOException
  {
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    final ObjectOutputStream objectOutput = new ObjectOutputStream(output);
    objectOutput.writeObject(object);
    objectOutput.flush();
    objectOutput.close();
    _cache.put(key, output.toByteArray());
  }

  private Object newCopy(final String key, final ClassLoader cl)
    throws ClassNotFoundException, IOException
  {
    byte[] copy = _cache.get(key);
    final ObjectInputStream stream = new ContextObjectInputStream(new ByteArrayInputStream(copy), cl);
    try
    {
      return stream.readObject();
    }
    finally
    {
      stream.close();
    }
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args)
    throws Throwable
  {
    final String cacheKey = method.getName();
    if (!_cache.containsKey(cacheKey))
    {
      final Object result = method.invoke(_delegate, args);
      save(cacheKey, result);
    }
    return newCopy(cacheKey, proxy.getClass().getClassLoader());
  }

}
