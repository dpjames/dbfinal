import java.sql.*;
import java.util.ArrayList;
public class Tables {
   public static final String tables[] = {"reservations", "rooms"};
   public static final String DB_NAME = "INN";
   public static void prettyPrint(ResultSet res) throws SQLException{
      if(res == null){
         return;
      }
      ResultSetMetaData md = res.getMetaData();
      int ncols = md.getColumnCount(); 
      ArrayList<String> cnames = new ArrayList<String>();
      for(int i = 1; i <= ncols; i++){
         cnames.add(md.getColumnName(i));
      }
      ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
      table.add(cnames);
      int maxstrlen[] = new int[ncols];
      for(int i = 0; i < ncols; i++){
         maxstrlen[i] = cnames.get(i).length();
      }
      while(res.next()){
         ArrayList<String> row = new ArrayList<String>();
         for(int i = 1; i <= ncols; i++){
            String s = res.getString(i);
            if(s.length() > maxstrlen[i-1]){
               maxstrlen[i-1] = s.length();
            }
            row.add(s);
         }
         table.add(row);
      }
      int linesize = 1;
      for(int i = 0; i < ncols; i++){
         linesize+=(maxstrlen[i]+3);
      }
      for(int i = 0; i < linesize; i++){
         System.out.print("-");
      }
      System.out.println("");
      for(ArrayList<String> row : table) {
         String fmtStr = "";
         for(int i = 0; i < ncols; i++){
            fmtStr+=("| %-"+maxstrlen[i]+"s ");
         }
         Object varparams[] = row.toArray(new String[row.size()]);
         System.out.format(fmtStr, varparams);
         System.out.println("|");
         for(int i = 0; i < linesize; i++){
            System.out.print("-");
         }
         System.out.println("");
      }
   }
   public static ResultSet doQuery(String query, Connection conn){
      try{
         PreparedStatement stmt = conn.prepareStatement(query); 
         ResultSet res = stmt.executeQuery();
         return res;
      } catch (SQLException e){
         System.out.println("error with db");
         System.out.println(e);
      }
      return null;
   }
   public static int doUpdate(String query, Connection conn){
      try{
         Statement stmt = conn.createStatement(); 
         int res = stmt.executeUpdate(query);
         return res;
      } catch (SQLException e){
         System.out.println("error with db");
         System.out.println(e);
      }
      return -1;
   }

}
