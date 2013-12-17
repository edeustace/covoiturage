package cache;

import models.Event;
import models.Subscriber;
import models.User;
import play.cache.Cache;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 05/09/13
 * Time: 20:01
 * To change this template use File | Settings | File Templates.
 */
public class CacheHandler {

    public static CachedEvent getCachedEvent(String id){
        CachedEvent currentEvent = (CachedEvent) Cache.get("Event-" + id);
        if(currentEvent==null){
            Event event = Event.read(id);
            if(event!=null){
                if(event.getContactsOnly()){
                    List<String> ids = newArrayList();
                    ids.add(event.getCreatorRef());
                    for(Subscriber subscriber : event.getSubscribers()){
                        ids.add(subscriber.getUserRef());
                    }
                    currentEvent = new CachedEvent(Boolean.TRUE, event.getContacts(), ids);
                }else{
                    currentEvent = new CachedEvent();
                }
                Cache.set("Event-"+id, currentEvent);
            }
        }
        return currentEvent;
    }

    public static void resetCachedEvent(String id){
        Cache.remove("Event-"+id);
    }

    public static CachedUser getCachedUser(String id){
        CachedUser cachedUser = (CachedUser)Cache.get("User-"+id);
        if(cachedUser==null){
            User user = User.findById(id);
            cachedUser = new CachedUser(user.getId(), user.getEmail());
        }
        return cachedUser;
    }
    public static void resetCachedUser(String id){
        Cache.remove("User-"+id);
    }

    public static class CachedUser {
        public String id;
        public String email;

        public CachedUser(String id, String email) {
            this.id = id;
            this.email = email;
        }
    }

    public static class CachedEvent {
        Boolean contactsOnly = Boolean.FALSE;
        List<String> mails;
        List<String> ids;

        public CachedEvent() {
        }

        public CachedEvent(Boolean contactsOnly, List<String> mails, List<String> ids) {
            this.contactsOnly =contactsOnly;
            this.mails = mails;
            this.ids = ids;
        }

        public Boolean getContactsOnly() {
            return contactsOnly;
        }

        public List<String> getMails() {
            return mails;
        }

        public List<String> getIds() {
            return ids;
        }
    }

}
