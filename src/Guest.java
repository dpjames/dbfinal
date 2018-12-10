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
   private static final int[][] specialDates = {{1,1},{4,4},{9,6},{10,30}};
   public static String rateSql(long dayms, String sqlday){
      double mult = 1;
      String[] dayvals = sqlday.split("-");  
      int todayYear =  Integer.parseInt(dayvals[0]);
      int todayMonth = Integer.parseInt(dayvals[1]);
      int todayDay =   Integer.parseInt(dayvals[2]);

      //weeknight is 1
      //weekend is 1.1
      //jan 1, july 4, sep 6, oct 30 is 1.25
      for(int i = 0; i < specialDates.length; i++){
         int month = specialDates[i][0];
         int day = specialDates[i][1];
         if(todayMonth == month && todayDay == day){
            mult = 1.1;
         }
      }
      return 
         "select BasePrice * "+mult+" " +
         "from rooms rmprice " +
         "where rmprice.RoomId = rm.RoomId";
   }
   public static void checkAvailability(String rid){
      System.out.println("checkin (month day): ");
      String checkin = Tables.escape(InnReservations.getDate());
      System.out.println("checkout (month day): ");
      String checkout = Tables.escape(InnReservations.getDate());
      long ndays = Tables.dateDiff(checkout,checkin);
      System.out.println("this many days: " + ndays);
      long checkinms = Tables.sqlDateToMs(checkin);
      for(int i = 0; i < ndays; i++){
         long todayms = checkinms + (i * Tables.MS_PER_DAY);
         String today = Tables.msToSqlDate(todayms);
         String ridWhere = "";
         if(rid != null){
            ridWhere+=" rm.RoomId = '" + rid + "' and ";
         }
         String query = 
            "select distinct '"+today+"' as date, rm.RoomId, " +
            "if(false = all ( "+
            "   select if(rs1.CheckIn <= '"+today+"' and rs1.CheckOut > '"+today+"',true,false) from rooms rm1, reservations rs1 "+
            "   where rs1.Room = rm1.RoomId and rm1.RoomId = rm.RoomId "+
            ") "+
            ",("+rateSql(todayms, today)+"),'occupied') as 'rate'"+
            "from rooms rm, reservations rs "+
            "where "+ridWhere+" rm.RoomId = rs.Room ";
         try {
            Tables.prettyPrint(Tables.doQuery(query, conn));
         } catch(SQLException e){
            //System.out.println(e);
         }
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
