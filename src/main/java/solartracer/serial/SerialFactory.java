package solartracer.serial;

/**
 * Defines a serial creation factory.
 */
public class SerialFactory {

  /**
   * private Serial factory constructor 
   */
  private SerialFactory() {}

  /**
   * Create a new Serial Instance.
   *
   * @return the newly created serial instance.
   */
  public static SerialConnection getSerial() {
    return new SerialCommImpl();
  }
}
