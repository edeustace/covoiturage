package models;


import dao.TokenActionDao;
import dao.TopicDao;
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

    private static TokenActionDao dao;

    private static TokenActionDao getDao(){
        return TokenAction.dao;
    }

    public static void setDao(TokenActionDao dao){
        TokenAction.dao = dao;
    }

	public static TokenAction findByToken(final String token, final Type type) {
        return getDao().findByToken(token, type);
	}

	public static void deleteByUser(final User u, final Type type) {
        getDao().deleteByUser(u, type);
	}

    @JsonIgnore
	public boolean isValid() {
		return this.expires.after(new Date());
	}

    public TokenAction save(){
        return getDao().save(this);
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
