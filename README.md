# Database-Engine

In this project, you are going to build a small database engine with support for Octrees Indices.
The required functionalities are 1) creating tables, 2) inserting tuples, 3) deleting tuples, 4)
searching in tables linearly, 5) creating an Octree upon demand, and 6) using the created
octree(s) where appropriate.

### Tables
1) Each table/relation will be stored as pages on disk (each page is a separate file).
2) Supported type for a table’s column is one of: java.lang.Integer , java.lang.String ,
java.lang.Double , java.util.Date (Note: date acceptable format is "YYYY-MM-DD")
### Pages
3) A page has a predetermined fixed maximum number of rows (N). For example, if a
table has 40000 tuples, and if N=200, the table will be stored in 200 files.
4) You are required to use Java’s binary object file (.class) for emulating a page (to avoid
having you work with file system pages, which is not the scope of this course). A single
page must be stored as a serialized Vector (java.util.Vector) , because Vectors are thread
safe). Note that you can save/load any Java object to/from disk by implementing the
java.io.Serializable interface. You don’t actually need to add any code to your class to
save it the hard disk. For more info, check this:
https://www.tutorialspoint.com/java/java_serialization.htm
5) A single tuple should be stored in a separate object inside the binary file.
6) You need to postpone the loading of a page until the tuples in that page are actually
needed. Note that the purpose of using pages is to avoid loading the entire table’s content
into memory. Hence, it defeats the purpose to load all pages upon program startup.
7) If all the rows in a page are deleted, then you are required to delete that page. Do not
keep around completely empty pages. In the case of insert, if you are trying to insert in a
full page, shift one row down to the following page. Do not create a new page unless you
are in the last page of the table and that last one was full.
8) You might find it useful to create a Table java class to store relevant information about
the pages and serialize it just like you serialize a page. Note that to prevent serializing an
attribute, you will need to use the Java transient keyword in your attribute declaration.
Read more here: https://www.tutorialspoint.com/difference-between-volatile-and-transient-injava
Meta-Data File
9) Each user table has meta data associated with it; number of columns, data type of
columns, which columns have indices built on them.
10) You will need to store the meta-data in a text file. This should have the following
##### layout:
TableName,ColumnName, ColumnType, ClusteringKey, IndexName, IndexType, min, max
- ClusteringKey is set true if the column is the primary key. For simplicity, you will
always sort the rows in the table according to the primary key. That’s why, it is also
called the clusteringkey. Only 1 clustering key per table.
- min and max refer to the minimum and maximum values possible for that column.
For example, if a user creates a table/relation CityShop, specifying several attributes
with their types, etc…
12) You must use the metadata.csv file to learn about the types of the data being passed
and verify it is of the correct type.
13) You can (but not required to) use Java reflection API to load the data type and also
value of a column, for example:
strColType = "java.lang.Integer";
strColValue = "100";
Class class = Class.forName( strColType );
Constructor constructor = class.getConstructor( ….);
… = constructor.newInstance( );
For more info on reflection, check this article:
http://download.oracle.com/javase/tutorial/reflect/
### Indices
14) You are required to use an Octree to support creating indices. An Octree is on exactly
3 dimensions. You are not required to support other type of indices.
15) It is up to you how to implement the octree. Most straight forward implementation
would be that of a generic tree where an insert into a full node creates the 8 octant
children of that node and distribute the parent node content appropriately among them.
For simplicity, in your implementation, you can insert with each value in the octree a
reference to where the row that has that value is (i.e. reference to the table’s page/file on
disk). You can also use third party implementations of octree but you need to
acknowledge that and it is your responsibility to test and adapt such before using it in the
project.
16) Each column has an associated range (minimum, maximum). You should use this
range to create the divisions on the Index scale.
17) Once a table is created, you do not need to create any page until the insertion of the
first row/tuple.
18) You should update existing relevant indices when a tuple is inserted/deleted.
19) If an index is created after a table has been populated, you have no option but to scan
the whole table to get the data read and inserted into the index.
20) Upon application startup; to avoid having to scan all tables to build existing indices,
you should save the index itself to disk and load it when the application starts next time.
21) When a table is created, you do not need to create an index. An index will be created
later on when the user requests that through a method call to createIndex
22) Once an index exists, it should be used in executing queries where possible. Hence, if
an index does not exist on columns that are being queried, (e.g. select * from T
where x = 20 and y= 30 and z = 20), then the query will be answered using linear
scanning in T. However, if an index is created on the 3 columns queried, then the index
should be used.
23) Note that indices should be used in answering multi-dimension partial queries if an
index has been created on any of columns used in the query, e.g.
select * from Table1 where Table1.column1=value1 and
Table1.column2=value2 and Table1.column3=value3 andTable1.column4=value4;
and an index has been created on Table1.column1, Table1.column2, and Table1.column3
then it should be used in answering the query.
### Required Methods/Class
24) Your main class should be called DBApp.java and should have the following seven
methods with the signature as specified. The parameters names are written using
Hungarian notation -- which you are not required to use but it does enhances the
readability of your code, so it is a good idea to try it out! You can add any other helper
methods and classes. Note that these 8 methods are the only way to run your Database
engine. You are not required to develop code that process SQL statements directly
because that requires knowledge beyond this course (the compilers course in 9th and 10th
semester) and it is not a learning outcome from this course. Note that most parameters are
passed as Strings (because that’s how they will be entered by user) but you are required
to convert them to correct type internally.




