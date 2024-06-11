import java.io.*;
import java.sql.Date;
import java.util.Vector;

public class Octree implements Serializable {

    OctreeNode root;
    String name;
    String xName;
    String yName;
    String zName;

    public Octree (Object xMin ,Object xMax ,Object yMin ,Object yMax,Object zMin ,Object zMax ,String name, String xName, String yName,String zName) { //loaded from metadata file
        if(xMin instanceof Double){
            xMin = (Double)xMin;
            xMax = (Double) xMax;
        }else if(xMin instanceof  Date){
            xMin = (Date)xMin;
            xMax = (Date) xMax;
        } else if (xMin instanceof Integer) {
            xMin = (Integer)xMin;
            xMax = (Integer) xMax;
        }else{
            xMin = ((String)xMin).toLowerCase();
            xMax = ((String)xMax).toLowerCase();
        }
        if(yMin instanceof Double){
            yMin = (Double)yMin;
            yMax = (Double)yMax;

        }else if(yMin instanceof Date){
            yMin = (Date)yMin;
            yMax = (Date)yMax;
        } else if (yMin instanceof Integer) {
            yMin = (Integer)yMin;
            yMax = (Integer) yMax;
        }else{
            yMin = ((String)yMin).toLowerCase();
            yMax = ((String)yMax).toLowerCase();
        }
        if(zMax instanceof Double){
            zMax = (Double)zMax;
            zMin = (Double)zMin;
        }else if(zMax instanceof Date){
            zMax = (Date)zMax;
            zMin = (Date)zMin;
        }  else if (xMin instanceof Integer) {
            zMin = (Integer) zMin;
            zMax = (Integer) zMax;
        }else{
            zMin = ((String)zMin).toLowerCase();
            zMax = ((String)zMax).toLowerCase();
        }
        root = new OctreeNode(xMin , xMax , yMin , yMax, zMin , zMax);
        this.xName=xName;
        this.yName=yName;
        this.zName=zName;
        this.name =name;
    }

    public void insert(Object x, Object y, Object z, String pagePath , Object clustringKey) {
        System.out.println("I am inserting " + " x : " + x + " Y" + y + " Z" + z);
        if(root.getIsLeaf()) {
            System.out.println("root is leaf");
            if(root.pointExist(x, y, z)) {
                System.out.println("point exist");
                int index = root.pointIndex(new Point(x,y,z));   //index of the point in the vector of points
                Reference r = new Reference(pagePath, clustringKey);
                root.getPoint().get(index).getReference().add(r);  //insert el pagePath in the point
            }
            else {
                if(!root.isFull()) {
                    System.out.println("root is not full");
                    Point p = new Point(x,y,z);
                    Reference r = new Reference(pagePath, clustringKey);
                    p.getReference().add(r);
                    root.getPoint().add(p);
                }else {
                    System.out.println("root is full");
                    //split to 8 children
                    //put boundries to each child   --> call method that gets middle of each object
                    //distribute the points over the children
                    splitNode(root);
                    Point newPoint  = new Point(x,y,z);
                    Reference r = new Reference(pagePath, clustringKey);
                    newPoint.getReference().add(r);
                    insertIntoChild(root,newPoint);
                    //set isleaf of root to false
                    root.getPoint().clear();
                    root.setIsLeaf(false);
                }
            }
        }else {
            System.out.println("root is not leaf");
            Point newPoint  = new Point(x,y,z);
            Reference r = new Reference(pagePath, clustringKey);
            newPoint.getReference().add(r);
            insertIntoChild(root,newPoint);
        }
        System.out.println(this.toString());
    }

    public Vector<Reference> find(Object MinX, Object MaxX , Object MinY, Object MaxY, Object MinZ, Object MaxZ){
        Vector<Reference> references = new Vector<Reference> ();
        references=  findHelper(this.root, MinX, MaxX , MinY, MaxY, MinZ, MaxZ, references);
        return removeDuplicates(references);
    }

