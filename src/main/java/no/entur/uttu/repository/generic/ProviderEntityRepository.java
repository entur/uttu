package no.entur.uttu.repository.generic;

import no.entur.uttu.model.ProviderEntity;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.List;

@NoRepositoryBean
public interface ProviderEntityRepository<T extends ProviderEntity> extends Repository<T, Long> {

    List<T> findAll();

    T getOne(Long id);

    T getOne(String netexId);

    <S extends T> S save(S entity);

    T delete(String netexId);

    void deleteAll();
}
