package no.entur.uttu.organisation.netex;

public class UnsupportedOrganisationTypeException extends RuntimeException {

  public UnsupportedOrganisationTypeException(Class<?> organisationType) {
    super("Unsupported organisation type: " + organisationType);
  }
}
