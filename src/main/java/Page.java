import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

public class Page implements Serializable {
    int pageId ;
    Table table;
    Vector<Hashtable<String,Object>> rows;
    int min=1;  //size
    int max;


    public Page(Table table){
        this.pageId = table.pages.size();
        this.table=table;
        rows = new Vector<Hashtable<String,Object>>();
        Properties prop = new Properties();
        String fileName = "src\\main\\resources\\DBApp.config";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
            max= Integer.parseInt(prop.getProperty("MaximumRowsCountinTablePage"));
        } catch(Exception e){
            System.out.println("error in finding config file");
        }
    }

    public int size() {
        return this.rows.size();
    }

    public void remove(Hashtable<String, Object> Data) {
        this.rows.remove(Data);
    }

}
