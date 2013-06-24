/*
 * Copyright 2012 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package models;

import javax.persistence.Id;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import be.objectify.deadbolt.core.models.Role;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SecurityRole implements Role {

	@Id
	private String id;

    @JsonIgnore
	private String roleName;

	@Override
    @JsonIgnore
    public String getName() {
		return roleName;
	}

    @JsonProperty("id")
    public String getId() {
        return id;
    }
    @JsonProperty("id")
    public SecurityRole setId(String id) {
        this.id = id;
        return this;
    }
    @JsonProperty("roleName")
    public String getRoleName() {
        return roleName;
    }
    @JsonProperty("roleName")
    public SecurityRole setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public static SecurityRole securityRole(){
        return new SecurityRole();
    }
}
