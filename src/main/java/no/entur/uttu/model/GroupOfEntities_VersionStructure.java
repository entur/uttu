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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.CascadeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@MappedSuperclass
public abstract class GroupOfEntities_VersionStructure
        extends ProviderEntity {

    protected String name;

    protected String shortName;

    @Size(max = 4000)
    protected String description;

    protected String privateCode;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    protected Map<String, Value> keyValues = new HashMap<>();

    public GroupOfEntities_VersionStructure() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrivateCode() {
        return privateCode;
    }

    public void setPrivateCode(String value) {
        this.privateCode = value;
    }

    public Map<String, Value> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(Map<String, Value> keyValues) {
        this.keyValues = keyValues;
    }
}