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

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;
import no.entur.uttu.util.Preconditions;
import no.entur.uttu.util.ValidationHelper;
import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;
import org.rutebanken.netex.model.SimplePoint_VersionStructure;

@Entity
public class TimetabledPassingTime extends ProviderEntity {

  @NotNull
  @ManyToOne
  private ServiceJourney serviceJourney;

  // Order is reserved word in db
  @Column(name = "order_val")
  @Min(value = 1L, message = "The value must be positive")
  private int order;

  @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  private List<Notice> notices;

  private LocalTime departureTime;

  private LocalTime arrivalTime;

  private LocalTime earliestDepartureTime;

  private LocalTime latestArrivalTime;

  private int arrivalDayOffset;

  private int departureDayOffset;

  private int earliestDepartureDayOffset;

  private int latestArrivalDayOffset;

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public LocalTime getEarliestDepartureTime() {
    return earliestDepartureTime;
  }

  public void setEarliestDepartureTime(LocalTime earliestDepartureTime) {
    this.earliestDepartureTime = earliestDepartureTime;
  }

  public LocalTime getLatestArrivalTime() {
    return latestArrivalTime;
  }

  public void setLatestArrivalTime(LocalTime latestArrivalTime) {
    this.latestArrivalTime = latestArrivalTime;
  }

  public ServiceJourney getServiceJourney() {
    return serviceJourney;
  }

  public void setServiceJourney(ServiceJourney serviceJourney) {
    this.serviceJourney = serviceJourney;
  }

  public LocalTime getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(LocalTime departureTime) {
    this.departureTime = departureTime;
  }

