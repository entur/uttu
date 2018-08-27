package no.entur.uttu.model;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import java.time.DayOfWeek;
import java.util.List;

@Entity
public class DayType extends ProviderEntity {

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> daysOfWeek;

    @OneToMany(cascade = CascadeType.ALL)
    private List<DayTypeAssignment> dayTypeAssignments;

    public List<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<DayOfWeek> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }


    public List<DayTypeAssignment> getDayTypeAssignments() {
        return dayTypeAssignments;
    }

    public void setDayTypeAssignments(List<DayTypeAssignment> dayTypeAssignments) {
        this.dayTypeAssignments = dayTypeAssignments;
    }
}
