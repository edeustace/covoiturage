package dao;

import models.Topic;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 10/09/13
 * Time: 21:33
 * To change this template use File | Settings | File Templates.
 */
public interface TopicDao extends Dao<Topic>{

    List<Topic> findByIdEventAndCategorie(String idEvent, Topic.TopicCategorie categorie);

    Topic findByIdEventCategorieAndCreator(String idEvent, Topic.TopicCategorie categorie, String idCreator);

    List<Topic> findByIdEventAndIdUser(String idEvent, String idUser);

}
