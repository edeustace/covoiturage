package models;


import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
import play.modules.mongodb.jackson.MongoDB;

import javax.persistence.Id;
import java.util.Date;

@MongoCollection(name="tokens")
public class TokenAction {

    private static JacksonDBCollection<TokenAction, String> collection = null;
    public static void collection(JacksonDBCollection<TokenAction, String> collection){
        TokenAction.collection = collection;
    }
    public static JacksonDBCollection<TokenAction, String> collection(){
        if(collection==null){
            collection = MongoDB.getCollection(TokenAction.class, String.class);
        }
        return collection;
    }

	public enum Type {
		EMAIL_VERIFICATION,
		PASSWORD_RESET
	}

	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 * in seconds
	 * Defaults to one week
	 */
	private final static long VERIFICATION_TIME = 7 * 24 * 3600;

	@Id
    @ObjectId
	public String id;

	//Doit etre unique
	public String token;

	public String targetUser;

	public Type type;

	public Date created;

	public Date expires;

	public static TokenAction findByToken(final String token, final Type type) {
        return collection().findOne(DBQuery.and(DBQuery.is("token", token), DBQuery.is("type", type)));
	}

	public static void deleteByUser(final User u, final Type type) {
        collection().remove(DBQuery.and(DBQuery.is("targetUser", u.getId()), DBQuery.is("type", type)));
	}

    @JsonIgnore
	public boolean isValid() {
		return this.expires.after(new Date());
	}

    public TokenAction save(){
        net.vz.mongodb.jackson.WriteResult<TokenAction, String> result = collection().save(this);
        this.id = result.getSavedObject().id;
        return this;
    }

    public static TokenAction create(final Type type, final String token,
			final User targetUser) {
		final TokenAction ua = new TokenAction();
		ua.targetUser = targetUser.getId();
		ua.token = token;
		ua.type = type;
		final Date created = new Date();
		ua.created = created;
		ua.expires = new Date(created.getTime() + VERIFICATION_TIME * 1000);
		ua.save();
		return ua;
	}
}
