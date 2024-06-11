import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.util.*;

public class DBApp {
    String mainPath = "src\\main\\resources\\";
    String octreeCurrentName = null;
    String currentX;
    String currentY;
    String currentZ;

    public void init(){
        try {
            File file = new File(mainPath+"metadata.csv");
            file.createNewFile();
            CSVWriter writer = new CSVWriter(new FileWriter(mainPath+"metadata.csv"));
            String line1[] = {"Table Name", "Column Name", "Column Type", "ClusteringKey", "IndexName","IndexType", "min", "max"};
            writer.writeNext(line1);
            writer.flush();
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    // following method creates an octree
    // depending on the count of column names passed.
    // If three column names are passed, create an octree.
    // If only one or two column names is passed, throw an Exception.
    public void createIndex(String strTableName, String[] strarrColName) throws DBAppException{
        if(strarrColName.length != 3){
            throw new DBAppException("Index must be done on exactly 3 columns");
        }
        if(indexExist(strTableName,strarrColName[0]) || indexExist(strTableName,strarrColName[1]) || indexExist(strTableName,strarrColName[2])){
            throw new DBAppException("Index already exist on one of the columns");
        }
        String col1dt = getDataType(strTableName,strarrColName[0]);
        String col2dt = getDataType(strTableName,strarrColName[1]);
        String col3dt = getDataType(strTableName,strarrColName[2]);
        Object MaxX;
        Object MinY;
        Object MaxY;
        Object MinZ;
        Object MaxZ;
        Object MinX;
        if (col1dt.equalsIgnoreCase("java.lang.Integer")) {
            MaxX =Integer.parseInt((String) getMax(strTableName,strarrColName[0]));
            MinX =Integer.parseInt((String) getMin(strTableName,strarrColName[0]));
        } else if (col1dt.equalsIgnoreCase("java.lang.Double")) {
            MaxX = Double.parseDouble((String) getMax(strTableName,strarrColName[0]));
            MinX = Double.parseDouble((String) getMin(strTableName,strarrColName[0]));
        } else if (col1dt.equalsIgnoreCase("java.util.Date")) {
            MaxX = (Date) getMax(strTableName,strarrColName[0]);
            MinX = (Date)getMin(strTableName,strarrColName[0]);
        }else{
            MaxX = getMax(strTableName,strarrColName[0]);
            MinX = getMin(strTableName,strarrColName[0]);
        }
        if (col2dt.equalsIgnoreCase("java.lang.Integer")) {
            MinY = Integer.parseInt((String)getMin(strTableName,strarrColName[1]));
            MaxY = Integer.parseInt((String)getMax(strTableName,strarrColName[1]));
        } else if (col2dt.equalsIgnoreCase("java.lang.Double")) {
            MinY = Double.parseDouble((String) getMin(strTableName,strarrColName[1]));
            MaxY = Double.parseDouble((String) getMax(strTableName,strarrColName[1]));
        } else if (col2dt.equalsIgnoreCase("java.util.Date")) {
            MinY = (Date)getMin(strTableName,strarrColName[1]);
            MaxY = (Date)getMax(strTableName,strarrColName[1]);
        }else{
            MinY = getMin(strTableName,strarrColName[1]);
            MaxY = getMax(strTableName,strarrColName[1]);
        }
        if (col3dt.equalsIgnoreCase("java.lang.Integer")) {
            MinZ = Integer.parseInt((String)getMin(strTableName,strarrColName[2]));
            MaxZ = Integer.parseInt((String)getMax(strTableName,strarrColName[2]));
        } else if (col3dt.equalsIgnoreCase("java.lang.Double")) {
            System.out.println("Z at creating time is double" + col3dt);
            MinZ = Double.parseDouble((String) getMin(strTableName,strarrColName[2]));
            MaxZ = Double.parseDouble((String) getMax(strTableName,strarrColName[2]));
        } else if (col3dt.equalsIgnoreCase("java.util.Date")) {
            MinZ = (Date)getMin(strTableName,strarrColName[2]);
            MaxZ = (Date)getMax(strTableName,strarrColName[2]);
        }else{
            MinZ = getMin(strTableName,strarrColName[2]);
            MaxZ = getMax(strTableName,strarrColName[2]);
        }

        String name = strarrColName[0]+""+strarrColName[1]+""+strarrColName[2] +"Index";
        System.out.println("Z instance of double : " + (MinZ instanceof Double));
        Octree octree = new Octree(MinX,MaxX,MinY,MaxY,MinZ,MaxZ, name , strarrColName[0],strarrColName[1],strarrColName[2]) ;
        Table table = (Table) deserialize("src\\main\\resources\\data\\"+strTableName+"\\"+strTableName);
        if(table.pages.size()>0){
            createHelper(strTableName, octree, strarrColName);

        }else{
            octree.serialize(strTableName , name , octree);
        }
        updateMetaDataFile(strTableName,strarrColName , name);
        serialize(table, "src\\main\\resources\\data\\"+strTableName+"\\"+strTableName);
        System.out.println("printing: ");
        System.out.println(octree.toString());
        System.out.println("Printing the tree after creating");
        printTree(strTableName,name);

    }
    public static void printTree(String strTableName,String octreename){
        Octree octree = Octree.deserializeOctree(strTableName,octreename);
        System.out.println(octree.toString());
        octree.serialize(strTableName,octreename,octree);
    }
    private void updateMetaDataFile(String strTableName, String[] strarrColName, String name) {

    try{
        CSVReader reader = new CSVReader(new FileReader(mainPath+"metadata.csv"));
        List<String[]> lines =reader.readAll();
        File file = new File(mainPath+"metadata.csv");
        file.createNewFile();
        CSVWriter writer = new CSVWriter(new FileWriter(mainPath+"metadata.csv")) ;

        for (String[] line : lines) {
            if (line[0].equals(strTableName) &&(line[1].equals(strarrColName[0])||line[1].equals(strarrColName[1])||line[1].equals(strarrColName[2]))) {
                line[4] = name;
                line[5] = "Octree";
            }
            writer.writeNext(line);
        }
        writer.flush();
        writer.close();
    } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
    } catch (IOException e) {
        throw new RuntimeException(e);
    } catch (CsvException e) {
        throw new RuntimeException(e);
    }
    }
    private void createHelper(String strTableName, Octree tree, String[] colNames){
        System.out.println("Entered createHelper Method");
        Table table = (Table) deserialize("src\\main\\resources\\data\\"+strTableName+"\\"+strTableName);
        String col1dt = getDataType(strTableName,colNames[0]);
        String col2dt = getDataType(strTableName,colNames[1]);
        String col3dt = getDataType(strTableName,colNames[2]);
        Object X = null;
        Object Y = null;
        Object Z = null;

        for(String path : table.pages.values()){
            System.out.println(path);
            Page page = (Page) deserialize(path);
            for(int i = 0; i < page.rows.size(); i++ ){
                System.out.println(page.rows.get(i));
                if (col1dt.equalsIgnoreCase("java.lang.Integer")) {
                    X = (Integer) page.rows.get(i).get(colNames[0]);
                } else if (col1dt.equalsIgnoreCase("java.lang.Double")) {
                    X = Double.parseDouble(String.valueOf(page.rows.get(i).get(colNames[0])));
                    System.out.println("X instance of double :" + (X instanceof Double));

                } else if (col1dt.equalsIgnoreCase("java.util.Date")) {
                    X = (Date) page.rows.get(i).get(colNames[0]);
                }else if (col1dt.equalsIgnoreCase("java.lang.String")){
                    X = page.rows.get(i).get(colNames[0]);
                }
                if (col2dt.equalsIgnoreCase("java.lang.Integer")) {
                    Y= (Integer) page.rows.get(i).get(colNames[1]);
                } else if (col2dt.equalsIgnoreCase("java.lang.Double")) {
                    System.out.println("Y instance of double :" + (Y instanceof Double) );
                    Y = ((Double) page.rows.get(i).get(colNames[1]));
                    System.out.println("Y instance of double :" + (Y instanceof Double) );
                } else if (col2dt.equalsIgnoreCase("java.util.Date")) {
                    Y= (Date) page.rows.get(i).get(colNames[1]);
                }else if(col2dt.equalsIgnoreCase("java.lang.String")){
                    Y= page.rows.get(i).get(colNames[1]);
                }
                if (col3dt.equalsIgnoreCase("java.lang.Integer")) {
                    Z = (Integer) page.rows.get(i).get(colNames[2]);
                } else if (col3dt.equalsIgnoreCase("java.lang.Double")) {
                    Z= Double.parseDouble(String.valueOf(page.rows.get(i).get(colNames[2])));
                    System.out.println("Z instance of double :" + (Z instanceof Double) );

                } else if (col3dt.equalsIgnoreCase("java.util.Date")) {
                    Z=(Date) page.rows.get(i).get(colNames[2]);
                }else if(col3dt.equalsIgnoreCase("java.lang.String")){
                    Z= page.rows.get(i).get(colNames[2]);
                }
                    tree.insert(X,Y, Z,path, page.rows.get(i).get(table.primaryKey));
            }
            serialize(page, path);
        }
        System.out.println("before serializing: "+ tree);
        tree.serialize(strTableName,tree.name,tree);
        serialize(table, "src\\main\\resources\\data\\"+strTableName+"\\"+strTableName );
    }

    private boolean indexExist(String strTableName, String columnName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(mainPath+"metadata.csv"));
            String strLine = "";
            while ((strLine = br.readLine()) != null  ) {
                String[] content = strLine.split(",");
                if (content[0].substring(1, content[0].length() - 1).equalsIgnoreCase(strTableName)&& content[1].substring(1, content[1].length() - 1).equalsIgnoreCase(columnName) ) {
                    return !content[4].substring(1, content[4].length() - 1).equalsIgnoreCase("null");
                }
            }

            br.close();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return false;
    }


