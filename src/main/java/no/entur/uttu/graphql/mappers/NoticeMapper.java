package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.Notice;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_TEXT;

@Component
public class NoticeMapper extends AbstractProviderEntityMapper<Notice> {

    public NoticeMapper(ProviderRepository providerRepository, ProviderEntityRepository<Notice> entityRepository) {
        super(providerRepository, entityRepository);
    }

    @Override
    protected Notice createNewEntity(ArgumentWrapper input) {
        return new Notice();
    }

    @Override
    protected void populateEntityFromInput(Notice entity, ArgumentWrapper input) {
        input.apply(FIELD_TEXT, entity::setText);
    }
}