public void init( ); // this does whatever initialization you would like
// or leave it empty if there is no code you want to
// execute at application startup
// following method creates one table only
// strClusteringKeyColumn is the name of the column that will be the primary
// key and the clustering column as well. The data type of that column will
// be passed in htblColNameType
// htblColNameValue will have the column name as key and the data
// type as value
// htblColNameMin and htblColNameMax for passing minimum and maximum values
// for data in the column. Key is the name of the column
public void createTable(String strTableName,
String strClusteringKeyColumn,
Hashtable<String,String> htblColNameType,
Hashtable<String,String> htblColNameMin,
Hashtable<String,String> htblColNameMax )
throws DBAppException
// following method creates an octree
// depending on the count of column names passed.
// If three column names are passed, create an octree.
// If only one or two column names is passed, throw an Exception.
public void createIndex(String strTableName,
String[] strarrColName) throws DBAppException
// following method inserts one row only.
// htblColNameValue must include a value for the primary key
public void insertIntoTable(String strTableName,
Hashtable<String,Object> htblColNameValue)
throws DBAppException
// following method updates one row only
// htblColNameValue holds the key and new value
// htblColNameValue will not include clustering key as column name
// strClusteringKeyValue is the value to look for to find the row to update.
public void updateTable(String strTableName,
String strClusteringKeyValue,
Hashtable<String,Object> htblColNameValue )
throws DBAppException
// following method could be used to delete one or more rows.
// htblColNameValue holds the key and value. This will be used in search
// to identify which rows/tuples to delete.
// htblColNameValue enteries are ANDED together
public void deleteFromTable(String strTableName,
Hashtable<String,Object> htblColNameValue)
throws DBAppException
public Iterator selectFromTable(SQLTerm[] arrSQLTerms,
String[] strarrOperators)
throws DBAppException





25) For the parameters, the name documents what is being passed – for example
htblColNameType is a hashtable with key as ColName and value is the Type.
26) Operator Inside SQLTerm can either be >, >=, <, <=, != or =
27) Operator between SQLTerm (as in strarrOperators above) are AND, OR, or XOR.
28) DBAppException is a generic exception to avoid breaking the test cases when they
run. You can customize the Exception by passing a different message upon creation. You
should throw the exception whenever you are passed data you that will violate the
integrity of your schema.
29) SQLTerm is a class with 4 attributes: String _strTableName, String _strColumnName,
String _strOperator and Object _objValue
30) Iterator is java.util.Iterator It is an interface that enables client code to iterate over the
results row by row. Whatever object you return holding the result set, it should
implement the Iterator interface.
31) You should check on the passed types and do not just accept any type – otherwise,
your code will crash with invalid input.
32) You are not supporting SQL Joins in this mini-project.

Directory Structure
33) In a resources directory inside your project source, include a configuration file
DBApp.config which holds a parameters as key=value pairs
MaximumRowsCountinTablePage = 200
MaximumEntriesinOctreeNode = 16
Where
MaximumRowsCountinTablePage as the name
indicates specifies the maximum number of
rows in a page.
and
MaximumEntriesinOctreeNode is the maximum
entries you can store in an octree node, else
you have to create the 8 octants and distribute
the maximum + 1 among them.
Note: you can add any other configuration parameter in
DBApp.config
34) DBApp.config file could be read using java.util.Properties class
35) If you want to add any external library, you should add it as a resource to your
project. For example, in maven add it to the pom.xml file as a dependency. Do not
include any external 3rd jar files in your submission.
Bonus – Worth 2% of course grade
36) Add support for processing SQL statements. For that, you will need a SQL parser.
You can use ANTRL, a parser generator. Here is a page which include a number of ready
to use SQL grammars: https://www.antlr3.org/grammar/list.html
37) If you do the bonus, only accept statements that you can run in your mini database
engine. For example: create constraint …. Should throw an exception.
38) To pass a SQL string, such as create table …, add a method with the following
syntax:
// below method returns Iterator with result set if passed
// strbufSQL is a select, otherwise returns null.
public Iterator parseSQL( StringBuffer strbufSQL ) throws
DBAppException
