import java.io.Serializable;

public class Reference implements Serializable {
    private String pagePath;
    private Object clustringkeyValue;
    public Reference(String pagePath, Object clustringkeyValue) {
        this.pagePath = pagePath;
        this.clustringkeyValue = clustringkeyValue;
    }
    public String getPagePath() {
        return pagePath;
    }
    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }
    public Object getClustringkeyValue() {
        return clustringkeyValue;
    }
    public void setClustringkeyValue(Object clustringkeyValue) {
        this.clustringkeyValue = clustringkeyValue;
    }
    public String toString() {
        return "Page path: " + pagePath + "\n" + "Clustring Key : " + clustringkeyValue;
    }


}
