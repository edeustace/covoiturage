package models;

import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 30/05/13
 * Time: 20:50
 * To change this template use File | Settings | File Templates.
 */
public class AddressTest {

    @Test
    public void testMerge(){
        Address address = Address.address().setDescription("12 rue Toto").setLocation(Location.location().setLat("123").setLng("456"));
        Address address2 = Address.address().setDescription("14 rue Toto").setLocation(Location.location().setLat("321").setLng("654"));
        address.merge(address2);
        assertThat(address.getDescription()).isEqualTo(address2.getDescription());
        assertThat(address.getLocation().getLat()).isEqualTo(address2.getLocation().getLat());
        assertThat(address.getLocation().getLng()).isEqualTo(address2.getLocation().getLng());
    }
    @Test
    public void testMergeDescriptionProps(){
        Address address = Address.address().setDescription("12 rue Toto").setLocation(Location.location().setLat("123").setLng("456"));
        Address address2 = Address.address().setLocation(Location.location().setLat("321").setLng("654"));
        address.merge(address2);
        assertThat(address.getDescription()).isEqualTo(address.getDescription());
        assertThat(address.getLocation().getLat()).isEqualTo(address2.getLocation().getLat());
        assertThat(address.getLocation().getLng()).isEqualTo(address2.getLocation().getLng());
    }
    @Test
    public void testMergeLocationNull(){
        Address address = Address.address().setDescription("12 rue Toto").setLocation(Location.location().setLat("123").setLng("456"));
        Address address2 = Address.address().setDescription("14 rue Toto");
        address.merge(address2);
        assertThat(address.getDescription()).isEqualTo(address.getDescription());
        assertThat(address.getLocation().getLat()).isEqualTo(address.getLocation().getLat());
        assertThat(address.getLocation().getLng()).isEqualTo(address.getLocation().getLng());
    }
    @Test
    public void testMergeLocationNull2(){
        Address address = Address.address().setDescription("12 rue Toto").setLocation(Location.location().setLat("123").setLng("456"));
        Address address2 = Address.address().setDescription("14 rue Toto").setLocation(Location.location().setLat("123").setLng("456"));
        address.merge(address2);
        assertThat(address.getDescription()).isEqualTo(address.getDescription());
        assertThat(address.getLocation().getLat()).isEqualTo(address.getLocation().getLat());
        assertThat(address.getLocation().getLng()).isEqualTo(address.getLocation().getLng());
        address2.setDescription("12 rue toto").getLocation().setLat("321").setLng("654");
        assertThat(address.getDescription()).isEqualTo("14 rue Toto");
        assertThat(address.getLocation().getLat()).isEqualTo("123");
        assertThat(address.getLocation().getLng()).isEqualTo("456");
    }
    public void testMergeNullProps(){
        Address address = Address.address().setDescription("12 rue Toto").setLocation(Location.location().setLat("123").setLng("456"));
        Address address2 = Address.address();
        address.merge(address2);
        assertThat(address.getDescription()).isEqualTo(address.getDescription());
        assertThat(address.getLocation().getLat()).isEqualTo(address.getLocation().getLat());
        assertThat(address.getLocation().getLng()).isEqualTo(address.getLocation().getLng());
    }
    //EMPTY

}
