package models;

import be.objectify.deadbolt.core.models.Permission;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
public class UserPermission implements Permission {

	public Long id;

	public String value;

    @JsonIgnore
	public String getValue() {
		return value;
	}
}
