/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.graphql.mappers;

import static no.entur.uttu.graphql.GraphQLNames.*;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.TimetabledPassingTime;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

@Component
public class TimetabledPassingTimeMapper
  extends AbstractProviderEntityMapper<TimetabledPassingTime> {

  private NoticeMapper noticeMapper;

  public TimetabledPassingTimeMapper(
    ProviderRepository providerRepository,
    ProviderEntityRepository<TimetabledPassingTime> entityRepository,
    NoticeMapper noticeMapper
  ) {
    super(providerRepository, entityRepository);
    this.noticeMapper = noticeMapper;
  }

  @Override
  protected TimetabledPassingTime createNewEntity(ArgumentWrapper input) {
    return new TimetabledPassingTime();
  }

  @Override
  protected void populateEntityFromInput(
    TimetabledPassingTime entity,
    ArgumentWrapper input
  ) {
    input.apply(FIELD_ARRIVAL_DAY_OFFSET, entity::setArrivalDayOffset);
    input.apply(FIELD_ARRIVAL_TIME, entity::setArrivalTime);
    input.apply(FIELD_DEPARTURE_DAY_OFFSET, entity::setDepartureDayOffset);
    input.apply(FIELD_DEPARTURE_TIME, entity::setDepartureTime);
    input.apply(FIELD_LATEST_ARRIVAL_DAY_OFFSET, entity::setLatestArrivalDayOffset);
    input.apply(FIELD_LATEST_ARRIVAL_TIME, entity::setLatestArrivalTime);
    input.apply(
      FIELD_EARLIEST_DEPARTURE_DAY_OFFSET,
      entity::setEarliestDepartureDayOffset
    );
    input.apply(FIELD_EARLIEST_DEPARTURE_TIME, entity::setEarliestDepartureTime);
    input.applyList(FIELD_NOTICES, noticeMapper::mapList, entity::setNotices);
  }
}
