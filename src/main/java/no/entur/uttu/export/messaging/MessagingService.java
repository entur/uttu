package no.entur.uttu.export.messaging;

public interface MessagingService {
  void notifyExport(String codespace, String filename);
}
