package solartracer.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
  private final ExecutorService ses;

  /**
   * Constructs a new shutdown hook.
   *
   * @param main the main thread.
   */
  public ShutdownHook(final ExecutorService ses, final Thread main) {
    mainThread = main;
    this.ses = ses;
  }

  @Override
  public final void run() {
    try {
      if (!ses.awaitTermination(Constants.TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS)) {
        LOGGER.error("Application exit requested, but not all tasks are complete.");
        ses.shutdownNow();
      } else {
        LOGGER.debug("Scheduled executor service shut down successfully.");
      }
      if (mainThread.isAlive()) {
        mainThread.join();
      }
    } catch (final InterruptedException e1) {
      LOGGER.error("Interrupted while waiting for application exit.");
      ExceptionUtils.log(getClass(), e1);
      Thread.currentThread().interrupt();
    }
  }
}
