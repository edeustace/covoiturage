package models;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 30/05/13
 * Time: 21:15
 * To change this template use File | Settings | File Templates.
 */
public class LocationTest {

    @Test
    public void testEmptyTrue(){
         assertThat(Location.location().isEmpty()).isTrue();
    }
    @Test
    public void testEmptyFalse2(){
        assertThat(Location.location().setLng("lng").isEmpty()).isFalse();
    }
    @Test
    public void testEmptyFalse3(){
        assertThat(Location.location().setLat("lat").isEmpty()).isFalse();
    }
    @Test
    public void testEmptyFalse4(){
        assertThat(Location.location().setLat("lat").setLng("lng").isEmpty()).isFalse();
    }
    @Test
    public void testMerge(){
        Location location = Location.location().setLat("lat").setLng("lng");
        Location location2 = Location.location().setLat("lat1").setLng("lng1");
        location.merge(location2);
        assertThat(location.getLat()).isEqualTo("lat1");
        assertThat(location.getLng()).isEqualTo("lng1");
    }
    @Test
    public void testMerge2(){
        Location location = Location.location().setLat("lat").setLng("lng");
        Location location2 = Location.location().setLng("lng1");
        location.merge(location2);
        assertThat(location.getLat()).isEqualTo("lat");
        assertThat(location.getLng()).isEqualTo("lng1");
    }
    @Test
    public void testMerge3(){
        Location location = Location.location().setLat("lat").setLng("lng");
        Location location2 = Location.location().setLat("lat1");
        location.merge(location2);
        assertThat(location.getLat()).isEqualTo("lat1");
        assertThat(location.getLng()).isEqualTo("lng");
    }
}
