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

import com.google.common.base.Preconditions;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "flexible_lines_unique_name_constraint", columnNames = {"provider_pk", "name"})})
public class FlexibleLine extends GroupOfEntities_VersionStructure {

    private String publicCode;

    @Enumerated(EnumType.STRING)
    @NotNull
    private VehicleModeEnumeration transportMode;

    @Enumerated(EnumType.STRING)
    @NotNull
    private VehicleSubmodeEnumeration transportSubmode;

    @Enumerated(EnumType.STRING)
    @NotNull
    private FlexibleLineTypeEnumeration flexibleLineType;

    @NotNull
    @ManyToOne
    private Network network;

    private Long operatorRef;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Notice> notices;

    @OneToOne(cascade = CascadeType.ALL)
    private BookingArrangement bookingArrangement;

    @OneToMany(mappedBy = "flexibleLine", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<JourneyPattern> journeyPatterns = new ArrayList<>();

    public String getPublicCode() {
        return publicCode;
    }

    public void setPublicCode(String publicCode) {
        this.publicCode = publicCode;
    }

    public VehicleModeEnumeration getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(VehicleModeEnumeration transportMode) {
        this.transportMode = transportMode;
    }

    public FlexibleLineTypeEnumeration getFlexibleLineType() {
        return flexibleLineType;
    }

    public void setFlexibleLineType(FlexibleLineTypeEnumeration flexibleLineType) {
        this.flexibleLineType = flexibleLineType;
    }

    public List<JourneyPattern> getJourneyPatterns() {
        return journeyPatterns;
    }

    public void setJourneyPatterns(List<JourneyPattern> journeyPatterns) {
        this.journeyPatterns.clear();
        if (journeyPatterns != null) {
            journeyPatterns.stream().forEach(jp -> jp.setFlexibleLine(this));
            this.journeyPatterns.addAll(journeyPatterns);
        }
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Long getOperatorRef() {
        return operatorRef;
    }

    public void setOperatorRef(Long operatorRef) {
        this.operatorRef = operatorRef;
    }

    public List<Notice> getNotices() {
        return notices;
    }

    public void setNotices(List<Notice> notices) {
        this.notices = notices;
    }

    public BookingArrangement getBookingArrangement() {
        return bookingArrangement;
    }

    public void setBookingArrangement(BookingArrangement bookingArrangement) {
        this.bookingArrangement = bookingArrangement;
    }

    public VehicleSubmodeEnumeration getTransportSubmode() {
        return transportSubmode;
    }

    public void setTransportSubmode(VehicleSubmodeEnumeration transportSubmode) {
        this.transportSubmode = transportSubmode;
    }

    @Override
    public boolean isValid(LocalDate from, LocalDate to) {
        return super.isValid(from, to) && getJourneyPatterns().stream().anyMatch(e -> e.isValid(from, to));
    }

    @Override
    public void checkPersistable() {
        super.checkPersistable();

        Preconditions.checkArgument(Objects.equals(transportMode, transportSubmode.getVehicleMode()), "%s transportSubmode %s is valid for transportMode %s", identity(), transportSubmode.value(), transportMode.value());

        getJourneyPatterns().stream().forEach(ProviderEntity::checkPersistable);
    }
}
