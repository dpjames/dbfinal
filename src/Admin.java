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
      try{
         String query = "Select * from `"+toks[1]+"`;";
         Statement stmt = conn.prepareStatement(query); 
      }catch(Exception e){
         //just so it compiles right now
      }
   }
}
