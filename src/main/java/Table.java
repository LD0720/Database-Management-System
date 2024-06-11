import java.io.*;
import java.util.Hashtable;

public class Table implements Serializable {
     String tableName;
     String primaryKey;
     Hashtable<String, String> htblColNameType;
     Hashtable<String,String> htblColNameMin;
     Hashtable<String,String> htblColNameMax;
     Hashtable<Integer, String> pages;

    public Table(String tableName, String primaryKey, Hashtable<String, String> htblColNameType, Hashtable<String,String> htblColNameMin, Hashtable<String,String> htblColNameMax) throws DBAppException {
        this.tableName = tableName;
        this.primaryKey = primaryKey;
        pages = new Hashtable<Integer,String>();
        this.htblColNameType = htblColNameType;
        this.htblColNameMax = htblColNameMax;
        this.htblColNameMin=htblColNameMin;
        File file = new File("src\\main\\resources\\data\\"+tableName);
        Boolean flag = file.mkdirs();
        if(!flag){
            throw new DBAppException("Can't create table folder");
        }
        serialize(this, "src\\main\\resources\\data\\"+tableName+"\\"+tableName);
    }


    public void serialize(Object obj, String fileLocation) {
        try{
        FileOutputStream fos = new FileOutputStream(fileLocation);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(obj);
        fos.close();
        }catch(Exception e){
            System.out.println("Problem in serializing");
        }
    }

    public static Object deserialize(String fileLocation) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileLocation);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object obj = ois.readObject();
        ois.close();
        return obj;

    }

/*
    public static void printTable(String tableName) throws IOException, ClassNotFoundException {
        Table t = (Table)deserialize(tableName);

        for(int Key : t.pages.keySet()){
            Page p = (Page) deserialize(t.pages.get(Key));
            for(int j=0; j<p.rows.size(); j++){
                String r = "";
                for(Object obj : p.rows.get(j).values()){
                    r=r+obj+"-";
                }
                System.out.println(r);
            }
        }
    }
*/

    public static void main(String[] args) throws Exception {
    }
}


