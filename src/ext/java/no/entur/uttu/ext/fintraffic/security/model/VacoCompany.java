package no.entur.uttu.ext.fintraffic.security.model;

import java.util.List;

public record VacoCompany(
  String businessId,
  String name,
  String language,
  List<String> codespaces
) {}
