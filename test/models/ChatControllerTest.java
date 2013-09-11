package models;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 05/08/13
 * Time: 21:46
 * To change this template use File | Settings | File Templates.
 */
public class ChatControllerTest {

    @Test
    public void testEqualsIds(){

        Topic topic1 = new Topic();
        topic1.setId("123");

        Topic topic2 = new Topic();
        topic2.setId("123");

        assertThat(topic1).isEqualTo(topic2);
    }

    @Test
    public void testEqualsByList(){

        Topic topic1 = new Topic();
        topic1.idEvent = "1";
        topic1.categorie = Topic.TopicCategorie.carChat;
        topic1.subscribers.add("1");
        topic1.subscribers.add("2");

        Topic topic2 = new Topic();
        topic2.idEvent = "1";
        topic2.categorie = Topic.TopicCategorie.carChat;
        topic2.subscribers.add("1");
        topic2.subscribers.add("2");

        assertThat(topic1.equals(topic2)).isEqualTo(true);
    }
    @Test
    public void testEqualsByCategorie(){

        Topic topic1 = new Topic();
        topic1.idEvent = "1";
        topic1.categorie = Topic.TopicCategorie.carChat;

        Topic topic2 = new Topic();
        topic2.idEvent = "1";
        topic2.categorie = Topic.TopicCategorie.carChat;

        assertThat(topic1.equals(topic2)).isEqualTo(true);
    }

    @Test
    public void testNotEquals1(){

        Topic topic1 = new Topic();
        topic1.idEvent = "1";
        topic1.categorie = Topic.TopicCategorie.carChat;
        topic1.subscribers.add("1");
        topic1.subscribers.add("2");
        topic1.subscribers.add("3");

        Topic topic2 = new Topic();
        topic2.idEvent = "1";
        topic2.categorie = Topic.TopicCategorie.carChat;
        topic2.subscribers.add("1");
        topic2.subscribers.add("2");

        assertThat(topic1.equals(topic2)).isEqualTo(false);
    }

    @Test
    public void testNotEquals2(){

        Topic topic1 = new Topic();
        topic1.idEvent = "1";
        topic1.categorie = Topic.TopicCategorie.carChat;
        topic1.subscribers.add("1");
        topic1.subscribers.add("2");


        Topic topic2 = new Topic();
        topic2.idEvent = "1";
        topic2.categorie = Topic.TopicCategorie.carChat;
        topic2.subscribers.add("1");
        topic2.subscribers.add("2");
        topic2.subscribers.add("3");

        assertThat(topic1.equals(topic2)).isEqualTo(false);
    }


    @Test
    public void testNotEquals3(){

        Topic topic1 = new Topic();
        topic1.idEvent = "1";
        topic1.categorie = Topic.TopicCategorie.wall;
        topic1.subscribers.add("1");
        topic1.subscribers.add("2");


        Topic topic2 = new Topic();
        topic2.idEvent = "1";
        topic2.categorie = Topic.TopicCategorie.carChat;
        topic2.subscribers.add("1");
        topic2.subscribers.add("2");

        assertThat(topic1.equals(topic2)).isEqualTo(false);
    }
}
