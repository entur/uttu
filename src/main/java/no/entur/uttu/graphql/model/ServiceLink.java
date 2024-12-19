package no.entur.uttu.graphql.model;

import no.entur.uttu.routing.RouteGeometry;

public record ServiceLink(
  String serviceLinkRef,
  String quayRefFrom,
  String quayRefTo,
  RouteGeometry routeGeometry
) {}
