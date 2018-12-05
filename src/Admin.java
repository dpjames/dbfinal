import java.sql.*;

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
   }
}
