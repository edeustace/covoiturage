package dao;

import models.ChatMessage;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 10/09/13
 * Time: 21:34
 * To change this template use File | Settings | File Templates.
 */
public interface ChatMessageDao extends Dao<ChatMessage>{

    List<ChatMessage> findByIdTopic(String idTopic);
}
