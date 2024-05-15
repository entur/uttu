package no.entur.uttu.export.messaging.spi;

/**
 * Represents a service which can send a notification to an external system about an export
 */
public interface MessagingService {

  /**
   * Notify about export
   * @param codespace The codespace of the provider
   * @param filename The filename for this export
   */
  void notifyExport(String codespace, String filename);
}
