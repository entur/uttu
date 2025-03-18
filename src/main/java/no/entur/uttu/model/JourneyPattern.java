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

package no.entur.uttu.model;

import static no.entur.uttu.model.Constraints.JOURNEY_PATTERN_UNIQUE_NAME;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import no.entur.uttu.util.Preconditions;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(
  uniqueConstraints = {
    @UniqueConstraint(
      name = JOURNEY_PATTERN_UNIQUE_NAME,
      columnNames = { "provider_pk", "name" }
    ),
  }
)
@BatchSize(size = 100)
public class JourneyPattern extends GroupOfEntities_VersionStructure {

  @ManyToOne
  @NotNull
  @Fetch(FetchMode.JOIN)
  private Line line;

  @OneToMany(mappedBy = "journeyPattern", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotNull
  @BatchSize(size = 100)
  @Fetch(FetchMode.SUBSELECT)
  private final List<ServiceJourney> serviceJourneys = new ArrayList<>();

  @OneToMany(mappedBy = "journeyPattern", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotNull
  @OrderBy("order")
  @BatchSize(size = 100)
  @Fetch(FetchMode.SUBSELECT)
  private final List<StopPointInJourneyPattern> pointsInSequence = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  private DirectionTypeEnumeration directionType;

  @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @BatchSize(size = 100)
  @Fetch(FetchMode.SUBSELECT)
  private List<Notice> notices;

  public @NotNull Line getLine() {
    return line;
  }

  public void setLine(Line line) {
    this.line = line;
  }

  public List<ServiceJourney> getServiceJourneys() {
    return serviceJourneys;
  }

  public DirectionTypeEnumeration getDirectionType() {
    return directionType;
  }

  public void setDirectionType(DirectionTypeEnumeration directionType) {
    this.directionType = directionType;
  }

  public List<Notice> getNotices() {
    return notices;
  }

  public void setNotices(List<Notice> notices) {
    this.notices = notices;
  }

  public void setServiceJourneys(List<ServiceJourney> serviceJourneys) {
    this.serviceJourneys.clear();
    if (serviceJourneys != null) {
      serviceJourneys.stream().forEach(sj -> sj.setJourneyPattern(this));
      this.serviceJourneys.addAll(serviceJourneys);
    }
  }

  public List<StopPointInJourneyPattern> getPointsInSequence() {
    return pointsInSequence;
  }

  public void setPointsInSequence(List<StopPointInJourneyPattern> pointsInSequence) {
    this.pointsInSequence.clear();
    if (pointsInSequence != null) {
      int i = 1;
      for (StopPointInJourneyPattern sp : pointsInSequence) {
        sp.setOrder(i++);
        sp.setJourneyPattern(this);
      }
      this.pointsInSequence.addAll(pointsInSequence);
    }
  }

  @Override
  public boolean isValid(LocalDate from, LocalDate to) {
    return (
      super.isValid(from, to) &&
      getServiceJourneys().stream().anyMatch(e -> e.isValid(from, to))
    );
  }

  @Override
  public void checkPersistable() {
    super.checkPersistable();

    Preconditions.checkArgument(
      getPointsInSequence().size() >= 2,
      CodedError.fromErrorCode(ErrorCodeEnumeration.MINIMUM_POINTS_IN_SEQUENCE),
      "%s does not have minimum of 2 pointsInSequence",
      identity()
    );

    getPointsInSequence().stream().forEach(ProviderEntity::checkPersistable);

    Preconditions.checkArgument(
      getPointsInSequence().get(0).getDestinationDisplay() != null,
      "%s is missing destinationDisplay for first pointsInSequence",
      identity()
    );

    Preconditions.checkArgument(
      !Boolean.FALSE.equals(getPointsInSequence().get(0).getForBoarding()),
      "%s does not permit boarding on first pointsInSequence",
      identity()
    );

    Preconditions.checkArgument(
      !Boolean.FALSE.equals(
        getPointsInSequence().get(getPointsInSequence().size() - 1).getForAlighting()
      ),
      "%s does not permit alighting on last pointsInSequence",
      identity()
    );

    Preconditions.checkArgument(
      getPointsInSequence()
        .get(getPointsInSequence().size() - 1)
        .getDestinationDisplay() ==
      null,
      "%s has destinationDisplay for last pointsInSequence",
      identity()
    );

    getServiceJourneys().stream().forEach(ProviderEntity::checkPersistable);
    if (getNotices() != null) {
      getNotices().stream().forEach(IdentifiedEntity::checkPersistable);
    }
  }
}
