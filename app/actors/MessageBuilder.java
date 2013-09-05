package actors;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 03/09/13
 * Time: 20:31
 * To change this template use File | Settings | File Templates.
 */
public class MessageBuilder {

    private Message message;

    private MessageBuilder(){
        super();
        message = new Message();
    }

    public static MessageBuilder newMessage(){
        return new MessageBuilder();
    }

    public Message get() {
        return message;
    }

    public MessageBuilder setType(String type) {
        this.message.type = type;
        return this;
    }

    public MessageBuilder setAction(Message.Statut action) {
        this.message.statut = action;
        return this;
    }

    public MessageBuilder setData(Object data) {
        this.message.data = data;
        return this;
    }

    public MessageBuilder setTo(List<String> to) {
        this.message.to = to;
        return this;
    }

    public MessageBuilder setFrom(String from) {
        this.message.from = from;
        return this;
    }
}