    private Vector<Reference> findHelper(OctreeNode node, Object minX, Object maxX, Object minY, Object maxY, Object minZ, Object maxZ, Vector<Reference> references) {
        if(node==null) {
            return references;
        }
        if((inBetween(minX, node.getxMin(), node.getxMax()) || inBetween(maxX, node.getxMin(), node.getxMax())) &&
                (inBetween(minY, node.getyMin(), node.getyMax()) || inBetween(maxY, node.getyMin(), node.getyMax()))&&
                (inBetween(minZ, node.getzMin(), node.getzMax()) || inBetween(maxZ, node.getzMin(), node.getzMax()))) {
            System.out.println("My minX value : " + minX +" My maxX value : " + maxX + " Minimum X : " + node.getxMin() + "Maximum X : " + node.getxMax() +"\n"
                    +"My minY value : " + minY +" My maxY value : " + maxY +  " Minimum Y : "+ node.getyMin() + " Maximum Y : "+ node.getyMax() + "\n" +
                    "My minZ value : " + minZ +" My maxZ value : " + maxZ + " Minimum Z : " +node.getzMin() + " Maximum Z : "+  node.getzMax());
            if(node.getIsLeaf()) {
                System.out.println("I am a leaf");
                for(int i=0 ; i<node.getPoint().size(); i++) {
                    if(inBetween(node.getPoint().get(i).getX(), minX, maxX) && inBetween(node.getPoint().get(i).getY(),minY, maxY)
                            && (inBetween(node.getPoint().get(i).getZ(), minZ, maxZ))){
                        System.out.println("This point is in my range");
                        references.addAll(node.getPoint().get(i).getReference());

                    }
                }
                findHelper(node.getRight(),minX,maxX,minY,maxY,minZ,maxZ,references);
            }else {
                for(int i=0; i<node.getChildren().length; i++) {
                    findHelper(node.getChildren()[i],minX,maxX,minY,maxY,minZ,maxZ,references);
                }
            }
        }
        return references;
    }
    public static Vector<Reference> removeDuplicates(Vector<Reference> vector) {
        Vector<Reference> uniqueElements = new Vector<>();

        for (Reference reference : vector) {
            if (!uniqueElements.contains(reference)) {
                uniqueElements.add(reference);
            }
        }

        vector.clear();
        vector.addAll(uniqueElements);
        return vector;
    }

    public void delete(Object x , Object y , Object z , Object clustringkey) {
        System.out.println("Entered delete Method");
        deleteHelper(this.root, x, y, z, clustringkey);   //problen
    }

    private void deleteHelper(OctreeNode node, Object x, Object y, Object z, Object clustringkey) {
        System.out.println("Entered deleteHelper Method");
        if(inBetween(x, node.getxMax(), node.getxMin()) && inBetween(y, node.getyMax(), node.getyMin())
                && (inBetween(z, node.getzMax(), node.getzMin()))){
            System.out.println("My X value : " + x + "Minimum X : " + node.getxMin() + "Maximum X : " + node.getxMax() +"\n"
                    +"My Y value: " +y + " Minimum Y : "+ node.getyMin() + " Maximum Y : "+ node.getyMax() + "\n" +
                    "My Z value : " +  z + " Minimum Z : " +node.getzMin() + " Maximum Z : "+  node.getzMax());
            if(node.getIsLeaf()) {
                System.out.println(node.getChildren().toString());
                if(node.pointExist(x, y, z)) {
                    System.out.println("My points : "+ node.getPoint());
                    int index = node.pointIndex(new Point (x,y,z));
                    int referenceIndex = node.getPoint().get(index).ReferenceIndex(clustringkey);  //probelm
                    node.getPoint().get(index).getReference().remove(referenceIndex);
                    if(node.getPoint().get(index).getReference().size()==0) {
                        node.getPoint().remove(index);
                    }
                    return;
                }
                else {
                    return;
                }

            }
            else {
                for(int i=0; i<node.getChildren().length; i++) {
                    deleteHelper(node.getChildren()[i], x, y, z , clustringkey);   //problem
                }
            }
        }


    }
    public void update(Object oldX, Object oldY,Object oldZ, Object newX, Object newY, Object newZ, String pagePath, Object clustringkey) {
        delete(oldX, oldY, oldZ, clustringkey);   //problem
        insert(newX, newY, newZ, pagePath, clustringkey);
    }


