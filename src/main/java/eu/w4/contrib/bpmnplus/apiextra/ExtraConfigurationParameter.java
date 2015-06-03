package eu.w4.contrib.bpmnplus.apiextra;

import java.util.Map;

import eu.w4.common.configuration.ConfigurationParameter;

/**
 * Parameters taken in account by {@link ExtraEngineServiceFactory} to
 * configure additionnal feature it provides
 */
public enum ExtraConfigurationParameter implements ConfigurationParameter
{

  /**
   * Enable ability to recover automatically without changing
   * EngineService instance or sub-services instances
   */
  AUTO_RECOVERY("false"),

  /**
   * Cache instances provided by ObjectFactories to save
   * round-trips to server.
   */
  FACTORY_CACHE("false");

  private String _defaultValue;

  private ExtraConfigurationParameter(final String defaultValue)
  {
    _defaultValue = defaultValue;
  }

  @Override
  public String getKey()
  {
    return ExtraConfigurationParameter.class.getName() + "." + name();
  }

  @Override
  public java.util.List<String> getValues()
  {
    return null;
  }

  public String getValue(final Map<ConfigurationParameter, String> configurationParameters)
  {
    final String value = configurationParameters.get(this);
    if (value == null)
    {
      return _defaultValue;
    }
    else
    {
      return value;
    }
  }
}
