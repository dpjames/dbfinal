import java.sql.*;
import java.util.*;

public class Owner {
   private static Connection conn;
   public static void setConn(Connection c){
      conn = c;
   }

   public static Boolean occupancy_overview(){
      int numDates = InnReservations.getNumDates();
      String dates[] = new String[2];
      for (int i = 0; i < numDates; i++) {
         System.out.print("Enter date (Month day): ");
         dates[i] = InnReservations.getDate();
      }

      if (numDates == 1) {
         System.out.println(dates[0]);
         String query = 
            "SELECT DISTINCT(ro.RoomName), ro.RoomId, " +
               "IF(ro.RoomId IN " +
                  "(SELECT r.Room \n" +
                  "FROM reservations r \n" +
                  "WHERE r.CheckIn <= '" + dates[0] + "' AND " +
                     "r.CheckOut > '" + dates[0] + "' \n" +
                  "GROUP BY r.Room), 'Occupied', 'Empty') 'Status' \n" +
            "FROM reservations re, rooms ro \n" +
            "WHERE re.Room = ro.RoomId \n" +
            "ORDER BY ro.RoomId;";
         ResultSet results = Tables.doQuery(query, conn);
         try{
            Tables.prettyPrint(results);
         }catch(SQLException e){
            System.out.println(e);
         }
         String roomCode = InnReservations.getRoomCodeOrQ();
         System.out.println(roomCode);
         if (roomCode.equals("q")) {
            return true;
         }
         query =
            "SELECT * \n" +
            "FROM reservations \n" +
            "WHERE Room = '" + roomCode + 
               "' AND CheckIn <= '" + dates[0] + 
               "' AND CheckOut > '" + dates[0] + "';";

         results = Tables.doQuery(query, conn);
         try{
            Tables.prettyPrint(results);
         }catch(SQLException e){
            System.out.println(e);
         }

         char back = InnReservations.askIfGoBack();
         if (back == 'b') {
            return true;
         }
         return false;
      }
      else {

         String query =
            "SELECT ro0.RoomName, ro0.RoomId," +
              "IF (0 = SUM(ddiff.ddd), 'Empty', " +
                "IF (DATEDIFF(ADDDATE('" + dates[1] + "', INTERVAL 1 DAY), '" +
                     dates[0] + "') = SUM(ddiff.ddd), " +
                  "'Full', 'Partial')) 'Status' \n" +
            "FROM rooms ro0, \n" +
               "(SELECT ro.RoomId, IF ('" + dates[0] + 
               "' >= r.CheckOut OR r.CheckIn > '" + dates[1] +
               "', 0, DATEDIFF(IF(r.CheckOut > ADDDATE('" +
                     dates[1] + "', INTERVAL 1 DAY), " + 
                                       "ADDDATE('" + dates[1] +
                                       "', INTERVAL 1 DAY), r.CheckOut), \n" +
                                       "IF(r.CheckIn < '" + dates[0] + "', " +
                                 "'" + dates[0] + "', r.CheckIn))) as ddd \n" +
                "FROM reservations r, rooms ro \n" +
                "WHERE r.Room = ro.RoomId \n" +
                ") as ddiff \n" +
            "WHERE ro0.RoomId = ddiff.RoomId \n" +
            "GROUP BY ro0.RoomName, ro0.RoomId;";

         ResultSet results = Tables.doQuery(query, conn);
         try{
            Tables.prettyPrint(results);
         }catch(SQLException e){
            System.out.println(e);
         }

         String roomCode = InnReservations.getRoomCodeOrQ();
         System.out.println(roomCode);
         if (roomCode.equals("q")) {
            return true;
         }
         query =
            "SELECT Code, CheckIn, CheckOut \n" +
            "FROM reservations \n" +
            "WHERE Room = '" + roomCode + 
               "' AND (CheckIn <= '" + dates[0] + 
               "' AND CheckOut > '" + dates[1] +
               "' OR  CheckIn > '" + dates[0] +
               "' AND CheckIn <= '" + dates[1] +
               "' OR  CheckOut > '" + dates[0] +
               "' AND CheckOut <= '" + dates[1] +
                "');";

         results = Tables.doQuery(query, conn);
         try{
            Tables.prettyPrint(results);
         }catch(SQLException e){
            System.out.println(e);
         }

         String resCode = InnReservations.getReservCodeOrQ();
         System.out.println(resCode);
         if (resCode.equals("q")) {
            return true;
         }
         query =
            "SELECT *\n" +
            "FROM reservations \n" +
            "WHERE Code = " + resCode + ";";

         results = Tables.doQuery(query, conn);
         try{
            Tables.prettyPrint(results);
         }catch(SQLException e){
            System.out.println(e);
         }

         return false;
      }
   }

   public static void Revenue() {

   }
}