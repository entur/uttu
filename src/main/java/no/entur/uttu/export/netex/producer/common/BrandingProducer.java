package no.entur.uttu.export.netex.producer.common;

import java.util.List;
import java.util.stream.Collectors;
import no.entur.uttu.export.netex.NetexExportContext;
import org.rutebanken.netex.model.Branding;
import org.rutebanken.netex.model.MultilingualString;
import org.springframework.stereotype.Component;

@Component
public class BrandingProducer {

  public List<Branding> produce(NetexExportContext context) {
    return context.brandings
      .stream()
      .map(branding ->
        new Branding()
          .withId(branding.getNetexId())
          .withVersion("1")
          .withName(new MultilingualString().withValue(branding.getName()))
          .withShortName(
            branding.getShortName() != null
              ? new MultilingualString().withValue(branding.getShortName())
              : null
          )
          .withDescription(
            branding.getDescription() != null
              ? new MultilingualString().withValue(branding.getDescription())
              : null
          )
          .withUrl(branding.getUrl())
          .withImage(branding.getImageUrl())
      )
      .collect(Collectors.toList());
  }
}
