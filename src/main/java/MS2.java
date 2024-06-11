public class MS2 {
 //Select from table
 /*
 * Given data is already inserted
 * Without indexing
 * --primary Key in condition (Binary search for results)
 * --No primary key (linearly search)
 * --results are added to an iterator object
 * With index
 * --Conditions are all indexed (Octree)
 * --partially indexed (Octree + linear search f el results)   **binary search
 * --results are added to an iterator object
 * */

    //Insert into table
    /*
    * With Index
    * Call the tree.add_nodes
    * get the page from tree and insert into this page
    * after inserting check for violation
    * shift in page if needed
    * shift between pages if needed
    * ------if a column is indexed and inserted as null then throw an exception
    * Without index
    * already done
    * */

    //DeleteFromTable
    /*
    * Without index
    * already done
    * With index
    * (Kolo indexed)
    * --Octree.get
    * -----if false throw an exception "Data doesn't exist"
    * -----if true delete and adjust nodes and pages
    * (Partially indexed)
    * --Octree.get
    * --Linearly search in results
    * --if not found throw an exception "Data doesn't exist"
    * */

    //UpdateTable
    /*
    * Without index
    * already done
    * With index
    * --(Kolo indexed)
    * -----if false throw an exception "Data doesn't exist"
    * -----if true update and adjust nodes and pages
    * --(Partially indexed)
    * --Octree.get
    * --Linearly search in results
    * --if not found throw an exception "Data doesn't exist"
    * */
}



//-------------Select without index tracing
//loop over all pages linearly then select all where attribute == value and put it in a vector.iterator()
//then check on the or / and  / xor conditions
/*
 *ORING loop over pages   // make sure no duplicates are in result set
 *ANDING loop over results
 *XORING loop over pages and loop over results (Satisfy only one condition)
 * */

            /*page 1
             id Name  Age GPA
            * 1 Ahmed  21 0.7
            * 2 Khaled 20 0.8
            * 3 Hossam 20 0.9
            * */
/*page 2
 * 4 Noor 23 1
 * 5 Ahmed 22 1.1
 * 6 Zeina 25 1.2
 * */

// ((gpa = 2 and name = ahmed ) or age = 19   )  XOR gender = female
            /*if count = 0
            fill result1 vector
            don't enter the condition of anding/ oring / xoring
            if count > 0
            fill result2 vector
            check if anding / oring / xoring
            apply the operator
            put result in result1 vector
            * */
//result1 -> gpa=2
//result2 ->name = ahmed    -- and both put result in result 1
//result2 -> age 19      -- or both vector put result in result1
//result2 -> gender= female -- xor both vectors put result in result1
//result = result1
//       "gpa" ">"  "0.8"
/*
 * open metadatfile
 * check data type of column    --getDataType
 * then parse the object value    -- done
 * and change the operator from a string to real operator
 * get results and output them in an iterator object
 * */