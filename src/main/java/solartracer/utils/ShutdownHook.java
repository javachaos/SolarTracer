package solartracer.utils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
  public static final Logger LOGGER = LogManager.getLogger(ShutdownHook.class);

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
        ExceptionUtils.log(getClass(), e1);
        Thread.currentThread().interrupt();
    }
  }
}
