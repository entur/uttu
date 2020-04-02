package no.entur.uttu.model;

import no.entur.uttu.util.Preconditions;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static no.entur.uttu.model.Constraints.LINE_UNIQUE_NAME;

@Entity
@Inheritance(strategy= InheritanceType.JOINED)
@Table(uniqueConstraints = {@UniqueConstraint(name = LINE_UNIQUE_NAME, columnNames = {"provider_pk", "name"})})
public abstract class Line extends GroupOfEntities_VersionStructure {

    private String publicCode;

    @Enumerated(EnumType.STRING)
    @NotNull
    private VehicleModeEnumeration transportMode;

    @Enumerated(EnumType.STRING)
    @NotNull
    private VehicleSubmodeEnumeration transportSubmode;

    @NotNull
    @ManyToOne
    private Network network;

    private String operatorRef;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Notice> notices;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public List<JourneyPattern> getJourneyPatterns() {
        return journeyPatterns;
    }

    public void setJourneyPatterns(List<JourneyPattern> journeyPatterns) {
        this.journeyPatterns.clear();
        if (journeyPatterns != null) {
            journeyPatterns.stream().forEach(jp -> jp.setLine(this));
            this.journeyPatterns.addAll(journeyPatterns);
        }
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

        Preconditions.checkArgument(transportMode != null, "% transportMode not set", identity());
        Preconditions.checkArgument(transportSubmode != null, "% transportSubmode not set", identity());
        Preconditions.checkArgument(Objects.equals(transportMode, transportSubmode.getVehicleMode()), "%s transportSubmode %s is valid for transportMode %s", identity(), transportSubmode.value(), transportMode.value());

        getJourneyPatterns().stream().forEach(ProviderEntity::checkPersistable);
    }
}