  public LocalTime getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(LocalTime arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public int getArrivalDayOffset() {
    return arrivalDayOffset;
  }

  public void setArrivalDayOffset(int arrivalDayOffset) {
    this.arrivalDayOffset = arrivalDayOffset;
  }

  public int getDepartureDayOffset() {
    return departureDayOffset;
  }

  public void setDepartureDayOffset(int departureDayOffset) {
    this.departureDayOffset = departureDayOffset;
  }

  public int getEarliestDepartureDayOffset() {
    return earliestDepartureDayOffset;
  }

  public void setEarliestDepartureDayOffset(int earliestDepartureDayOffset) {
    this.earliestDepartureDayOffset = earliestDepartureDayOffset;
  }

  public int getLatestArrivalDayOffset() {
    return latestArrivalDayOffset;
  }

  public void setLatestArrivalDayOffset(int latestArrivalDayOffset) {
    this.latestArrivalDayOffset = latestArrivalDayOffset;
  }

  public TimetabledPassingTime withEarliestDepartureTime(
    LocalTime earliestDepartureTime
  ) {
    this.earliestDepartureTime = earliestDepartureTime;
    return this;
  }

  public TimetabledPassingTime withLatestArrivalTime(LocalTime latestArrivalTime) {
    this.latestArrivalTime = latestArrivalTime;
    return this;
  }

  public TimetabledPassingTime withDepartureTime(LocalTime departureTime) {
    this.departureTime = departureTime;
    return this;
  }

  public TimetabledPassingTime withArrivalTime(LocalTime arrivalTime) {
    this.arrivalTime = arrivalTime;
    return this;
  }

  public TimetabledPassingTime withArrivalDayOffset(int arrivalDayOffset) {
    this.arrivalDayOffset = arrivalDayOffset;
    return this;
  }

  public TimetabledPassingTime withDepartureDayOffset(int departureDayOffset) {
    this.departureDayOffset = departureDayOffset;
    return this;
  }

  public TimetabledPassingTime withEarliestDepartureDayOffset(
    int earliestDepartureDayOffset
  ) {
    this.earliestDepartureDayOffset = earliestDepartureDayOffset;
    return this;
  }

  public TimetabledPassingTime withLatestArrivalDayOffset(int latestArrivalDayOffset) {
    this.latestArrivalDayOffset = latestArrivalDayOffset;
    return this;
  }

  public List<Notice> getNotices() {
    return notices;
  }

  public void setNotices(List<Notice> notices) {
    this.notices = notices;
  }

  @Override
  public void checkPersistable() {
    super.checkPersistable();
    Preconditions.checkArgument(
      departureTime != null ||
      arrivalTime != null ||
      earliestDepartureTime != null ||
      latestArrivalTime != null,
      "%s must have at least one of the following fields set: arrivalTime, latestArrivalTime, departureTime or earliestDepartureTime",
      identity()
    );

    Preconditions.checkArgument(
      ValidationHelper.isNotAfter(
        arrivalTime,
        arrivalDayOffset,
        departureTime,
        departureDayOffset
      ),
      "%s arrivalTime cannot be later than departureTime",
      identity()
    );
    Preconditions.checkArgument(
      ValidationHelper.isNotAfter(
        earliestDepartureTime,
        earliestDepartureDayOffset,
        departureTime,
        departureDayOffset
      ),
      "%s earliestDepartureTime cannot be later than departureTime",
      identity()
    );
    Preconditions.checkArgument(
      ValidationHelper.isNotAfter(
        arrivalTime,
        arrivalDayOffset,
        latestArrivalTime,
        latestArrivalDayOffset
      ),
      "%s arrivalTime cannot be later than latestArrivalTime",
      identity()
    );
    Preconditions.checkArgument(
      ValidationHelper.isNotSame(
        arrivalTime,
        arrivalDayOffset,
        departureTime,
        departureDayOffset
      ),
      "%s arrivalTime and departureTime cannot be the same, use departureTime only",
      identity()
    );
  }

  /**
   * Check that this TimetabledPassingTime fits chronologically before another TimetablePassingTime in a ServiceJourney.
   *
   * @param other the other TimetabledPassingTime
   */
  public void checkBeforeOther(TimetabledPassingTime other) {
    // Validate regular passing times
    Preconditions.checkArgument(
      ValidationHelper.isNotAfter(
        departureTime,
        departureDayOffset,
        other.departureTime,
        other.departureDayOffset
      ),
      "%s departureTime cannot be after next elements (%s) departureTime",
      identity(),
      other.identity()
    );
    Preconditions.checkArgument(
      ValidationHelper.isNotAfter(
        departureTime,
        departureDayOffset,
        other.arrivalTime,
        other.arrivalDayOffset
      ),
      "%s departureTime cannot be after next elements (%s) arrivalTime",
      identity(),
      other.identity()
    );
    Preconditions.checkArgument(
      ValidationHelper.isNotAfter(
        arrivalTime,
        arrivalDayOffset,
        other.arrivalTime,
        other.arrivalDayOffset
      ),
      "%s arrivalTime cannot be after next elements (%s) arrivalTime",
      identity(),
      other.identity()
    );
    Preconditions.checkArgument(
      ValidationHelper.isNotAfter(
        arrivalTime,
        arrivalDayOffset,
        other.departureTime,
        other.departureDayOffset
      ),
      "%s arrivalTime cannot be after next elements (%s) departureTime",
      identity(),
      other.identity()
    );

    // Validate combinations of time windows
    Preconditions.checkArgument(
      ValidationHelper.isNotAfter(
        latestArrivalTime,
        latestArrivalDayOffset,
        other.latestArrivalTime,
        other.latestArrivalDayOffset
      ),
      "%s latestArrivalTime cannot be after later next elements (%s) latestArrivalTime",
      identity(),
      other.identity()
    );
    Preconditions.checkArgument(
      ValidationHelper.isNotAfter(
        earliestDepartureTime,
        earliestDepartureDayOffset,
        other.earliestDepartureTime,
        other.earliestDepartureDayOffset
      ),
      "%s earliestDepartureTime cannot be after later next elements (%s) earliestDepartureTime",
      identity(),
      other.identity()
    );

    // Validate time window in combination with regular passing time
    Preconditions.checkArgument(
      ValidationHelper.isNotAfter(
        latestArrivalTime,
        latestArrivalDayOffset,
        other.arrivalTime,
        other.arrivalDayOffset
      ),
      "%s latestArrivalTime cannot be after next element (%s) arrivalTime",
      identity(),
      other.identity()
    );
    Preconditions.checkArgument(
      ValidationHelper.isNotAfter(
        departureTime,
        departureDayOffset,
        other.earliestDepartureTime,
        other.earliestDepartureDayOffset
      ),
      "%s departureTime cannot be after next element (%s) earliestDepartureTime",
      identity(),
      other.identity()
    );
  }

  public static class MultilingualString {

    private String lang;
    private String value;

    public String getLang() {
      return lang;
    }

    public void setLang(String lang) {
      this.lang = lang;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Quay {

    private String id;
    private String publicCode;
    private SimplePoint_VersionStructure centroid;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getPublicCode() {
      return publicCode;
    }

    public void setPublicCode(String publicCode) {
      this.publicCode = publicCode;
    }

    public SimplePoint_VersionStructure getCentroid() {
      return centroid;
    }

    public void setCentroid(SimplePoint_VersionStructure centroid) {
      this.centroid = centroid;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class StopPlace {

    private String id;
    private MultilingualString name;
    private AllVehicleModesOfTransportEnumeration transportMode;
    private SimplePoint_VersionStructure centroid;
    private List<Quay> quays;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public MultilingualString getName() {
      return name;
    }

    public void setName(MultilingualString name) {
      this.name = name;
    }

    public List<Quay> getQuays() {
      return quays;
    }

    public void setQuays(List<Quay> quays) {
      this.quays = quays;
    }

    public AllVehicleModesOfTransportEnumeration getTransportMode() {
      return transportMode;
    }

    public void setTransportMode(AllVehicleModesOfTransportEnumeration transportMode) {
      this.transportMode = transportMode;
    }

    public SimplePoint_VersionStructure getCentroid() {
      return centroid;
    }

    public void setCentroid(SimplePoint_VersionStructure centroid) {
      this.centroid = centroid;
    }
  }
}
