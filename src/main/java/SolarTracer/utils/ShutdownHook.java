package SolarTracer.utils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shutdown hook.
 *
 * @author fred
 *
 */
public class ShutdownHook extends Thread {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHook.class);

  /**
   * Main thread reference.
   */
  private final Thread mainThread;

  /**
   * Executor service.
   */
  private final ScheduledExecutorService ses;

  /**
   * Constructs a new shutdown hook.
   *
   * @param main the main thread.
   */
  public ShutdownHook(final ScheduledExecutorService ses, final Thread main) {
    mainThread = main;
    this.ses = ses;
  }

  @Override
  public final void run() {
    try {
      ses.awaitTermination(Constants.TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS);
      mainThread.join();
    } catch (final InterruptedException e1) {
      LOGGER.error(e1.getMessage());
    }
  }
}
