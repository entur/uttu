package no.entur.uttu.model;

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
import java.util.List;

@Entity
@Table(
        uniqueConstraints = {
                                    @UniqueConstraint(name = "flexible_lines_unique_name_constraint", columnNames = {"provider_pk", "name"})}
)
public class FlexibleLine extends GroupOfEntities_VersionStructure {

    @NotNull
    private String publicCode;

    @Enumerated(EnumType.STRING)
    @NotNull
    private VehicleModeEnumeration transportMode;


    // TODO submode

    @Enumerated(EnumType.STRING)
    @NotNull
    private FlexibleLineTypeEnumeration flexibleLineTypeEnumeration;

    @NotNull
    @ManyToOne
    private Network network;

    private String operatorRef;

    @ManyToMany
    private List<Notice> notices;

    @OneToOne
    private BookingArrangement bookingArrangement;

    @OneToMany(mappedBy = "flexibleLine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JourneyPattern> journeyPatterns;

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

    public FlexibleLineTypeEnumeration getFlexibleLineTypeEnumeration() {
        return flexibleLineTypeEnumeration;
    }

    public void setFlexibleLineTypeEnumeration(FlexibleLineTypeEnumeration flexibleLineTypeEnumeration) {
        this.flexibleLineTypeEnumeration = flexibleLineTypeEnumeration;
    }

    public List<JourneyPattern> getJourneyPatterns() {
        return journeyPatterns;
    }

    public void setJourneyPatterns(List<JourneyPattern> journeyPatterns) {
        this.journeyPatterns = journeyPatterns;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public String getOperatorRef() {
        return operatorRef;
    }

    public void setOperatorRef(String operatorRef) {
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
}
