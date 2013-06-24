package controllers.decorators;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 02/06/13
 * Time: 20:59
 * To change this template use File | Settings | File Templates.
 */
public class Link {

    public static String SELF = "self";
    public static String CREATE = "create";
    public static String UPDATE = "update";
    public static String DELETE = "delete";
    

    private String rel;
    private String href;

    private Link(String rel, String href) {
        this.rel = rel;
        this.href = href;
    }

    public static Link link(String rel, String href){
        return new Link(rel, href);
    }

    @JsonProperty("rel")
    public String getRel() {
        return rel;
    }
    @JsonProperty("href")
    public String getHref() {
        return href;
    }
}
