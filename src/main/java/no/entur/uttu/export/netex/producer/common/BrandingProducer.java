package no.entur.uttu.export.netex.producer.common;

import java.util.List;
import java.util.stream.Collectors;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
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
          .withVersion(branding.getVersion().toString())
          .withName(new MultilingualString().withValue(branding.getName())) // TODO lang?
          .withShortName(new MultilingualString().withValue(branding.getShortName())) // TODO lang?
          .withDescription(new MultilingualString().withValue(branding.getDescription()))
          .withUrl(branding.getUrl())
          .withImage(branding.getImageUrl())
      )
      .collect(Collectors.toList());
  }
}
