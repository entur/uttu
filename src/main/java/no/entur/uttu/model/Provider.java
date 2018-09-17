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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "provider_code_index", columnNames = "code")})
public class Provider extends IdentifiedEntity {
    @NotNull
    private String code;

    @NotNull
    private String name;

    @NotNull
    @ManyToOne
    private Codespace codespace;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Codespace getCodespace() {
        return codespace;
    }

    public void setCodespace(Codespace codespace) {
        this.codespace = codespace;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
