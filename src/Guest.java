import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Guest {
   private static Connection conn;
   public static void setConn(Connection c){
      conn = c;
   }
   public static void roomsAndRates(){
      Scanner in = new Scanner(System.in);
      try {
         String query = "select RoomId from rooms;";
         Tables.prettyPrint(Tables.doQuery(query, conn)); 
      } catch(SQLException e){
         System.out.println(e);
      }
      String rid = Tables.escape(InnReservations.getRoomCodeOrQ());
      if(rid.charAt(0) == 'q'){
         return;
      }
      try {
         String query = "select * from rooms where RoomId = '"+rid+"';";
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

      char seeAvail = InnReservations.availabilityOrGoBack();
      if(seeAvail == 'a'){
         checkAvailability(rid);
      }
   }

// Checking Room Availability. When the user chooses the ‘‘Check Availability’’ option,
// the system will offer the following dialog to the user. The system will provide the means for the
// user to enter a pair of dates (check-in and check-out). Once the dates are entered, the system will
// show information about whether or not the room is available on each of the nights of the proposed
// stay5
// . If the room is available for a given night, the system will display the room rate (see R-3 for
// room rate computation). If the room is unavailable, the system will display ‘‘Occupied’’ for that
// night. If the room is available for the duration of the selected stay (i.e., available on each night), a
// ‘‘Place a Reservation’’ option will be provided for the user to complete the reservation.

   public static void checkAvailability(String rid){
      System.out.println("checkin (month day): ");
      String checkin = Tables.escape(InnReservations.getDate());
      System.out.println("checkout (month day): ");
      String checkout = Tables.escape(InnReservations.getDate());
      long ndays = Tables.dateDiff(checkout,checkin);
      System.out.println("this many days: " + ndays);
      long checkinms = Tables.sqlDateToMs(checkin);
      for(int i = 0; i < ndays; i++){
         System.out.println(Tables.msToSqlDate(checkinms + (i * Tables.MS_PER_DAY)));
         String query =  
            "select distinct rm.RoomId " +
            "from rooms rm, reservtions rs " +
            "where rs.Room = rm.RoomId and not (";
      }

      /*
      String query = 
         "select distinct rm.RoomId " + 
         "from rooms rm, reservations rs " +
         "where rs.Room = rm.RoomId and not (rs.CheckIn > '"+checkout+"' or rs.CheckOut < '"+checkin+"')";   
      if(rid != null){
         query+=" and rm.RoomId = '" + rid + "';";
      }
      try {
         Tables.prettyPrint(Tables.doQuery(query, conn));
      } catch(SQLException e){
         System.out.println(e);
      }
      */
   }
}
