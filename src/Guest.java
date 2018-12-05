import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Guest {
   private static Connection conn;
   public static void setConn(Connection c){
      conn = c;
   }
   public static void roomsAndRates(){
      //display list of rooms
      //when room is selected -> show details
      //
      Scanner in = new Scanner(System.in);
      try {
         String query = "select RoomId from rooms;";
         Tables.prettyPrint(Tables.doQuery(query, conn)); 
      } catch(SQLException e){
         System.out.println(e);
      }
      System.out.print("RoomId: ");
      String rid = in.nextLine();
      try {
         String query = "select * from rooms where RoomId = '"+Tables.escape(rid)+"';";
         ResultSet r = Tables.doQuery(query, conn);
         if(!r.next()){
            System.out.println("room not found");
            return;
         }
         r.previous();
         Tables.prettyPrint(r);
      } catch (SQLException e){
         System.out.println(e);
         return;
      }
      System.out.print("See Availability for room? (y/n): ");
      String seeAvail = in.nextLine();
      if(seeAvail.toLowerCase().charAt(0) == 'y'){
         checkAvailability();
      }
   }
   public static void checkAvailability(){
          
   }
}
