import java.io.FileInputStream;
import java.io.Serializable;
import java.sql.Date;
import java.util.Properties;
import java.util.Vector;

public class OctreeNode implements Serializable {
    private Object xMin , xMax , yMin , yMax, zMin , zMax ;
    private OctreeNode [] children ;
    private Vector<Point> point;
    private Boolean isLeaf;
    private OctreeNode left;
    private OctreeNode right;
    public OctreeNode(Object xMin, Object xMax, Object yMin, Object yMax, Object zMin, Object zMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.zMin = zMin;
        this.zMax = zMax;
        children = new OctreeNode[8];
        point = new Vector<Point> ();
        isLeaf = true;
    }
    public Boolean getIsLeaf() {
        return isLeaf;
    }
    public void setIsLeaf(Boolean isLeaf) {
        this.isLeaf = isLeaf;
    }
    public Object getxMin() {
        return xMin;
    }
    public void setxMin(Object xMin) {
        this.xMin = xMin;
    }
    public Object getxMax() {
        return xMax;
    }
    public void setxMax(Object xMax) {
        this.xMax = xMax;
    }
    public Object getyMin() {
        return yMin;
    }
    public void setyMin(Object yMin) {
        this.yMin = yMin;
    }
    public Object getyMax() {
        return yMax;
    }
    public void setyMax(Object yMax) {
        this.yMax = yMax;
    }
    public Object getzMin() {
        return zMin;
    }
    public void setzMin(Object zMin) {
        this.zMin = zMin;
    }
    public Object getzMax() {
        return zMax;
    }
    public void setzMax(Object zMax) {
        this.zMax = zMax;
    }
    public OctreeNode[] getChildren() {
        return children;
    }
    public void setChildren(OctreeNode[] children) {
        this.children = children;
    }
    public Vector<Point> getPoint() {
        return point;
    }
    public void setPoint(Vector<Point> point) {
        this.point = point;
    }
    public OctreeNode getLeft() {
        return left;
    }
    public void setLeft(OctreeNode left) {
        this.left = left;
    }
    public OctreeNode getRight() {
        return right;
    }
    public void setRight(OctreeNode right) {
        this.right = right;
    }

    public Boolean isFull() {
        String fileName = "src\\main\\resources\\DBApp.config";
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
            return this.point.size()==Integer.parseInt(prop.getProperty("MaximumEntriesinOctreeNode"));
        } catch(Exception e){
            System.out.println("error in finding config file");
        }
        return null;
    }
    public Boolean isEmpty() {
        return this.point.size()==0;
    }
    public Boolean pointExist(Object x, Object y , Object z) {
        for(int i = 0; i<this.point.size();i++) {
            if(this.point.get(i).getX()==x && this.point.get(i).getY()==y && this.point.get(i).getZ()==z )
                return true;
        }
        return false;
    }
    public int pointIndex(Point p) {
        for(int i = 0; i<this.point.size();i++) {
            if(compareTo(this.point.get(i).getX(),p.getX())==0 && compareTo(this.point.get(i).getY(),p.getY())==0 && compareTo(this.point.get(i).getZ(),p.getZ())==0 )
                return i;
        }
        return -1;
    }
    public static int compareTo(Object o1, Object o2) {
        if (o1 instanceof Integer && o2 instanceof Integer) {
            if ((int) o1 == (int) o2)
                return 0;
            else if ((int) o1 > (int) o2)
                return 1;
            else
                return -1;

        } else if (o1 instanceof Double && o2 instanceof Double) {
            if (((Double) o1).equals(((Double) o2)))
                return 0;
            else if (((Double) o1) > ((Double) o2))
                return 1;
            else
                return -1;

        } else if (o1 instanceof String && o2 instanceof String) {
            return (((String) o1).compareTo((String) o2));

        } else if (o1 instanceof Date && o2 instanceof Date) {
            System.out.println("I am a date");
            return (((Date) o1).compareTo((Date) o2));
        }
        return -1000000;
    }
    public String toString(){
        return "Points in my node " + point;
    }

}
