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

package no.entur.uttu.model.job;

import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.ErrorCodeEnumeration;
import no.entur.uttu.util.Preconditions;
import no.entur.uttu.model.ProviderEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
public class Export extends ProviderEntity {

    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ExportStatusEnumeration exportStatus = ExportStatusEnumeration.IN_PROGRESS;

    private LocalDate fromDate;

    private LocalDate toDate;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExportMessage> messages = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String fileName;

    private boolean dryRun;

    public ExportStatusEnumeration getExportStatus() {
        return exportStatus;
    }

    public void markAsFinished() {
        if (messages.stream().anyMatch(m -> SeverityEnumeration.ERROR.equals(m.getSeverity()))) {
            exportStatus = ExportStatusEnumeration.FAILED;
        } else {
            exportStatus = ExportStatusEnumeration.SUCCESS;
        }

    }

    public void addMessage(ExportMessage message) {
        this.messages.add(message);
    }

    public SortedSet<ExportMessage> getMessages() {
        return new TreeSet<>(messages);
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    public String toString() {
        return "Export{" +
                       super.toString() +
                       ", name='" + name + '\'' +
                       ", exportStatus=" + exportStatus +
                       ", fromDate=" + fromDate +
                       ", toDate=" + toDate +
                       ", messages=" + messages +
                       '}';
    }

    @Override
    public void checkPersistable() {
        if (fromDate != null && toDate != null) {
            Preconditions.checkArgument(!fromDate.isAfter(toDate), CodedError.fromErrorCode(ErrorCodeEnumeration.FROM_DATE_AFTER_TO_DATE),"%s fromDate(%s) cannot be after toDate(%s)", identity(), fromDate, toDate);
        }
    }
}