    public void splitNode(OctreeNode node) {
        System.out.println("splitting node");
        Object middlex = getMiddle(node.getxMax(),node.getxMin());
        Object middley = getMiddle(node.getyMax(),node.getyMin());
        Object middlez = getMiddle(node.getzMax(),node.getzMin());
        if(node.getzMax() instanceof Double){
            middlez = (Double)getMiddle(node.getzMax(),node.getzMin());
        }
        //split to 8 children
        node.getChildren()[0] = new OctreeNode(node.getxMin(),middlex , node.getyMin(), middley , node.getzMin(),middlez);  //child 1
        node.getChildren()[1] = new OctreeNode(node.getxMin(),middlex , node.getyMin(), middley ,middlez , node.getzMax()); //child 2
        node.getChildren()[2] = new OctreeNode(node.getxMin(),middlex ,middley , node.getyMax(),node.getzMin(),middlez );   //child 3
        node.getChildren()[3] = new OctreeNode(node.getxMin(),middlex ,middley , node.getyMax(),middlez , node.getzMax());  //child 4
        node.getChildren()[4] = new OctreeNode(middlex , node.getxMax(),node.getyMin() , middley, node.getzMin(),middlez ); //child 5
        node.getChildren()[5] = new OctreeNode(middlex , node.getxMax(),node.getyMin() , middley, middlez,node.getzMax());  //child 6
        node.getChildren()[6] = new OctreeNode(middlex , node.getxMax(),middley , node.getyMax() , node.getzMin(),middlez); //child 7
        node.getChildren()[7] = new OctreeNode(middlex , node.getxMax(),middley , node.getyMax() , middlez,node.getzMax()); //child 8

        node.getChildren()[0].setRight(node.getChildren()[1]);
        node.getChildren()[1].setRight(node.getChildren()[2]);
        node.getChildren()[1].setLeft(node.getChildren()[0]);
        node.getChildren()[2].setRight(node.getChildren()[3]);
        node.getChildren()[2].setLeft(node.getChildren()[1]);
        node.getChildren()[3].setRight(node.getChildren()[4]);
        node.getChildren()[3].setLeft(node.getChildren()[2]);
        node.getChildren()[4].setRight(node.getChildren()[5]);
        node.getChildren()[4].setLeft(node.getChildren()[3]);
        node.getChildren()[5].setRight(node.getChildren()[6]);
        node.getChildren()[5].setLeft(node.getChildren()[4]);
        node.getChildren()[6].setRight(node.getChildren()[7]);
        node.getChildren()[6].setLeft(node.getChildren()[5]);
        node.getChildren()[7].setLeft(node.getChildren()[6]);


        distributeData(node);
    }

    public void distributeData(OctreeNode node) {
        System.out.println("distributing data");
        for(int i = 0; i<node.getPoint().size();i++) {
            insertIntoChildrenAfterSplitting(node , node.getPoint().get(i));
        }
    }
    public void insertIntoChildrenAfterSplitting(OctreeNode node, Point point) {
        System.out.println("insertIntoChildrenAfterSplitting");
        Object x, y,z;
        x=point.getX();
        y=point.getY();
        z=point.getZ();
        for (int i=0; i< node.getChildren().length;i++) {
            OctreeNode child = node.getChildren()[i];
            if(inBetween(x,child.getxMin(),child.getxMax()) && inBetween(y,child.getyMin(),child.getyMax()) && inBetween(z,child.getzMin(),child.getzMax())) {
                if(child.isFull()) {
                    System.out.println("I am the full child"+child.toString());
                    splitNode(child);
                    insertIntoChild(child,point);
                    child.getPoint().clear();
                    child.setIsLeaf(false);
                }
                else {
                    Point newPoint = (new Point(x,y,z));
                    for(Reference reference : point.getReference()) {
                        newPoint.getReference().add(reference);
                    }
                    child.getPoint().add(newPoint);
                }
            }
        }
    }
    public Boolean inBetween(Object x, Object min , Object max) {
        if(compareTo(min,x) <=0   &&  compareTo(max,x)>=0)
            return true;
        return false;
    }

