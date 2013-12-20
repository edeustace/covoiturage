package models;

import dao.ChatMessageDao;
import dao.RepositoryLocator;
import dao.TopicDao;
import net.vz.mongodb.jackson.*;
import play.modules.mongodb.jackson.MongoDB;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 25/07/13
 * Time: 20:26
 * To change this template use File | Settings | File Templates.
 */

@MongoCollection(name="messages")
public class ChatMessage extends AbstractModel {

    public String type = "message";

    public Date date;

    @NotNull
    public String from;

    public String topicRef;

    @NotNull
    public String tmpId;

    @NotNull
    public Topic topic;

    @NotNull
    public String message;


    public ChatMessage() {
        this.date = new Date();
    }



    ////////////  STATIC  ////////////////
    public ChatMessage save(){
        if(this.topicRef==null){
            this.topicRef = topic.getId();
        }
        return getDao().save(this);
    }

    private static ChatMessageDao getDao(){
        return RepositoryLocator.getRepositoryLocator().getChatMessageDao();
    }


    public static List<ChatMessage> findByIdTopic(String idTopic){
        return getDao().findByIdTopic(idTopic);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTopicRef() {
        return topicRef;
    }

    public void setTopicRef(String topicRef) {
        this.topicRef = topicRef;
    }

    public String getTmpId() {
        return tmpId;
    }

    public void setTmpId(String tmpId) {
        this.tmpId = tmpId;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }
}
