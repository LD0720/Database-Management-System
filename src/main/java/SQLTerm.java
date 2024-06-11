public class SQLTerm {
    String _strTableName ;
    String _strColumnName;
    String _strOperator;
    Object _objValue;

    public SQLTerm(String _strTableName, String _strColumnName, String _strOperator, Object _objValue) throws DBAppException {
        this._strTableName = _strTableName;
        this._strColumnName = _strColumnName;
        if(_strOperator!=">"&& _strOperator!=">="&& _strOperator!="<"
                &&_strOperator!="<="&&_strOperator!="!=" &&_strOperator!="="){
            throw new DBAppException("Operator inserted doesn't exist");
        }
        this._strOperator = _strOperator;
        this._objValue = _objValue;
    }
    public SQLTerm(){

    }

}
