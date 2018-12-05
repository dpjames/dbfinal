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
            } else if(cur.next() && cur.getString(1) == "0"){
               fill.add(i);   
            }
         } 
         for(int i : create){
              
         }
         for(int i : fill){
             
         }
      } catch (SQLException e){
         System.out.println(e);
      }
   }
}