    public Object getMin(String strTableName, String columnName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(mainPath+"metadata.csv"));
            String strLine = "";
            while ((strLine = br.readLine()) != null  ) {
                String[] content = strLine.split(",");
                if (content[0].substring(1, content[0].length() - 1).equalsIgnoreCase(strTableName)&& content[1].substring(1, content[1].length() - 1).equalsIgnoreCase(columnName) ) {
                    return content[6].substring(1, content[6].length() - 1);
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }
    public Object getMax(String strTableName, String columnName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(mainPath+"metadata.csv"));
            String strLine = "";
            while ((strLine = br.readLine()) != null  ) {
                String[] content = strLine.split(",");
                if (content[0].substring(1, content[0].length() - 1).equalsIgnoreCase(strTableName) && content[1].substring(1, content[1].length() - 1).equalsIgnoreCase(columnName) ) {
                    return content[7].substring(1, content[7].length() - 1);
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void writeInMetaDataFile(String strTableName, String strClusteringKeyColumn, Hashtable<String,String> htblColNameType,
                                    Hashtable<String,String> htblColNameMin, Hashtable<String,String> htblColNameMax){
        try{
            CSVWriter writer = new CSVWriter(new FileWriter(mainPath+"metadata.csv",true));
            for(String columnName :htblColNameType.keySet() ){
                String line[] = {strTableName, columnName,htblColNameType.get(columnName), String.valueOf((strClusteringKeyColumn==columnName)), "null", "null",String.valueOf(htblColNameMin.get(columnName)),String.valueOf(htblColNameMax.get(columnName))};
                writer.writeNext(line);
                writer.flush();
            }
            writer.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void createTable(String strTableName, String strClusteringKeyColumn, Hashtable<String,String> htblColNameType,
                            Hashtable<String,String> htblColNameMin, Hashtable<String,String> htblColNameMax) throws DBAppException {
        if(CheckIfTableExist(strTableName)){
            throw new DBAppException("Table already Exists");
        }
        if(!CheckiIfTableHasPrimaryKey(strClusteringKeyColumn,htblColNameType)){
            throw new DBAppException("Table doesn't have a primary key");
        }
//throw an error if table already exists in metaData file
        Table table = new Table (strTableName,strClusteringKeyColumn,htblColNameType,htblColNameMin,htblColNameMax);
        writeInMetaDataFile(strTableName, strClusteringKeyColumn, htblColNameType, htblColNameMin, htblColNameMax);
    }

    public void insertIntoTable(String strTableName, Hashtable<String,Object> htblColNameValue) throws DBAppException{
        //check metadata
        //metadata returns all octrees' name
        //method(coulmnName-> "Name") returns "NameAgeGpaOctree"
       // System.out.println("Entered insert Method");
        if(!CheckIfTableExist(strTableName)){
            throw new DBAppException("Table doesn't Exist ");
        }

        if(!checkDataType(htblColNameValue, strTableName)){
            throw new DBAppException("Violation of table data types " );
        }
        if(!CheckIfPrimaryKeyExist(htblColNameValue,strTableName)){
            throw new DBAppException("Record doesn't have primary Key " );
        }
        if(!CheckMinandMax(htblColNameValue, strTableName)){
            throw new DBAppException("Violation of min and max of each column ");
        }
       // System.out.println("I passed all exceptions");
        boolean flag = false;

        Table table = (Table) deserialize( mainPath+"data\\"+strTableName+"\\"+strTableName);

        for (String key :htblColNameValue.keySet()){
            if(indexExist(strTableName,key)){
                flag = true;
            }
        }
        Octree octree = null;
        if(flag){
            System.out.println("There is an Octree");
            String oName = getOctree(strTableName);
            octree = Octree.deserializeOctree(strTableName,oName);

        }
        // we shouldn't check on vector of type page, we should check on the size of the hashtable(pageid, page path)
        //create new page

        if (table.pages.size() == 0) {
            Page p = new Page(table);
           // System.out.println("I created a new Page");
            table.pages.put(p.pageId, mainPath+"data\\"+table.tableName+"\\"+ table.tableName + "_" + p.pageId);
           // System.out.println("added page to pages vector");
            p.rows.add(htblColNameValue);
           // System.out.println("inserted hashtable");
            if(octree != null){
                System.out.println("Octree is not null");
                Vector<String> v = arrangeAttributes(table,htblColNameValue,octree);
                octree.insert(htblColNameValue.get(v.get(0)),htblColNameValue.get(v.get(1)),htblColNameValue.get(v.get(2)),mainPath+"data\\"+table.tableName+"\\"+ table.tableName + "_" + p.pageId,htblColNameValue.get(table.primaryKey));
                octree.serialize(strTableName,octree.name,octree);

            }

            System.out.println("entered awel data "+ p.rows);
            serialize(p,  mainPath+"data\\"+table.tableName+"\\"+table.tableName + "_" + p.pageId);
            System.out.println("Entered where no pages " + htblColNameValue.get(table.primaryKey));
        } else {
            int pageIdtoInsertinto = checkPageforInsert(table, htblColNameValue);    // returned page to insert into
            //System.out.println("Entered where there is pages ");
            if (pageIdtoInsertinto != 404) {
                //  System.out.println("Entered 1    :   " + htblColNameValue.get(table.primaryKey) );
                Page p = (Page) deserialize(table.pages.get(pageIdtoInsertinto)); //deserialize the page using pageID as a key
                int indextoInsertinto = binarySearch(p, htblColNameValue.get(table.primaryKey));
                if(checkIndex(table, p,htblColNameValue, indextoInsertinto )==1){
                    indextoInsertinto++;
                }
                if(p.rows.size()!=indextoInsertinto){
                    //     System.out.println("Entered line 73");
                    //   System.out.println(p.rows.get(indextoInsertinto).get(table.primaryKey));
                    // System.out.println(htblColNameValue.get(table.primaryKey));
                    if (compareTo(p.rows.get(indextoInsertinto).get(table.primaryKey) ,htblColNameValue.get(table.primaryKey))==0) {
                        //System.out.println("Entered throw error in insert "+ htblColNameValue.get(table.primaryKey) );
                        //throw error using DBAppException "Record already inserted , Can't have a duplicate in primary key"
                        throw new DBAppException("Cannot enter a primary that already exists" );
                    }
                }
                if (indextoInsertinto == p.rows.size()) {     //p.rows.get(indextoInsertinto) == null
                    //System.out.println("Entered 1a "+ htblColNameValue.get(table.primaryKey) );
                    p.rows.add(indextoInsertinto,htblColNameValue);
                    if(octree != null){
                        Vector<String> v = arrangeAttributes(table,htblColNameValue,octree);
                        octree.insert(htblColNameValue.get(v.get(0)),htblColNameValue.get(v.get(1)),htblColNameValue.get(v.get(2)),table.pages.get(pageIdtoInsertinto),htblColNameValue.get(table.primaryKey));
                        octree.serialize(strTableName,octree.name,octree);

                    }
                    checkviolation(p, table,octree);
                    //System.out.println("Index to insert into : "+indextoInsertinto);
                    //System.out.println("Vector after insertion: " +p.rows);
                    serialize(p, table.pages.get(p.pageId));
                }  else {
                    //System.out.println("Entered 1c "+ htblColNameValue.get(table.primaryKey));
                    p = shiftInPlace(p,indextoInsertinto,htblColNameValue);
                    if(octree != null){
                        Vector<String> v = arrangeAttributes(table,htblColNameValue,octree);
                        octree.insert(htblColNameValue.get(v.get(0)),htblColNameValue.get(v.get(1)),htblColNameValue.get(v.get(2)),table.pages.get(pageIdtoInsertinto),htblColNameValue.get(table.primaryKey));
                        octree.serialize(strTableName,octree.name,octree);

                    }
                    checkviolation(p, table,octree);
                    //System.out.println("Vector after insertion: " +p.rows);
                    //System.out.println("Index to insert into : "+indextoInsertinto);
                    serialize(p, table.pages.get(p.pageId));
                }
            } else {
                //System.out.println("Entered 2 "+ htblColNameValue.get(table.primaryKey) );
                createPage(table, htblColNameValue,octree);
            }

        }
        serialize(table,mainPath+"data\\"+table.tableName+"\\"+ table.tableName);

//throw an error if table doesn't exist
    }

    private Vector<String> arrangeAttributes(Table table, Hashtable<String, Object> htblColNameValue, Octree octree) {
        String columnX = "" ;
        String columnY = "" ;
        String columnZ = "" ;
        String comparingString;
        Vector<String> vector = new Vector();


        for (int i =0 ; i< 3; i++){
            for(String key:htblColNameValue.keySet()){
                if (i==0 ){
                    comparingString = octree.name.substring(0,key.length());
                    if(comparingString.equals(key)){
                        columnX = key;
                    }
                }
                if(i==1){
                    comparingString = octree.name.substring(octree.name.length()-5-(key.length()),octree.name.length()-5);
                    if(comparingString.equals(key)){
                        columnZ = key;
                    }

                }
                if (i==2){
                    comparingString = octree.name.substring(columnX.length(),octree.name.length()-5-columnZ.length()-key.length());
                    if(comparingString.equals(key)) {
                        columnY = key;
                    }
                }

            }
            vector.add(0,columnX);
            vector.add(1,columnY);
            vector.add(2,columnZ);

        }
        return vector;
    }

    private String getOctree(String strTableName) {
        try {
            BufferedReader br = new BufferedReader( new FileReader(mainPath+"metadata.csv"));
            System.out.println("");
            String strLine = "";
            while( (strLine = br.readLine()) != null){
                String[] content = strLine.split(",");

                if(content[0].substring(1, content[0].length()-1).equalsIgnoreCase(strTableName)){
                    if (!(content[4].substring(1, content[4].length()-1).equals("null"))){
                        return content[4].substring(1, content[4].length()-1);
                    }
                }
            }
            return null;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void createPage(Table table, Hashtable<String, Object> h, Octree octree)  {
        //System.out.println("Creating new Page...... " );
        Page newPage = new Page(table);
        newPage.rows.insertElementAt(h, 0);
        if(octree != null){
            Vector<String> v = arrangeAttributes(table,h,octree);
            octree.insert(h.get(v.get(0)),h.get(v.get(1)),h.get(v.get(2)),table.pages.get(newPage.pageId),h.get(table.primaryKey));
            octree.serialize(table.tableName,octree.name,octree);

        }
        table.pages.put(newPage.pageId,  mainPath+"data\\"+newPage.table.tableName+"\\"+newPage.table.tableName + "_" + newPage.pageId);
        //System.out.println("New Page records:" + newPage.rows);
        serialize(newPage, table.pages.get(newPage.pageId));

    }
    public Page shiftInPlace(Page p, int indexToshiftTo, Hashtable<String,Object> newEntry){

        p.rows.add(p.rows.size(), p.rows.get( p.rows.size()-1));
        for(int i = p.rows.size()-2; i> indexToshiftTo-1; i--) {
            p.rows.set(i+1, p.rows.get(i));
        }
        p.rows.set(indexToshiftTo, newEntry);
        return p;
    }
    public void checkviolation(Page p, Table table,Octree octree)  {

        if (p.rows.size() > p.max) {
            //  System.out.println("Violation occured");

            Hashtable<String, Object> h = p.rows.get(p.rows.size() - 1);
            p.rows.removeElementAt(p.rows.size() - 1);
            if (octree != null){
                Vector<String> vector = arrangeAttributes(table,h, octree) ;
                octree.delete(h.get(vector.get(0)),h.get(vector.get(1)),h.get(vector.get(2)),h.get(table.primaryKey));

            }


            if (table.pages.containsKey(p.pageId + 1)) {
                //    System.out.println("Entered violation's if");
                Page newPage = (Page) deserialize(table.pages.get(p.pageId + 1));

                shiftDown(newPage, 0);
                serialize(newPage, table.pages.get(newPage.pageId));
                checkviolation(newPage, table, octree);
            } else {
                //  System.out.println("Entered violation's else");
                createPage(table, h,octree);
            }


        }
        if(octree!=null)
        octree.serialize(table.tableName,octree.name,octree);
    }
    public void shiftDown(Page p, int index) {
        for (int i = p.rows.size() - 1; i >= index; i--) {
            p.rows.insertElementAt(p.rows.get(i), i + 1);

        }
    }
    public int checkIndex(Table table, Page p, Hashtable<String,Object> data, int oldindex) {
        if(compareTo(p.rows.get(oldindex).get(table.primaryKey),data.get(table.primaryKey))>0) {   //khaled ahmed
            return -1;
        }
        else if(compareTo(p.rows.get(oldindex).get(table.primaryKey),data.get(table.primaryKey))==0){
            return 0;
        }
        return 1;
    }

    public int checkPageforInsert(Table table,Hashtable<String, Object> data)  {
        //loop over all pages
        //check min and max record of each page
        //until the right page is found and return page
        int result;
        for (String path : table.pages.values()) {
            Page p = (Page) deserialize(path);
            Hashtable<String, Object> minRecord = p.rows.get(0);    //first value in the page
            Hashtable<String, Object> maxRecord = p.rows.get(p.rows.size() - 1);     //last value in the page
            int resultMin = compareTo(minRecord.get(table.primaryKey), data.get(table.primaryKey));
            int resultMax = compareTo(maxRecord.get(table.primaryKey), data.get(table.primaryKey));
            if (resultMin < 0 && resultMax > 0) {       //in between min and max2
                result = p.pageId;
                serialize(p, path);
                //System.out.println("Page to insert into:" + path);
                return result;
            }
            if (resultMin > 0) {                     // before min and there is space in the page
                result = p.pageId;
                serialize(p, path);
                //System.out.println("Page to insert into:" + path);
                return result;
            }
            if (resultMax < 0 && p.rows.size() < p.max) { // after max and there is space in the page
                result = p.pageId;
                serialize(p, path);
                //System.out.println("Page to insert into:" + path);
                return result;
            }
        }
        //System.out.println("Page to insert into:" + 404);
        return 404;
    }

    public void deleteFromTable(String strTableName, Hashtable<String,Object> htblColNameValue) throws DBAppException{

        if(!CheckIfTableExist(strTableName)){
            throw new DBAppException("Table doesn't Exists");
        }
        if(!checkDataType(htblColNameValue, strTableName)){
            throw new DBAppException("violation of column data type");
        }
        Table table = (Table) deserialize(mainPath+"data\\"+strTableName+"\\"+strTableName);

        if(getReferencesToModify(htblColNameValue,strTableName)!=null)
        {
            Vector<Reference> refrence = getReferencesToModify(htblColNameValue,strTableName);
            for(int i =0; i<refrence.size();i++){
                Page p = (Page)deserialize(refrence.get(i).getPagePath());
                ArrayList<Hashtable<String,Object>> DataTobeRemoved = new ArrayList<Hashtable<String,Object>>();

                for(Hashtable<String, Object> row : p.rows){//int i =0; i<p.rows.size(); i++
                    Boolean flag = true;

                    for(String key : htblColNameValue.keySet()){

                        if (compareTo(row.get(key),htblColNameValue.get(key)) != 0){
                            flag = false;
                        }
                    }
                    if (flag){
                        DataTobeRemoved.add(row);

                    }

                }
                Octree octree = Octree.deserializeOctree(strTableName,octreeCurrentName);
                for(Hashtable<String,Object> data: DataTobeRemoved){
                    p.rows.remove(data);
                    octree.delete(data.get(currentX),data.get(currentY),data.get(currentZ),data.get(table.primaryKey));
                }
                serialize(p,table.pages.get(p.pageId));
                octree.serialize(strTableName,octree.name,octree);
                serialize(table, mainPath+"data\\"+table.tableName+"\\"+table.tableName);
                return;

            }
        }


        int j = table.pages.size();
        for (String path : table.pages.values()){
            //System.out.println("Page Entered: "+ path);
            Page p = (Page) deserialize(path);
            ArrayList<Hashtable<String,Object>> DataTobeRemoved = new ArrayList<Hashtable<String,Object>>();
            //System.out.println("csizeee of pageeee  " + p.rows.size() );
            for(Hashtable<String, Object> row : p.rows){//int i =0; i<p.rows.size(); i++
                //  System.out.println("Data Name : " + row.get("id"));
                Boolean flag = true;
                //System.out.println("KeySet : "+ htblColNameValue.keySet());
                for(String key : htblColNameValue.keySet()){
                    // System.out.println("Key : "+ key);
                    // System.out.println(row.get(key)+" FIRST DATA TO COMPARE");
                    //System.out.println(htblColNameValue.get(key)+" SECOND DATA TO COMPARE");
                    if (compareTo(row.get(key),htblColNameValue.get(key)) != 0){
                        flag = false;
                        //   System.out.println("Result of comparison: " + compareTo(row.get(key),htblColNameValue.get(key)));
                        // System.out.println("row.get(key):   "+ row.get(key));
                        // System.out.println("htblColNameValue.get(key):   "+htblColNameValue.get(key));
                        //System.out.println("Entered if condition : "+  row.get(key));
                    }
                }
                if (flag){
                    DataTobeRemoved.add(row);
                    //System.out.println("I am inserted in arraylist!  "+ row.get(table.primaryKey));

                }

            }
            for(Hashtable<String,Object> data: DataTobeRemoved){
                p.rows.remove(data);
                //System.out.println("I am removed!!!!   "+ data.get(table.primaryKey));
            }
            serialize(p, table.pages.get(p.pageId));
        }
        checkviolationAfterDelete(table);

        serialize(table, mainPath+"data\\"+table.tableName+"\\"+table.tableName);


    }
    public void checkviolationAfterDelete(Table table)  {
        try{
            ArrayList <Integer> pathsToBeDeleted = new ArrayList<Integer>();
            for(String path: table.pages.values()){
                Page p = (Page) deserialize(path);
                String x  = path;
                if(p.rows.size()==0){
                    //  System.out.println("I am a page that should be deleted " + path);
                    pathsToBeDeleted.add(p.pageId);
                    //System.out.println( "Am I deleted? "+  Files.deleteIfExists(Path.of( x)));
                }else{
                    serialize(p, path);
                }
            }
            for(int pagesID : pathsToBeDeleted){
                File file = new File(table.pages.remove(pagesID));
                file.deleteOnExit();
                table.pages.remove(pagesID);
            }
            //System.out.println(table.pages);
            serialize(table,mainPath+"data\\"+table.tableName+"\\"+ table.tableName);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            //   System.out.println("Problem with check violation method");
        }
    }

    public void updateTable(String strTableName, String strClusteringKeyValue, Hashtable<String,Object> htblColNameValue) throws DBAppException{
//throw an error if table doesn't exist
        if(!CheckIfTableExist(strTableName)){
            throw new DBAppException("Table doesn't Exist");
        }
        if(!checkDataType(htblColNameValue, strTableName)){
            throw new DBAppException("Violation of column data type");
        }
        if(CheckIfPrimaryKeyExist(htblColNameValue,strTableName)){
            throw new DBAppException("Primary key is not inserted");
        }
        if(!CheckMinandMax(htblColNameValue, strTableName)){
            throw new DBAppException("Violation of column min and max values");
        }
        Table table = (Table) deserialize(mainPath+"data\\"+strTableName+"\\"+strTableName);
        //System.out.println("Updating");
        Object clustringKey = strClusteringKeyValue;
        if(checkPrimaryKeyDataType(strTableName).equalsIgnoreCase("java.lang.Integer")){
            clustringKey = Integer.parseInt(strClusteringKeyValue);
        }
        else if(checkPrimaryKeyDataType(strTableName).equalsIgnoreCase("java.lang.Double")){
            clustringKey = Double.parseDouble(strClusteringKeyValue);
        }else if(checkPrimaryKeyDataType(strTableName).equalsIgnoreCase("java.util.Date")){
            clustringKey = Date.parse(strClusteringKeyValue);
        }
        if(indexExist(strTableName,table.primaryKey)) {
            String[] columns = {table.primaryKey};
            String octreeName = indexName(strTableName, columns);
            Octree octree = Octree.deserializeOctree(strTableName, octreeName);
            if (octree != null) {
                String axis = null;
                Object oldx;
                Object newx;
                Object oldy;
                Object newy;
                Object oldz;
                Object newz;

                if (compareTo(table.primaryKey, octree.xName) == 0) {
                    axis = "x";
                } else if (compareTo(table.primaryKey, octree.yName) == 0) {
                    axis = "y";
                } else {
                    axis = "z";
                }
                Vector<Reference> references = new Vector<Reference>();
                if (axis == "x") {
                    references = octree.find(strClusteringKeyValue, strClusteringKeyValue, getMin(strTableName, octree.yName), getMax(strTableName, octree.yName), getMin(strTableName, octree.zName), getMax(strTableName, octree.zName));
                } else if (axis == "y") {
                    references = octree.find(getMin(strTableName, octree.xName), getMax(strTableName, octree.xName), strClusteringKeyValue, strClusteringKeyValue, getMin(strTableName, octree.zName), getMax(strTableName, octree.zName));
                } else {
                    references = octree.find(getMin(strTableName, octree.xName), getMax(strTableName, octree.xName), getMin(strTableName, octree.yName), getMax(strTableName, octree.yName), strClusteringKeyValue, strClusteringKeyValue);
                }
                for (Reference r : references) {
                    Page p = (Page) deserialize(r.getPagePath());
                    for (Hashtable h : p.rows) {
                        if (compareTo(h.get(table.primaryKey), strClusteringKeyValue) == 0) {
                            oldx = h.get(octree.xName);
                            oldy = h.get(octree.yName);
                            oldz = h.get(octree.zName);
                            for (String key : htblColNameValue.keySet()) {
                                h.replace(key, htblColNameValue.get(key));
                            }
                            newx = h.get(octree.xName);
                            newy = h.get(octree.yName);
                            newz = h.get(octree.zName);
                            octree.update(oldx, oldy, oldz, newx, newy, newz, r.getPagePath(), strClusteringKeyValue);
                        }
                    }
                    serialize(p, r.getPagePath());
                }
                serialize(table, mainPath + "data\\" + strTableName + "\\" + strTableName);
                octree.serialize(strTableName,octree.name,octree);
            }

        }
        int pageID = checkPageforUpdate(table,clustringKey );
        if(pageID==404){
            throw new DBAppException("No page found");


            //throw an error from DBAppException
        }
        else{
            //  System.out.println("page found");
            // System.out.println("Page to update into : "+ pageID);
            Page p = (Page) deserialize(table.pages.get(pageID));
            // System.out.println("page path:"+   table.pages.get(pageID));

            int indexToUpdate = binarySearch(p, clustringKey);
            if(compareTo(p.rows.get(indexToUpdate).get(table.primaryKey),clustringKey)==0){
                //    System.out.println("right index found");
                //update value
                for(String key : htblColNameValue.keySet()){
                    //      System.out.println("I entered the for loop");
                    //p.rows.get(indexToUpdate).put(key, updateRequirments.get(key));
                    //p.rows.get(indexToUpdate).replace(key,p.rows.get(indexToUpdate).get(key),updateRequirments.get(key) );
                    p.rows.get(indexToUpdate).replace(key, htblColNameValue.get(key));
                }
            }
            else{
                //System.out.println("wrong index found");
                throw new DBAppException("Data not found");
            }
            serialize(p,table.pages.get(p.pageId));
        }
        serialize(table,mainPath+"data\\"+table.tableName+"\\"+table.tableName);

    }
    public int checkPageforUpdate(Table table,Object primaryKeyValue) {
        int result;
        //System.out.println("checking for page to update");
        for (String path : table.pages.values()) {
            Page p = (Page) deserialize(path);
            Hashtable<String, Object> minRecord = p.rows.get(0);    //first value in the page
            Hashtable<String, Object> maxRecord = p.rows.get(p.rows.size() - 1);     //last value in the page
            int resultMin = compareTo(minRecord.get(table.primaryKey), primaryKeyValue);
            int resultMax = compareTo(maxRecord.get(table.primaryKey), primaryKeyValue);
            if (resultMin <= 0 && resultMax >= 0) {       //in between min and max
                result = p.pageId;
                serialize(p, path);
                return result;
            }
        }
        return 404;
    }
    public Vector<Reference> getReferencesToModify (Hashtable<String,Object> columnNameValue ,String strTableName ) {
        //loop over columnnAMES
        //columnName  leh index
        // if rest of attribute are there
        // call index find 3la tool
        //else complete the find parameters with the metadata min and max
        Hashtable<String, String> indexAttributes = new Hashtable<>();
        String[] columns = new String[1];
        for (String columnName : columnNameValue.keySet()) {
            columns[0] = columnName;
            if (indexExist(strTableName, columnName)) {
                indexAttributes.put(columnName, indexName(strTableName, columns));
            }
        }
        Hashtable<String, Integer> valueCounts = new Hashtable<>();
        for (String value : indexAttributes.values()) {
            valueCounts.put(value, valueCounts.getOrDefault(value, 0) + 1);
        }


        // Check if any value occurs three times

        Object minX = null;
        Object maxX = null;
        Object minY = null;
        Object maxY = null;
        Object minZ = null;
        Object maxZ = null;
        String columnAxis;
        String xName = null;
        String yName = null;
        String zName = null;
        //find with less than 3 attributes
        for (String key : valueCounts.keySet()) {
            if (valueCounts.get(key) == 2 || valueCounts.get(key) == 1 || valueCounts.get(key) == 3) {
                Octree octree = Octree.deserializeOctree(strTableName, key);
                if (octree != null) {
                    ArrayList<String> columnNames = new ArrayList<>();
                    for (String columnName : indexAttributes.keySet()) {
                        if (indexAttributes.get(columnName).contains(key)) {
                            columnNames.add(columnName);
                        }
                    }
                    for (int i = 0; i < columnNames.size(); i++) {
                        if (columnNames.get(i).substring(0, columnNames.get(i).length()).equals(octree.name))
                            columnAxis = "x";
                        else if (columnNames.get(i).substring(octree.name.length() - 5 - columnNames.get(i).length(), octree.name.length() - 5).equals(octree.name))
                            columnAxis = "z";
                        else
                            columnAxis = "y";
                        if (columnAxis.equalsIgnoreCase("x")) {
                            minX = columnNameValue.get(columnNames.get(i));
                            maxX = columnNameValue.get(columnNames.get(i));
                            xName = columnNames.get(i);
                        } else if (columnAxis.equalsIgnoreCase("y")) {
                            minY = columnNameValue.get(columnNames.get(i));
                            maxY = columnNameValue.get(columnNames.get(i));
                            yName = columnNames.get(i);
                        } else if (columnAxis.equalsIgnoreCase("Z")) {
                            minZ = columnNameValue.get(columnNames.get(i));
                            maxZ = columnNameValue.get(columnNames.get(i));
                            zName = columnNames.get(i);
                        }
                    }
                    if (minX == null) {
                        minX = getMin(strTableName, octree.name.substring(0, octree.name.length() - 5 - yName.length() - zName.length()));
                        maxX = getMax(strTableName, octree.name.substring(0, octree.name.length() - 5 - yName.length() - zName.length()));
                    }
                    if (minY == null) {
                        minY = getMin(strTableName, octree.name.substring(octree.name.length() - xName.length(), octree.name.length() - 5 - zName.length()));
                        maxY = getMax(strTableName, octree.name.substring(octree.name.length() - xName.length(), octree.name.length() - 5 - zName.length()));
                    }
                    if (minZ == null) {
                        minZ = getMin(strTableName, octree.name.substring(octree.name.length() - yName.length() - xName.length(), octree.name.length() - 5));
                        maxZ = getMax(strTableName, octree.name.substring(octree.name.length() - yName.length() - xName.length(), octree.name.length() - 5));
                    }
                    octreeCurrentName = octree.name;
                    currentX = xName;
                    currentY = yName;
                    currentZ = zName;
                    return octree.find(minX, maxX, minY, maxY, minZ, maxZ);

                }
            }
        }
        return null;
    }

    public  Object deserialize(String fileLocation)  {
        try{
            FileInputStream fis = new FileInputStream(fileLocation);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            ois.close();
            return obj;
        }catch(Exception e){
            // System.out.println("Deserializing problem");
            System.out.println(e.getMessage());
        }
        return null;
    }
    public void serialize(Object obj, String fileLocation)  {
        try{
            FileOutputStream fos = new FileOutputStream(fileLocation);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            fos.close();
        }catch(Exception e){
            //    System.out.println("problem in serialize method");
            System.out.println(e.getMessage());
        }
    }
    public int binarySearch(Page page, Object x) {
        int l = 0, r = page.rows.size()-1;
        int m = l + (r - l) / 2;
        while (l <= r) {
            m = l + (r - l) / 2;

            int res = compareTo(x, page.rows.get(m).get(page.table.primaryKey));
            //      System.out.println("my object: "+ x);
            //    System.out.println("DATA Comparing to: "+page.rows.get(m).get(page.table.primaryKey));

            // Check if x is present at mid
            if (res == 0){
                //      System.out.println("This is my result because I already exist : "+ m);
                return m;                             //should be handled as primary key is already inserted
            }
            // If x greater, ignore left half
            if (res > 0){
                l = m + 1;
                //    System.out.println("This is my new low boundry : "+ l);
            }
            // If x is smaller, ignore right half
            else{
                r = m - 1;
                //  System.out.println("This is my new high boundry : " + r);
            }
        }
        //System.out.println("This is the binarySearch Result: "+ m);
        return m;
    }
    public int compareTo(Object o1, Object o2) {
        if (o1 instanceof Integer && o2 instanceof Integer) {
            if ((int) o1 == (int) o2)
                return 0;
            else if ((int) o1 > (int) o2)
                return 1;
            else
                return -1;

        } else if (o1 instanceof Double && o2 instanceof Double) {
            if (((Double) o1 ).equals(((Double) o2)))
                return 0;
            else if ((Double) o1 > (Double) o2)
                return 1;
            else
                return -1;

        } else if (o1 instanceof String && o2 instanceof String) {
            return (((String) o1).compareToIgnoreCase((String) o2));

        } else if (o1 instanceof Date && o2 instanceof Date) {
            return (((Date) o1).compareTo((Date) o2));
        }
        return -1000000;
    }
    public  void printTable(String tableName) {
        Table t = (Table)deserialize(mainPath+"data\\"+tableName+"\\"+tableName);

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
        serialize(t, mainPath+"data\\"+tableName+"\\"+tableName);
    }
    public Boolean CheckiIfTableHasPrimaryKey(String PrimaryKey, Hashtable<String,String> htblColNameType ){
        if (PrimaryKey != "") {

            if (htblColNameType.containsKey(PrimaryKey))
                return true;
        }
        return false;
    }
    public boolean CheckIfPrimaryKeyExist (Hashtable<String, Object> htblColNameValue, String strTableName){

        try {
            BufferedReader br = new BufferedReader(new FileReader(mainPath+"metadata.csv"));
            String strLine = "";
            while ((strLine = br.readLine()) != null) {
                String[] content = strLine.split(",");

                if ( content[0].substring(1, content[0].length()-1).equalsIgnoreCase(strTableName)){

                    if (content[3].substring(1, content[3].length()-1).equalsIgnoreCase("true"))
                    {

                        if (htblColNameValue.containsKey(content[1].substring(1, content[1].length()-1)))
                            return true;

                    }
                }

            }
            return false;
        }

        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    public boolean CheckMinandMax(Hashtable<String, Object> htblColNameValue, String strTableName) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(mainPath+"metadata.csv"));
            String strLine = "";
            int count = 0 ;
            //  System.out.println(htblColNameValue);
            while ((strLine = br.readLine()) != null   && count <= htblColNameValue.size()) {
                String[] content = strLine.split(",");
                count++;

                if (content[0].substring(1, content[0].length()-1).equalsIgnoreCase(strTableName)){
                    Object value = htblColNameValue.get(content[1].substring(1, content[1].length()-1));
                    Object min = content[6].substring(1, content[6].length()-1);
                    Object max = content[7].substring(1, content[7].length()-1);
                    String dataType = content[2].substring(1, content[2].length()-1);

                    if (dataType.equalsIgnoreCase("java.lang.Integer")){
                        min = Integer.parseInt(content[6].substring(1, content[6].length()-1));
                        max = Integer.parseInt(content[7].substring(1, content[7].length()-1));
                        //            System.out.println("I am an integer");
                    }
                    else if (dataType .equalsIgnoreCase("java.lang.String")){
                        min = String.valueOf(content[6].substring(1, content[6].length()-1)).toLowerCase();
                        max = String.valueOf(content[7].substring(1, content[7].length()-1)).toLowerCase();
                        //          System.out.println("I am a String");
                    }
                    else if(dataType.equalsIgnoreCase( "java.lang.Double")){
                        min = Double.parseDouble(content[6].substring(1, content[6].length()-1));
                        max =  Double.parseDouble(content[7].substring(1, content[7].length()-1));
                        //        System.out.println("I am a double");
                    }
                    else if (dataType .equalsIgnoreCase( "java.util.Date")) {
                        min = new Date(content[6].substring(1, content[6].length()-1));
                        max = new Date(content[7].substring(1, content[7].length()-1));
                        //      System.out.println("I am a Date");
                    }

                    //System.out.println("printed out of the if "+"-------"+"I am  in range, The min is : "+ min + "The max is:  " + max + "  I am :  "+ value );
                    if (!(compareTo(min, value) <= 0 && compareTo(max, value ) >= 0)){
                        //  System.out.println("I am not with in range, The min is : "+ min + "The max is:  " + max + "I am : "+ value );
                        return false;
                    }
                }
            }
            return true;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public  Boolean checkDataType(Hashtable<String, Object> htblColNameValue, String strTableName){

        try {
            BufferedReader br = new BufferedReader( new FileReader(mainPath+"metadata.csv"));
            String strLine = "";
            int count  = 0;
            while( (strLine = br.readLine()) != null   && count< htblColNameValue.size()){
                String[] content = strLine.split(",");
                count++;
                if(content[0].substring(1, content[0].length()-1).equalsIgnoreCase(strTableName)){
                    Object value = htblColNameValue.get(content[1].substring(1, content[1].length()-1));
                    String dataType = content[2].substring(1, content[2].length()-1);
                    //System.out.println("I am table: "+ content[0]);
                    //System.out.println("Content[1]: "+ content[1]);

                    if (dataType.equalsIgnoreCase("java.lang.Integer")){
                        if (!(value instanceof java.lang.Integer )) {
                            //      System.out.println("I am not an int" + "   This is my value : " + value + " This is my supposed data Type: " + dataType);
                            return false;
                        }
                    }
                    else if (dataType.equalsIgnoreCase("java.lang.String")){
                        System.out.println("its a string " + content[0]);
                        if (!(value instanceof java.lang.String )){
                            //    System.out.println("I am not a String" + "   This is my value : " + value + " This is my supposed data Type: "+ dataType );
                            return false;
                        }
                    }
                    else if(dataType.equalsIgnoreCase( "java.lang.Double")){
                        if (!(value instanceof java.lang.Double )){
                            //  System.out.println("I am not a Double" + "   This is my value : " + value + " This is my supposed data Type: "+ dataType );
                            return false;
                        }
                    }
                    else if (dataType .equalsIgnoreCase( "java.util.Date")) {
                        if (!(value instanceof java.util.Date )) {
                            //System.out.println("I am not a Date" + "   This is my value : " + value + " This is my supposed data Type: " + dataType);
                            return false;
                        }
                    }

                }
            }
            return true;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public String checkPrimaryKeyDataType( String strTableName){

        try {
            BufferedReader br = new BufferedReader( new FileReader(mainPath+"metadata.csv"));
            String strLine = "";

            while( (strLine = br.readLine()) != null   ){
                String[] content = strLine.split(",");
                if(content[0].substring(1, content[0].length()-1).equalsIgnoreCase(strTableName) && content[3].substring(1, content[3].length() - 1).equalsIgnoreCase("true")) {
                    String dataType = content[2].substring(1, content[2].length() - 1);
                    return dataType;
                }
            }
            return null;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public boolean CheckIfTableExist(String strTableName) {
        try {
            BufferedReader br = new BufferedReader( new FileReader(mainPath+"metadata.csv"));
            System.out.println("");
            String strLine = "";
            while( (strLine = br.readLine()) != null){
                String[] content = strLine.split(",");

                if(content[0].substring(1, content[0].length()-1).equalsIgnoreCase(strTableName)){
                    return true;
                }
            }
            return false;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    public Iterator selectFromTable(SQLTerm[] arrSQLTerms, String[] strarrOperators) throws DBAppException{
        for(int i =0 ; i< strarrOperators.length;i++){  //Check operations are valid
            if(strarrOperators[i]!="AND" && strarrOperators[i]!="OR" && strarrOperators[i]!="XOR"){
                throw new DBAppException("Wrong operator");
            }
        }
        if(useIndex(arrSQLTerms,strarrOperators)){
            return SelectWithIndex(arrSQLTerms,strarrOperators);
        }
        int count = 0;
        Vector<Hashtable<String,Object>> result;
        Vector<Hashtable<String,Object>> result1 = new Vector<Hashtable<String,Object>>();
        Vector<Hashtable<String,Object>> result2 = new Vector<Hashtable<String,Object>>();
        Table table = (Table)deserialize("src\\main\\resources\\data\\"+arrSQLTerms[0]._strTableName+"\\"+arrSQLTerms[0]._strTableName);

        for(int i = 0; i<arrSQLTerms.length; i++){
            System.out.println("I am the condition : " + arrSQLTerms[i]._objValue);
            Object value ;
            String DataType = getDataType(arrSQLTerms[i]._strTableName, arrSQLTerms[i]._strColumnName);
            if(DataType.equals("java.lang.Integer")){
                value = (Integer) arrSQLTerms[i]._objValue;
            }else if(DataType.equalsIgnoreCase("java.lang.Double")){
                System.out.println("I entered here");
                value = (Double) arrSQLTerms[i]._objValue;
            } else if (DataType.equals("java.util.Date")) {
                value =  (Date) arrSQLTerms[i]._objValue;
            }else{
                value = String.valueOf(arrSQLTerms[i]._objValue).toLowerCase();
            }

            for(String path : table.pages.values()){
                System.out.println("page path"+path);
                Page p = (Page)  deserialize(path);
                for(Hashtable row : p.rows) {
                    System.out.println("row: " + row);
                    if (arrSQLTerms[i]._strOperator.equalsIgnoreCase("=")) {
                        System.out.println("My value : " + row.get(arrSQLTerms[i]._strColumnName));
                        System.out.println("I should be : " + value);
                        System.out.println("count: "+ count);
                        System.out.println("Compare to : " +(compareTo(row.get(arrSQLTerms[i]._strColumnName), value) == 0) );
                        if (compareTo(row.get(arrSQLTerms[i]._strColumnName), value) == 0) {

                            if (count == 0) {
                                System.out.println("result one : " + row);
                                result1.add(row);
                            } else {
                                System.out.println("result two : " + row);
                                result2.add(row);
                            }
//                            count++;

                        }
                    } else if (arrSQLTerms[i]._strOperator.equalsIgnoreCase(">")) {    //age > 20
                        if (compareTo(row.get(arrSQLTerms[i]._strColumnName), value) > 0) {
                            System.out.println("My value : " + row.get(arrSQLTerms[i]._strColumnName));
                            System.out.println("I should be : " + value);
                            if (count == 0) {
                                result1.add(row);
                            } else {
                                result2.add(row);
                            }
//                            count++;

                        }
                    } else if (arrSQLTerms[i]._strOperator.equalsIgnoreCase("<")) { //age < 20
                        if (compareTo(row.get(arrSQLTerms[i]._strColumnName), value) < 0) {
                            System.out.println("My value : " + row.get(arrSQLTerms[i]._strColumnName));
                            System.out.println("I should be : " + value);
                            if (count == 0) {
                                result1.add(row);
                            } else {
                                result2.add(row);
                            }
//                            count++;

                        }
                    } else if (arrSQLTerms[i]._strOperator.equalsIgnoreCase("<=")) {
                        if (compareTo(row.get(arrSQLTerms[i]._strColumnName), value) <= 0) {
                            System.out.println("My value : " + row.get(arrSQLTerms[i]._strColumnName));
                            System.out.println("I should be : " + value);
                            if (count == 0) {
                                result1.add(row);
                            } else {
                                result2.add(row);
                            }
//                            count++;

                        }
                    } else if (arrSQLTerms[i]._strOperator.equalsIgnoreCase(">=")) {
                        if (compareTo(row.get(arrSQLTerms[i]._strColumnName), value) >= 0) {
                            System.out.println("My value : " + row.get(arrSQLTerms[i]._strColumnName));
                            System.out.println("I should be : " + value);
                            if (count == 0) {
                                result1.add(row);
                            } else {
                                result2.add(row);
                            }
//                            count++;

                        }
                    } else if (arrSQLTerms[i]._strOperator.equalsIgnoreCase("!=")) {
                        if (compareTo(row.get(arrSQLTerms[i]._strColumnName), value) != 0) {
                            System.out.println("My value : " + row.get(arrSQLTerms[i]._strColumnName));
                            System.out.println("I should be : " + value);
                            if (count == 0) {
                                result1.add(row);
                            } else {
                                result2.add(row);
                            }
                        }

                    }

                    if (count > 0 && i<arrSQLTerms.length) {
                        if (strarrOperators[count - 1].equalsIgnoreCase("AND")) {
                            System.out.println("Entered and");
                            result1.addAll(result2);
                            result1 = removeDuplicates(result1);
                            result2.clear();
                        } else if (strarrOperators[count - 1].equalsIgnoreCase("OR")) {
                            System.out.println("Entered or");
                            result1.addAll(result2);
                           // System.out.println("1st line after or");
                            result1 = removeDuplicates(result1);
                            //System.out.println("second line");
                            result2.clear();
                            //System.out.println(result1);
                            //System.out.println("third line");
                        } else if (strarrOperators[count - 1].equalsIgnoreCase("XOR")) {
                            System.out.println("Entered xor");
                            result1.addAll(result2);
                            removeAllDuplicateHashtables(result1);
                            result2.clear();
                        }
                    }
                }
                serialize(p, path);
            }
            count++;
        }
        serialize(table,"src\\main\\resources\\data\\"+arrSQLTerms[0]._strTableName+"\\"+arrSQLTerms[0]._strTableName );

        result = result1;
        return result.iterator();
    }

    private Iterator SelectWithIndex(SQLTerm[] arrSQLTerms, String[] strarrOperators) {
        String[] columnName = {arrSQLTerms[0]._strColumnName,arrSQLTerms[1]._strColumnName,arrSQLTerms[2]._strColumnName};
        Octree octree = Octree.deserializeOctree(arrSQLTerms[0]._strTableName,indexName(arrSQLTerms[0]._strTableName,columnName));
        Vector<Hashtable<String,Object>> resultOfFind = new Vector<Hashtable<String,Object>>();
        Object minX = null;
        Object maxX = null;
        Object minY = null;
        Object maxY = null;
        Object minZ = null;
        Object maxZ = null;

        String xNotEqual = null;
        String yNotEqual = null;
        String zNotEqual = null;




        for(int i = 0; i<arrSQLTerms.length; i++) {

            if (arrSQLTerms[i]._strOperator.equalsIgnoreCase("=")) {
                if(i==0){
                    minX = arrSQLTerms[i]._objValue ;
                    maxX = arrSQLTerms[i]._objValue;
                } else if (i==1) {
                    minY=arrSQLTerms[i]._objValue;
                    maxY=arrSQLTerms[i]._objValue;
                } else if (i==2) {
                    minZ=arrSQLTerms[i]._objValue;
                    maxZ=arrSQLTerms[i]._objValue;
                }
            } else if (arrSQLTerms[i]._strOperator.equalsIgnoreCase(">")) {    //age > 20
                if(i==0){
                    minX = arrSQLTerms[i]._objValue;
                    maxX = getMax(arrSQLTerms[i]._strTableName,arrSQLTerms[i]._strColumnName);
                } else if (i==1) {
                    minY=arrSQLTerms[i]._objValue;
                    maxY=getMax(arrSQLTerms[i]._strTableName,arrSQLTerms[i]._strColumnName);
                } else if (i==2) {
                    minZ=arrSQLTerms[i]._objValue;
                    maxZ=getMax(arrSQLTerms[i]._strTableName,arrSQLTerms[i]._strColumnName);
                }

            } else if (arrSQLTerms[i]._strOperator.equalsIgnoreCase("<")) { //age < 20
                if(i==0){
                    minX = getMin(arrSQLTerms[i]._strTableName,arrSQLTerms[i]._strColumnName);
                    maxX = arrSQLTerms[i]._objValue;
                } else if (i==1) {
                    minY=getMin(arrSQLTerms[i]._strTableName,arrSQLTerms[i]._strColumnName);
                    maxY=arrSQLTerms[i]._objValue;
                } else if (i==2) {
                    minZ=getMin(arrSQLTerms[i]._strTableName,arrSQLTerms[i]._strColumnName);
                    maxZ=arrSQLTerms[i]._objValue;
                }

            } else if (arrSQLTerms[i]._strOperator.equalsIgnoreCase("<=")) {
                if(i==0){
                    minX = getMin(arrSQLTerms[i]._strTableName,arrSQLTerms[i]._strColumnName);
                    maxX = arrSQLTerms[i]._objValue;
                } else if (i==1) {
                    minY=getMin(arrSQLTerms[i]._strTableName,arrSQLTerms[i]._strColumnName);
                    maxY=arrSQLTerms[i]._objValue;
                } else if (i==2) {
                    minZ=getMin(arrSQLTerms[i]._strTableName,arrSQLTerms[i]._strColumnName);
                    maxZ=arrSQLTerms[i]._objValue;
                }

            } else if (arrSQLTerms[i]._strOperator.equalsIgnoreCase(">=")) {
                if(i==0){
                    minX = arrSQLTerms[i]._objValue;
                    maxX = getMax(arrSQLTerms[i]._strTableName,arrSQLTerms[i]._strColumnName);
                } else if (i==1) {
                    minY=arrSQLTerms[i]._objValue;
                    maxY=getMax(arrSQLTerms[i]._strTableName,arrSQLTerms[i]._strColumnName);
                } else if (i==2) {
                    minZ=arrSQLTerms[i]._objValue;
                    maxZ=getMax(arrSQLTerms[i]._strTableName,arrSQLTerms[i]._strColumnName);
                }
            } else if (arrSQLTerms[i]._strOperator.equalsIgnoreCase("!=")) {
                /*return selectWithIndexNotEqual(arrSQLTerms,strarrOperators,octree);*/
                if(i==0){
                    minX = arrSQLTerms[i]._objValue;
                    maxX = arrSQLTerms[i]._objValue;
                    xNotEqual = "I am not equal";
                } else if (i==1) {
                    minY=arrSQLTerms[i]._objValue;
                    maxY=arrSQLTerms[i]._objValue;
                    yNotEqual = "I am not equal";
                } else if (i==2) {
                    minZ=arrSQLTerms[i]._objValue;
                    maxZ=arrSQLTerms[i]._objValue;
                    zNotEqual = "I am not equal";
                }

            }
        }
        Vector<Reference> references = octree.find(minX,maxX,minY,maxY,minZ,maxZ);

        if(!xNotEqual.equals(null) ){
            Vector<Reference> alldata = octree.find(getMin(arrSQLTerms[0]._strTableName,arrSQLTerms[0]._strColumnName),getMax(arrSQLTerms[0]._strTableName,arrSQLTerms[0]._strColumnName),minY,maxY,minZ,maxZ);
            alldata.removeAll(references);
            references=alldata;
        }
        else if (!yNotEqual.equals(null)){
            Vector<Reference> alldata= octree.find(minX,maxX,getMin(arrSQLTerms[1]._strTableName,arrSQLTerms[1]._strColumnName),getMax(arrSQLTerms[1]._strTableName,arrSQLTerms[1]._strColumnName),minZ,maxZ);
            alldata.removeAll(references);
            references=alldata;
        }
        else if (!zNotEqual.equals(null)){
            Vector<Reference> alldata = octree.find(minX,maxX,minY,maxY,getMin(arrSQLTerms[2]._strTableName,arrSQLTerms[2]._strColumnName),getMax(arrSQLTerms[2]._strTableName,arrSQLTerms[2]._strColumnName));
            alldata.removeAll(references);
            references=alldata;
        }
        Table table= (Table) deserialize("src\\main\\resources\\data\\"+arrSQLTerms[0]._strTableName+"\\"+arrSQLTerms[0]._strTableName);
        for(Reference r : references){
            Page p = (Page) deserialize(r.getPagePath());
            for(Hashtable h : p.rows){
                if(compareTo(h.get(table.primaryKey),r.getClustringkeyValue())==0){
                    resultOfFind.add(h);
                }
            }
            serialize(p,r.getPagePath());
        }
        serialize(table,"src\\main\\resources\\data\\"+arrSQLTerms[0]._strTableName+"\\"+arrSQLTerms[0]._strTableName);
        //select * from student where name = khaled and age = 20 and gpa = 2
        //select * from Person where name = khaled and age = 20 and gpa = 2 and gender = m and hobby = music and school = els
        return resultOfFind.iterator();
    }


    public Boolean useIndex(SQLTerm[] arrSQLTerms, String[] strarrOperators){
        for(int i = 0; i<strarrOperators.length;i++){
            if(!strarrOperators[i].equalsIgnoreCase("AND")){
                return false;
            }
        }
        Boolean flag = true;
        for(int i=0; i<arrSQLTerms.length;i++){
            if(! indexExist(arrSQLTerms[i]._strTableName, arrSQLTerms[i]._strColumnName)){
                return false;
            }
        }
        if(arrSQLTerms.length!=3){
            return false;
        }
        if(strarrOperators.length!=2){
            return false;
        }
        String[] columnName = {arrSQLTerms[0]._strColumnName,arrSQLTerms[1]._strColumnName,arrSQLTerms[2]._strColumnName};
        return checkIndexSame(arrSQLTerms[0]._strTableName, columnName );

    }

    private Boolean checkIndexSame(String strTableName, String[] columnName) {
        String indexName = "";
        int count = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(mainPath+"metadata.csv"));
            String strLine = "";
            while ((strLine = br.readLine()) != null) {
                String[] content = strLine.split(",");
                if (content[0].substring(1, content[0].length() - 1).equalsIgnoreCase(strTableName)&& content[1].substring(1, content[0].length() - 1).equalsIgnoreCase(columnName[count]) ) {
                    if(count ==0 ){
                        indexName = content[4].substring(1, content[7].length() - 1);
                    }
                    else{
                        if(!content[4].substring(1, content[7].length() - 1).equalsIgnoreCase(indexName)){
                            return false;
                        }
                    }
                    count++;
                }
            }
            return true;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return false;
    }
    public String indexName(String strTableName, String[] columnName){
        String indexName = "";
        int count = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(mainPath+"metadata.csv"));
            String strLine = "";
            while ((strLine = br.readLine()) != null) {
                String[] content = strLine.split(",");
                if (content[0].substring(1, content[0].length() - 1).equalsIgnoreCase(strTableName)&& content[1].substring(1, content[0].length() - 1).equalsIgnoreCase(columnName[count]) ) {
                    return content[4].substring(1, content[7].length() - 1);
                }
            }

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return "";
    }

    public static void removeAllDuplicateHashtables(Vector<Hashtable<String, Object>> vector) {
        // Count the occurrences of each Hashtable
        Hashtable<Hashtable<String, Object>, Integer> hashtableCounts = new Hashtable<>();
        for (Hashtable<String, Object> hashtable : vector) {
            hashtableCounts.put(hashtable, hashtableCounts.getOrDefault(hashtable, 0) + 1);
        }

        // Remove duplicates
        for (int i = vector.size() - 1; i >= 0; i--) {
            Hashtable<String, Object> hashtable = vector.get(i);
            if (hashtableCounts.get(hashtable) > 1) {
                vector.remove(i);
            }
        }
    }


    public static <T> Vector<T> removeDuplicates(Vector<T> vector) {
        Vector<T> result = new Vector<>();
        for (T element : vector) {
            if (!result.contains(element)) {
                result.add(element);
            }
        }
        return result;
    }
    public String getDataType( String strTableName, String columnName){
        try {
            BufferedReader br = new BufferedReader( new FileReader(mainPath+"metadata.csv"));
            String strLine = "";
            while( (strLine = br.readLine()) != null   ){
                String[] content = strLine.split(",");
                if(content[0].substring(1, content[0].length()-1).equalsIgnoreCase(strTableName) && content[1].substring(1, content[1].length() - 1).equalsIgnoreCase(columnName)) {
                    String dataType = content[2].substring(1, content[2].length() - 1);
                    return dataType;
                }
            }
            return null;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void insertCoursesRecords(DBApp dbApp, int limit) throws Exception {
        BufferedReader coursesTable = new BufferedReader(new FileReader("C:\\Users\\Lobna\\IdeaProjects\\DBII-Project\\courses_table.csv"));
        String record;
        Hashtable<String, Object> row = new Hashtable<>();
        int c = limit;
        if (limit == -1) {
            c = 1;
        }
        while ((record = coursesTable.readLine()) != null && c > 0) {
            String[] fields = record.split(",");


            int year = Integer.parseInt(fields[0].trim().substring(0, 4));
            int month = Integer.parseInt(fields[0].trim().substring(5, 7));
            int day = Integer.parseInt(fields[0].trim().substring(8));

            Date dateAdded = new Date(year - 1900, month - 1, day);

            row.put("date_added", dateAdded);

            row.put("course_id", fields[1]);
            row.put("course_name", fields[2]);
            row.put("hours", Integer.parseInt(fields[3]));

            dbApp.insertIntoTable("courses", row);
            row.clear();

            if (limit != -1) {
                c--;
            }
        }

        coursesTable.close();
    }

    private static void insertStudentRecords(DBApp dbApp, int limit) throws Exception {
        BufferedReader studentsTable = new BufferedReader(new FileReader("C:\\Users\\Lobna\\IdeaProjects\\DBII-Project\\students_table.csv"));
        String record;
        int c = limit;
        if (limit == -1) {
            c = 1;
        }

        Hashtable<String, Object> row = new Hashtable<>();
        while ((record = studentsTable.readLine()) != null && c > 0) {
            String[] fields = record.split(",");

            row.put("id", fields[0]);
            row.put("first_name", fields[1]);
            row.put("last_name", fields[2]);

            int year = Integer.parseInt(fields[3].trim().substring(0, 4));
            int month = Integer.parseInt(fields[3].trim().substring(5, 7));
            int day = Integer.parseInt(fields[3].trim().substring(8));

            Date dob = new Date(year - 1900, month - 1, day);
            row.put("dob", dob);

            double gpa = Double.parseDouble(fields[4].trim());

            row.put("gpa", gpa);

            dbApp.insertIntoTable("students", row);
            row.clear();
            if (limit != -1) {
                c--;
            }
        }
        studentsTable.close();
    }
    private static void insertTranscriptsRecords(DBApp dbApp, int limit) throws Exception {
        BufferedReader transcriptsTable = new BufferedReader(new FileReader("C:\\Users\\Lobna\\IdeaProjects\\DBII-Project\\transcripts_table.csv"));
        String record;
        Hashtable<String, Object> row = new Hashtable<>();
        int c = limit;
        if (limit == -1) {
            c = 1;
        }
        while ((record = transcriptsTable.readLine()) != null && c > 0) {
            String[] fields = record.split(",");

            row.put("gpa", Double.parseDouble(fields[0].trim()));
            row.put("student_id", fields[1].trim());
            row.put("course_name", fields[2].trim());

            String date = fields[3].trim();
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7));
            int day = Integer.parseInt(date.substring(8));

            Date dateUsed = new Date(year - 1900, month - 1, day);
            row.put("date_passed", dateUsed);

            dbApp.insertIntoTable("transcripts", row);
            row.clear();

            if (limit != -1) {
                c--;
            }
        }

        transcriptsTable.close();
    }
    private static void insertPCsRecords(DBApp dbApp, int limit) throws Exception {
        BufferedReader pcsTable = new BufferedReader(new FileReader("C:\\Users\\Lobna\\IdeaProjects\\DBII-Project\\pcs_table.csv"));
        String record;
        Hashtable<String, Object> row = new Hashtable<>();
        int c = limit;
        if (limit == -1) {
            c = 1;
        }
        while ((record = pcsTable.readLine()) != null && c > 0) {
            String[] fields = record.split(",");

            row.put("pc_id", Integer.parseInt(fields[0].trim()));
            row.put("student_id", fields[1].trim());

            dbApp.insertIntoTable("pcs", row);
            row.clear();

            if (limit != -1) {
                c--;
            }
        }

        pcsTable.close();
    }
    private static void createTranscriptsTable(DBApp dbApp) throws Exception {
        // Double CK
        String tableName = "transcripts";

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("gpa", "java.lang.Double");
        htblColNameType.put("student_id", "java.lang.String");
        htblColNameType.put("course_name", "java.lang.String");
        htblColNameType.put("date_passed", "java.util.Date");

        Hashtable<String, String> minValues = new Hashtable<>();
        minValues.put("gpa", "0.7");
        minValues.put("student_id", "43-0000");
        minValues.put("course_name", "AAAAAA");
        minValues.put("date_passed", "1990-01-01");

        Hashtable<String, String> maxValues = new Hashtable<>();
        maxValues.put("gpa", "5.0");
        maxValues.put("student_id", "99-9999");
        maxValues.put("course_name", "zzzzzz");
        maxValues.put("date_passed", "2020-12-31");

        dbApp.createTable(tableName, "gpa", htblColNameType, minValues, maxValues);
    }

    private static void createStudentTable(DBApp dbApp) throws Exception {
        // String CK
        String tableName = "students";

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("id", "java.lang.String");
        htblColNameType.put("first_name", "java.lang.String");
        htblColNameType.put("last_name", "java.lang.String");
        htblColNameType.put("dob", "java.util.Date");
        htblColNameType.put("gpa", "java.lang.Double");

        Hashtable<String, String> minValues = new Hashtable<>();
        minValues.put("id", "43-0000");
        minValues.put("first_name", "AAAAAA");
        minValues.put("last_name", "AAAAAA");
        minValues.put("dob", "1990-01-01");
        minValues.put("gpa", "0.7");

        Hashtable<String, String> maxValues = new Hashtable<>();
        maxValues.put("id", "99-9999");
        maxValues.put("first_name", "zzzzzz");
        maxValues.put("last_name", "zzzzzz");
        maxValues.put("dob", "2000-12-31");
        maxValues.put("gpa", "5.0");

        dbApp.createTable(tableName, "id", htblColNameType, minValues, maxValues);
    }
    private static void createPCsTable(DBApp dbApp) throws Exception {
        // Integer CK
        String tableName = "pcs";

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("pc_id", "java.lang.Integer");
        htblColNameType.put("student_id", "java.lang.String");


        Hashtable<String, String> minValues = new Hashtable<>();
        minValues.put("pc_id", "0");
        minValues.put("student_id", "43-0000");

        Hashtable<String, String> maxValues = new Hashtable<>();
        maxValues.put("pc_id", "20000");
        maxValues.put("student_id", "99-9999");

        dbApp.createTable(tableName, "pc_id", htblColNameType, minValues, maxValues);
    }
    private static void createCoursesTable(DBApp dbApp) throws Exception {
        // Date CK
        String tableName = "courses";

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("date_added", "java.util.Date");
        htblColNameType.put("course_id", "java.lang.String");
        htblColNameType.put("course_name", "java.lang.String");
        htblColNameType.put("hours", "java.lang.Integer");


        Hashtable<String, String> minValues = new Hashtable<>();
        minValues.put("date_added", "1901-01-01");
        minValues.put("course_id", "0000");
        minValues.put("course_name", "AAAAAA");
        minValues.put("hours", "1");

        Hashtable<String, String> maxValues = new Hashtable<>();
        maxValues.put("date_added", "2020-12-31");
        maxValues.put("course_id", "9999");
        maxValues.put("course_name", "zzzzzz");
        maxValues.put("hours", "24");

        dbApp.createTable(tableName, "date_added", htblColNameType, minValues, maxValues);

    }

    public static void main(String[] args) throws Exception {
        DBApp db = new DBApp();
        /*db.init();
        createCoursesTable(db);
        createPCsTable(db);
        createTranscriptsTable(db);
        createStudentTable(db);
        insertPCsRecords(db,500);
        insertTranscriptsRecords(db,500);
        insertStudentRecords(db,500);*/
       /* insertCoursesRecords(db,500);*/
      /*  Hashtable<String,Object> h = new Hashtable<>();
        h.put("id" , 13);
        h.put("DOB" , new Date(12/5/2012));
        h.put("last_name" , "ahmed");
        h.put("first_name" , "khaled");
        h.put("gpa" , 2.0);
        db.insertIntoTable("students", h);*/
        /*String [] arr = {"id" , "gpa" , "first_name"};
        db.createIndex("students", arr);*/
        /*Hashtable<String, Object> conditions = new Hashtable<>();
        conditions.put("gpa" , 2.0);
        db.updateTable("students" , "87-0123", conditions);*/
        SQLTerm[] arrSQLTerms= new SQLTerm[2];
        arrSQLTerms[0] = new SQLTerm();
        arrSQLTerms[1] = new SQLTerm();
        arrSQLTerms[1]._strTableName = "students";
        arrSQLTerms[1]._strColumnName= "id";
        arrSQLTerms[1]._strOperator = "=";
        arrSQLTerms[1]._objValue = "99-2620";
        arrSQLTerms[0]._strTableName = "students";
        arrSQLTerms[0]._strColumnName= "gpa";
        arrSQLTerms[0]._strOperator = "=";
        arrSQLTerms[0]._objValue = Double.valueOf(4.28) ;
        String[]strarrOperators = new String[1];
        strarrOperators[0] = "AND";
        // select * from Student where name = “John Noor” or gpa = 1.5;
        try {
            Iterator resultSet = db.selectFromTable(arrSQLTerms , strarrOperators);
            System.out.println("-------------------------------------");
            while (resultSet.hasNext()){
                System.out.println(((Hashtable)(resultSet.next())).toString());
            }

        } catch (DBAppException e) {
            throw new RuntimeException(e);
        }


    }

   /* public static void main(String[] args) {

        String strTableName = "person";
        DBApp dbApp = new DBApp();
        dbApp.init();
        Hashtable htblColNameType = new Hashtable();
        Hashtable htblcolNameMin  = new Hashtable ();
        htblcolNameMin.put("id",0 );
        htblcolNameMin.put("name","a" );
        htblcolNameMin.put("gpa", new Double( 0.7));
        Hashtable htblcolNameMax = new Hashtable ();
        htblcolNameMax.put("id",10000);
        htblcolNameMax.put("name","zzzzzzzzzzzzzzzzz");
        htblcolNameMax.put("gpa",new Double(  4.0));
        htblColNameType.put("id", "java.lang.Integer");
        htblColNameType.put("name", "java.lang.String");
        htblColNameType.put("gpa", "java.lang.Double");
        try {
            dbApp.createTable(strTableName, "id", htblColNameType,htblcolNameMin, htblcolNameMax);
        } catch (DBAppException e) {
            System.out.println(e.getMessage());
        }

        Hashtable htblColNameValue = new Hashtable();
        htblColNameValue.put("id", 2);
        htblColNameValue.put("name", new String("ahmed"));
        htblColNameValue.put("gpa", new Double( 0.95));
        try {
            dbApp.insertIntoTable(strTableName, htblColNameValue);
        } catch (DBAppException e) {
            System.out.println(e.getMessage());
        }
        dbApp.printTable(strTableName);
        htblColNameValue.clear();
        htblColNameValue.put("id", 4);
        htblColNameValue.put("name", new String("ahmed"));
        htblColNameValue.put("gpa", new Double( 0.95));
        try {
            dbApp.insertIntoTable(strTableName, htblColNameValue);
        } catch (DBAppException e) {
            System.out.println(e.getMessage());
        }
        htblColNameValue.clear();
        htblColNameValue.put("id", 7);
        htblColNameValue.put("name", "dalia");
        htblColNameValue.put("gpa", new Double( 3.5));
        try {
            dbApp.insertIntoTable(strTableName, htblColNameValue);
        } catch (DBAppException e) {
            System.out.println(e.getMessage());
        }
        htblColNameValue.clear();
        htblColNameValue.put("id", 10);
        htblColNameValue.put("name", new String("john"));
        htblColNameValue.put("gpa",new Double( 1.5));
        try {
            dbApp.insertIntoTable(strTableName, htblColNameValue);
        } catch (DBAppException e) {
            System.out.println(e.getMessage());
        }
        htblColNameValue.clear();
        htblColNameValue.put("id", 9);
        htblColNameValue.put("name", new String("khaled"));
        htblColNameValue.put("gpa", new Double(0.88));
        try {
            dbApp.insertIntoTable(strTableName, htblColNameValue);
        } catch (DBAppException e) {
            System.out.println(e.getMessage());
        }
        dbApp.printTable(strTableName);
        Hashtable deleteConditions = new Hashtable();
        System.out.println("Table after Deletion: ...................");
        deleteConditions.put("gpa",new Double(1.5));
        try {
            dbApp.deleteFromTable(strTableName,deleteConditions);
        } catch (DBAppException e) {
            System.out.println(e.getMessage());
        }
        dbApp.printTable(strTableName);
        System.out.println("Table after Updating: ...............................................................");
        Hashtable updateConditions = new Hashtable();
        updateConditions.put("gpa",new Double( 1.00));
        try {
            dbApp.updateTable(strTableName,"7", updateConditions);
        } catch (DBAppException e) {
            System.out.println(e.getMessage());
        }
        dbApp.printTable(strTableName);
        String[] columnName = {"id","name","gpa"};
        try {
            dbApp.createIndex(strTableName,columnName);
        } catch (DBAppException e) {
            throw new RuntimeException(e);
        }
       *//* htblColNameValue.clear();
        htblColNameValue.put("id", 14);
        htblColNameValue.put("name", new String("lobna"));
        htblColNameValue.put("gpa",new Double( 1.2));
        try {
            dbApp.insertIntoTable(strTableName, htblColNameValue);
        } catch (DBAppException e) {
            System.out.println(e.getMessage());
        }*//*
        //  System.out.println("After new Insertion -------------------------------------------------");
        //  printTree("Person" , "idnamegpaIndex");

        SQLTerm[] arrSQLTerms= new SQLTerm[2];
        arrSQLTerms[0] = new SQLTerm();
        arrSQLTerms[1] = new SQLTerm();
        arrSQLTerms[1]._strTableName = "Person";
        arrSQLTerms[1]._strColumnName= "name";
        arrSQLTerms[1]._strOperator = "=";
        arrSQLTerms[1]._objValue = "dalia";
        arrSQLTerms[0]._strTableName = "Person";
        arrSQLTerms[0]._strColumnName= "gpa";
        arrSQLTerms[0]._strOperator = "=";
        arrSQLTerms[0]._objValue = Double.valueOf(0.95) ;
        String[]strarrOperators = new String[1];
        strarrOperators[0] = "OR";
        // select * from Student where name = “John Noor” or gpa = 1.5;
        try {
            Iterator resultSet = dbApp.selectFromTable(arrSQLTerms , strarrOperators);
            while (resultSet.hasNext()){
                System.out.println(((Hashtable)(resultSet.next())).toString());
            }

        } catch (DBAppException e) {
            throw new RuntimeException(e);
        }*/
  //  }
}
