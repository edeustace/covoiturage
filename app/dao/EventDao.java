package dao;

import models.Event;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 10/09/13
 * Time: 21:10
 * To change this template use File | Settings | File Templates.
 */
public interface EventDao extends Dao<Event> {

    List<Event> listByUser(String idUser);


}
