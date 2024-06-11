import java.io.Serializable;
import java.util.Vector;

public class Point implements Serializable {
    private Object x;
    private Object y;
    private Object z;
    private Vector<Reference> reference;

    public Vector<Reference> getReference() {
        return reference;
    }
    public Point(Object x, Object y, Object z) {
        this.x = x;
        this.y = y;
        this.z = z;
        reference = new Vector<Reference>();
    }
    public Object getX() {
        return x;
    }
    public Object getY() {
        return y;
    }
    public Object getZ() {
        return z;

    }
    public int ReferenceIndex(Object clustringKey) {
        for(int i = 0; i<this.reference.size();i++) {

            if(this.reference.get(i).getClustringkeyValue()==clustringKey)
                return i;
        }
        return -1;
    }
    public String toString() {
        String result = "" ;
        result = "X value : "+ this.x + " " +  "Y value : "+ this.y + " " +  "Z value : "+ this.z + "Reference : " + this.reference.toString() + "\n";
        return result;
    }
}
