package actors;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 03/09/13
 * Time: 20:26
 * To change this template use File | Settings | File Templates.
 */
public class Message {

    public static enum Action {CREATE, UPDATE, DELETE, NA};

    public String idEvent;

    public String type;

    public Action action;

    public Object data;

    public String from;

    public List<String> to;

    public Message() {
    }

    public Message(String idEvent, String type, Action action, Object data, String from, List<String> to) {
        this.idEvent = idEvent;
        this.type = type;
        this.action = action;
        this.data = data;
        this.from = from;
        this.to = to;
    }

}
