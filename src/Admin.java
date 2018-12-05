import java.sql.*;
import java.util.ArrayList;

public class Admin {
   private static Connection conn;
   public static void setConn(Connection c){
      conn = c;
   }
   public static void display(String[] toks){
      if(toks.length != 2){
         System.out.println("incorrect parameter length");
         System.out.println("please enter only the table name");
         return;
      }
      PreparedStatement stmt = null;
      ResultSet res = null;
      String query = "Select * from `"+toks[1]+"`;";
      try{
         Tables.prettyPrint(Tables.doQuery(query, conn));
      }catch(SQLException e){
         System.out.println(e);
      }
   }
   public static void clearDB(){
      for(int i = 0; i < Tables.tables.length; i++){
         Tables.doUpdate("delete from `"+Tables.tables[i]+"` where 1 = 1;", conn);
      }
   }
   public static void loadDB() {
      try {
         ArrayList<Integer> create = new ArrayList<Integer>();
         ArrayList<Integer> fill = new ArrayList<Integer>();
         for(int i = 0; i < Tables.tables.length; i++){
            String query = "select count(*) from `"+Tables.tables[i]+"`;";
            ResultSet cur = Tables.doQuery(query,conn);
            if(cur == null){
               create.add(i);   
            } else if(cur.next() && cur.getString(1).equals("0")){
               fill.add(i);   
            }
         } 
         for(int i : create){
            String tname = Tables.tables[i];
            String query = "create table `"+tname+"` as select * from "+Tables.DB_NAME+"."+tname+";";
            Tables.doUpdate(query, conn);
         }
         for(int i : fill){
            String tname = Tables.tables[i];
            String query = "insert into `"+tname+"` select * from "+Tables.DB_NAME+"."+tname+";";
            Tables.doUpdate(query, conn);
         }
      } catch (SQLException e){
         System.out.println(e);
      }
   }
   public static void removeDB(){
      for(int i = 0; i < Tables.tables.length; i++){
         String tname = Tables.tables[i];
         String query = "drop table `"+tname+"`;";
         System.out.println(query);
         Tables.doUpdate(query, conn);
      }
   }
   public static String getStatus(){
      int errCount = 0;
      int zeroCount = 0;
      int otherCount = 0;
      for(int i = 0; i < Tables.tables.length; i++){
         String res = getCount(Tables.tables[i]);
         if(res.equals("Err")){
            errCount++;
         } else if (res.equals("0")){
            zeroCount++;
         } else {
            otherCount++;
         }
      }
      if(errCount > 0){
         return "no database";
      } else if(zeroCount == Tables.tables.length) {
         return "empty";
      } else {
         return "full";
      }
   }
   public static String getCount(String tname){
      try {
         String query = "select count(*) from `"+tname+"`;";
         ResultSet count = Tables.doQuery(query, conn);
         if(count == null){
            return "Err";
         }
         count.next();
         return count.getString(1);
      } catch(SQLException e){
         System.out.println(e);
      }
      return "Err";
   }
}
