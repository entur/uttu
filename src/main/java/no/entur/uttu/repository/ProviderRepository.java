package no.entur.uttu.repository;

import no.entur.uttu.model.Provider;

public interface ProviderRepository {
    Provider getOne(Long id);
}