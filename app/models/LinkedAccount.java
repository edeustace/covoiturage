package models;

import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.feth.play.module.pa.user.AuthUser;

public class LinkedAccount {

    ///////////    FIELDS  /////////////////////
	private String id = (new ObjectId()).toString();

    @JsonIgnore
	private String providerUserId;
    @JsonIgnore
    private String providerKey;

    @Override
	public int hashCode() {
		if(id!=null){
			return id.hashCode();
		}else{
			return super.hashCode();
		}
	}
	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}
		if(obj instanceof LinkedAccount){
			LinkedAccount linkedAccount = (LinkedAccount)obj;
			
			return 
					((this.providerUserId==null && linkedAccount.providerUserId==null) || 
							(this.providerUserId!=null && linkedAccount.providerUserId!=null && this.providerUserId.equals(providerUserId))) &&
					((this.providerKey==null && linkedAccount.providerKey==null) || (
							this.providerKey!=null && linkedAccount.providerKey!=null && this.providerKey.equals(linkedAccount.providerKey)));
		}
		return super.equals(obj);
	}
	///////////  CLASS METHODS /////////////////
    @JsonIgnore
	public void update(final AuthUser authUser) {
		this.providerKey = authUser.getProvider();
		this.providerUserId = authUser.getId();
	}
    //////////////////////////////////////////////
    /////////        STATIC //////////////////////
    //////////////////////////////////////////////
    public static LinkedAccount findByProviderKey(final User user, String key) {
        List<LinkedAccount> linkedAccounts = user.getLinkedAccounts();
        for(LinkedAccount linkedAccount : linkedAccounts){
            if(linkedAccount.providerKey.equals(key)){
                return linkedAccount;
            }
        }
        return null;
    }

	public static LinkedAccount create(final LinkedAccount acc) {
		final LinkedAccount ret = new LinkedAccount();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;
		return ret;
	}
    public static LinkedAccount create(final AuthUser authUser) {
        final LinkedAccount ret = new LinkedAccount();
        ret.update(authUser);
        return ret;
    }
    
    public static LinkedAccount linkedAccount(){
    	return new LinkedAccount();
    }


    //////////////////////////////////////////////
    /////////  GETTERS AND SETTERS ///////////////
    //////////////////////////////////////////////
    @JsonProperty("providerUserId")
    public String getProviderUserId() {
        return providerUserId;
    }
    @JsonProperty("providerUserId")
    public LinkedAccount setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
        return this;
    }
    @JsonProperty("providerKey")
    public String getProviderKey() {
        return providerKey;
    }
    @JsonProperty("providerKey")
    public LinkedAccount setProviderKey(String providerKey) {
        this.providerKey = providerKey;
        return this;
    }
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    @JsonProperty("id")
    public LinkedAccount setId(String id) {
        this.id = id;
        return this;
    }
}