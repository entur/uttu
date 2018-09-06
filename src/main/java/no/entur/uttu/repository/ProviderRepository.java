package no.entur.uttu.repository;

import no.entur.uttu.model.Provider;

import java.util.List;

public interface ProviderRepository {
    Provider getOne(String code);

    Provider getOne(Long id);

    List<Provider> findAll();

    <S extends Provider> S save(S entity);

}