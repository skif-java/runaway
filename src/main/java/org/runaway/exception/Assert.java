package org.runaway.exception;

//import hit.util.StringUtil;

/**
 * Runtime assertions
 *
 * Assertion utility class that assists in validating arguments.
 * Useful for identifying programmer errors early and clearly at runtime.
 *
 * <p>For example, if the contract of a public method states it does not
 * allow <code>null</code> arguments, Assert can be used to validate that
 * contract. Doing this clearly indicates a contract violation when it
 * occurs and protects the class's invariants.
 *
 * <p>Typically used to validate method arguments rather than configuration
 * properties, to check for cases that are usually programmer errors rather than
 * configuration errors. In contrast to config initialization code, there is
 * usally no point in falling back to defaults in such methods.
 *
 * <p>This class is similar to JUnit's and Spring framework assertion library.
 * If an argument value is deemed invalid, an {@link RunawayException} is thrown (typically).
 */

public class Assert
{

  /**
   * Assert that an object is not <code>null</code> .
   * <pre class="code">Assert.notNull(clazz, "source");</pre>
   * @param object the object to check
   * @param name variable name or expression to use if the assertion fails
   * @throws RunawayException if the object is <code>null</code>
   */
	
  public static void notNull(Object object, String name)
  {
    if (object == null)
    {
      if (name==null || name.trim().isEmpty()) //(StringUtil.isBlank(name))
      {
        name = "object";
      }

      throw new RunawayException(
          String.format("Assert: %s is null", name ));
    }
  }
  

  /**
   * Assert that an object is not <code>null</code> .
   * <pre class="code">Assert.notNull(clazz, "source");</pre>
   * @param string the object to check
   * @param name variable name or expression to use if the assertion fails
   * @throws RunawayException if the object is <code>null</code>
   */

  // TODO swap parameters
  public static void notBlank(String string, String name)
  {
    if (string==null || string.trim().isEmpty()) //(StringUtil.isBlank(string))
    {
      if (name==null || name.trim().isEmpty()) //(StringUtil.isBlank(name))
      {
        name = "string";
      }

      throw new RunawayException(
          String.format("Assert: %s is blank", name ));
    }
  }


  /**
   * Assert a boolean expression, throwing <code>IllegalStateException</code>
   * if the test result is <code>false</code>. Call isTrue if you wish to
   * throw IllegalArgumentException on an assertion failure.
   * <pre class="code">Assert.state(id == null, "The id property must not already be initialized");</pre>
   * @param expression a boolean expression
   * @param message the exception message to use if the assertion fails
   * @throws RunawayException if expression is <code>false</code>
   */

  public static void isTrue(boolean expression, String message)
  {
    if (!expression) {
      throw new RunawayException(message);
    }
  }

  /**
   * Assert a boolean expression, throwing <code>SystemException</code>
   * Call isFailure if you wish to throw SystemException on an assertion that boolean expression is true.
   * <pre class="code">Assert.state(id == null, "The id property must not already be initialized");</pre>
   * @param expression a boolean expression
   * @param message the exception message to use if the assertion fails
   * @throws RunawayException if expression is <code>false</code>
   */

  public static void isFailure(boolean expression, String message)
  {
    if (expression) {
      throw new RunawayException(message);
    }
  } 
}