    public void insertIntoChild(OctreeNode node, Point p) {
        System.out.println("Entered insertIntoChild Method");
        System.out.println("Inserting : " + p.toString());
        for(int i=0 ; i<node.getChildren().length;i++) {
            System.out.println("1");
            OctreeNode child = node.getChildren()[i];
            System.out.println("My X : " + p.getX() + "Limit : " + child.getxMin()+ "-" + child.getxMax() +"/n" + "My Y : " +p.getY() + "Limit : " + child.getyMin() +"-" +child.getyMax() + "/n" + "My Z : "+p.getZ() + " Limit : " +child.getzMin() + "-" + child.getzMax());
            if(inBetween(p.getX(),child.getxMin(),child.getxMax()) && inBetween(p.getY(), child.getyMin(),child.getyMax()) && inBetween(p.getZ(),child.getzMin(),child.getzMax())) {
                System.out.println("2");
                if(child.isFull()) {
                    System.out.println("3");
                    if(child.pointExist(p.getX(), p.getY(), p.getZ())) {
                        int index = child.pointIndex(new Point(p.getX(), p.getY(), p.getZ()));   //index of the point in the vector of points
                        child.getPoint().get(index).getReference().add(p.getReference().get(0));
                    }else {
                        splitNode(child);

                        child.getPoint().clear();
                        child.setIsLeaf(false);
                    }
                }else {
                    System.out.println("4");
                    if(child.getIsLeaf()) {
                        if(child.pointExist(p.getX(), p.getY(), p.getZ())) {
                            int index = child.pointIndex(new Point(p.getX(), p.getY(), p.getZ()));   //index of the point in the vector of points
                            child.getPoint().get(index).getReference().add(p.getReference().get(0));
                        }else {
                            child.getPoint().add(p);
                        }
                    }else {
                        insertIntoChild(child,p);
                    }
                }
            }
        }

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
            return (((String) o1).compareToIgnoreCase((String) o2));

        } else if (o1 instanceof Date && o2 instanceof Date) {
            System.out.println("I am a date");
            return (((Date) o1).compareTo((Date) o2));
        }
        return -1000000;
    }

    public Object getMiddle(Object obj1 , Object obj2) {
        if (obj1 instanceof Double) {
            System.out.println("I am a Double dataType");
            return ((Double) obj1 + (Double) obj2)/2;
        }
        else if (obj1 instanceof Integer) {
            System.out.println("I am an Integer dataType");
            return ((int) obj1 + (int) obj2)/2;
        }else if (obj1 instanceof Date) {
            System.out.println("I am a date DataType");
            return getMiddleDate((Date)obj1, (Date)obj2);
        }else if (obj1 instanceof String ) {
            System.out.println("I am a string dataType");
            int length1 = ((String) ((String) obj1).toLowerCase()).length();
            int length2 = (((String) obj2).toLowerCase()).length();
            return getMiddleString((String) ((String) obj1).toLowerCase() , (String) ((String) obj2).toLowerCase(),Integer.min(length1,length2));
        }
        return 0 ;
    }



    static Object getMiddleString(String S, String T, int N)
    {
        // Stores the base 26 digits after addition
        int[] a1 = new int[N + 1];

        for (int i = 0; i < N; i++) {
            a1[i + 1] = (int)S.charAt(i) - 97
                    + (int)T.charAt(i) - 97;
        }

        // Iterate from right to left
        // and add carry to next position
        for (int i = N; i >= 1; i--) {
            a1[i - 1] += (int)a1[i] / 26;
            a1[i] %= 26;
        }

        // Reduce the number to find the middle
        // string by dividing each position by 2
        for (int i = 0; i <= N; i++) {

            // If current value is odd,
            // carry 26 to the next index value
            if ((a1[i] & 1) != 0) {

                if (i + 1 <= N) {
                    a1[i + 1] += 26;
                }
            }

            a1[i] = (int)a1[i] / 2;
        }
        String result = "";
        for (int i = 1; i <= N; i++) {
            result+=(char)(a1[i] + 97);
        }
        return result;
    }


    public static Date getMiddleDate(Date date1 , Date date2){
        Date date = new Date(0);
        date.setYear(((date1.getYear()+date2.getYear())/2)-1900);
        date.setMonth(((date1.getMonth()+date2.getMonth())/2)-1);
        date.setDate(((date1.getDate()+date2.getDate())/2));
        return date;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toStringHelper(root, sb);
        return sb.toString();
    }

    private void toStringHelper(OctreeNode node, StringBuilder sb) {
        if (node == null) {
            return;
        }

        sb.append("Node: ");
        sb.append(node.getxMax());
        sb.append("\n");
        sb.append(node.getyMax());
        sb.append("\n");
        sb.append(node.getzMax());
        sb.append("\n");



        if (!node.getIsLeaf()) {
            for (OctreeNode child : node.getChildren()) {
                sb.append("\t");
                toStringHelper(child, sb);
            }
        } else {
            sb.append("\tPoints:\n");
            for (Point point : node.getPoint()) {
                sb.append("\t\t");
                sb.append(point.toString());
                sb.append("\n");
            }
        }
    }

    public static void main(String[] args) {
        Octree octree = new Octree(0, 100, 0, 100, 0, 100 , "XYZ","A", "B" , "C");

        // Insert points
        octree.insert(10, 20, 30, "page1", "key1");
        System.out.println(octree.toString());
        System.out.println("-------------------------------------------");

        octree.insert(50, 60, 70, "page2", "key2");
        /*
         * octree.insert(50, 60, 70, "page6", "key6"); octree.insert(50, 60, 70,
         * "page9", "key9");
         */
        System.out.println(octree.toString());
        System.out.println("-------------------------------------------");

        octree.insert(80, 90, 40, "page3", "key3");
        System.out.println(octree.toString());
        System.out.println("-------------------------------------------");
        octree.insert(100, 100, 100, "page9", "key9");
        System.out.println(octree.toString());
        System.out.println("-------------------------------------------");
        // Find points

        System.out.println("Finding in range");
        Vector<Reference> references = octree.find(0, 100, 0, 100, 0, 100);
        System.out.println("References found:"+references);
        System.out.println("-------------------------------------------");

        // Update a point
        System.out.println("Update Started: ");
        octree.update(10, 20, 30, 15, 25, 80, "page1", "key1");

        System.out.println(octree.toString());
        System.out.println("-------------------------------------------");
        // Delete a point
        System.out.println("Delete Started: ");
        octree.delete(50, 60, 70, "key2");

        System.out.println( octree.toString());
        System.out.println("-------------------------------------------");
    }


    public void serialize(String strTableName , String OctreeName , Octree octree ) {
        try{
            FileOutputStream fos = new FileOutputStream("src\\main\\resources\\data\\"+strTableName+"\\"+OctreeName );
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(octree);
            oos.close();
            fos.close();
        }catch(Exception e){
            //    System.out.println("problem in serialize method");
            System.out.println(e.getMessage());
        }
    }
    public static Octree deserializeOctree(String strTableName , String OctreeName)  {
        try{
            FileInputStream fis = new FileInputStream("src\\main\\resources\\data\\"+strTableName+"\\"+OctreeName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Octree obj = (Octree)ois.readObject();
            ois.close();
            fis.close();
            return obj;
        }catch(Exception e){
            // System.out.println("Deserializing problem");
            System.out.println(e.getMessage());
        }
        return null;
    }
}


