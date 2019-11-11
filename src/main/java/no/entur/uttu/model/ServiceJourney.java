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

import no.entur.uttu.error.ErrorCodeEnumeration;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.util.Preconditions;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static no.entur.uttu.model.Constraints.SERVICE_JOURNEY_UNIQUE_NAME;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = SERVICE_JOURNEY_UNIQUE_NAME, columnNames = {"provider_pk", "name"})})
public class ServiceJourney extends GroupOfEntities_VersionStructure {

    private String publicCode;

    private String operatorRef;

    @OneToOne(cascade = CascadeType.ALL)
    private BookingArrangement bookingArrangement;

    @NotNull
    @ManyToOne
    private JourneyPattern journeyPattern;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private final List<DayType> dayTypes = new ArrayList<>();


    @OneToMany(mappedBy = "serviceJourney", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    @OrderBy("order")
    private final List<TimetabledPassingTime> passingTimes = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Notice> notices;


    public JourneyPattern getJourneyPattern() {
        return journeyPattern;
    }

    public void setJourneyPattern(JourneyPattern journeyPattern) {
        this.journeyPattern = journeyPattern;
    }

    public List<TimetabledPassingTime> getPassingTimes() {
        return passingTimes;
    }

    public void setPassingTimes(List<TimetabledPassingTime> passingTimes) {
        this.passingTimes.clear();
        if (passingTimes != null) {
            int i = 1;
            for (TimetabledPassingTime passingTime : passingTimes) {
                passingTime.setOrder(i++);
                passingTime.setServiceJourney(this);
            }
            this.passingTimes.addAll(passingTimes);
        }
    }

    public String getPublicCode() {
        return publicCode;
    }

    public void setPublicCode(String publicCode) {
        this.publicCode = publicCode;
    }

    public String getOperatorRef() {
        return operatorRef;
    }

    public void setOperatorRef(String operatorRef) {
        this.operatorRef = operatorRef;
    }

    public BookingArrangement getBookingArrangement() {
        return bookingArrangement;
    }

    public void setBookingArrangement(BookingArrangement bookingArrangement) {
        this.bookingArrangement = bookingArrangement;
    }

    public List<DayType> getDayTypes() {
        return dayTypes;
    }

    public void updateDayTypes(List<DayType> dayTypes) {
        this.dayTypes.clear();
        if (dayTypes != null) {
            this.dayTypes.addAll(dayTypes);
        }
    }

    public List<Notice> getNotices() {
        return notices;
    }

    public void setNotices(List<Notice> notices) {
        this.notices = notices;
    }


    @Override
    public boolean isValid(LocalDate from, LocalDate to) {
        return super.isValid(from, to) && getDayTypes().stream().anyMatch(e -> e.isValid(from, to));
    }

    @Override
    public void checkPersistable() {
        super.checkPersistable();
        Preconditions.checkArgument(getJourneyPattern().getPointsInSequence().size() == getPassingTimes().size(),
                "%s must have same no of passingTimes as corresponding %s has no of pointsInSequence", identity(), journeyPattern.identity());

        TimetabledPassingTime prevPassingTime = null;
        for (TimetabledPassingTime timetabledPassingTime : getPassingTimes()) {
            timetabledPassingTime.checkPersistable();

            if (prevPassingTime == null) {
                Preconditions.checkArgument(timetabledPassingTime.getDepartureTime() != null || timetabledPassingTime.getEarliestDepartureTime() != null,
                        "%s First TimetablePassingTime in ServiceJourney %s must have at least one of the following fields set: departureTime, earliestDepartureTime", timetabledPassingTime.identity(), this.identity());
            } else {
                prevPassingTime.checkBeforeOther(timetabledPassingTime);

            }
            prevPassingTime = timetabledPassingTime;
        }
        Preconditions.checkArgument(prevPassingTime.getArrivalTime() != null || prevPassingTime.getLatestArrivalTime() != null,
                "%s Last TimetablePassingTime in ServiceJourney %s must have at least one of the following fields set: arrivalTime, latestArrivalTime", prevPassingTime.identity(), this.identity());

        Preconditions.checkArgument(operatorRef != null || getJourneyPattern().getFlexibleLine().getOperatorRef() != null,
                CodedError.fromErrorCode(ErrorCodeEnumeration.MISSING_OPERATOR),
                "%s has operator set on neither ServiceJourney or FlexibleLine", identity());

        getDayTypes().stream().forEach(IdentifiedEntity::checkPersistable);

    }


}
