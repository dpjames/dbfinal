import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Calendar;

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

   private static final int[][] specialDates = {{1,1},{7,4},{9,6},{10,30}};
   public static double dayMult(String sqlday){
      double mult = 1;
      String[] dayvals = sqlday.split("-");  
      int todayYear =  Integer.parseInt(dayvals[0]);
      int todayMonth = Integer.parseInt(dayvals[1]);
      int todayDay =   Integer.parseInt(dayvals[2]);
      Calendar c = Calendar.getInstance();
      c.set(todayYear, todayMonth - 1, todayDay);
      int dow = c.get(Calendar.DAY_OF_WEEK);
      if(dow == Calendar.SUNDAY || dow == Calendar.SATURDAY){
         mult = 1.1;
      }
      for(int i = 0; i < specialDates.length; i++){
         int month = specialDates[i][0];
         int day = specialDates[i][1];
         if(todayMonth == month && todayDay == day){
            mult = 1.25;
         }
      }
      return mult;
   }
   public static String rateSql(String sqlday){
      return 
         "select BasePrice * "+dayMult(sqlday)+" " +
         "from rooms rmprice " +
         "where rmprice.RoomId = rm.RoomId";
   }
   public static double findRateMult(String in,String out){
      long days = Tables.dateDiff(out, in);
      double rate = dayMult(in);
      long inms = Tables.sqlDateToMs(in);
      for(int i = 0; i < days; i++){
         rate = Math.max(rate, dayMult(Tables.msToSqlDate(inms+Tables.MS_PER_DAY * i)));
      }
      return rate;
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
            ",("+rateSql(today)+"),'occupied') as 'rate'"+
            "from rooms rm, reservations rs "+
            "where "+ridWhere+" rm.RoomId = rs.Room order by rm.RoomId";
         try {
            ResultSet res = Tables.doQuery(query, conn);
            Tables.prettyPrint(res);
         } catch(SQLException e){
            //System.out.println(e);
         }
      }
      String availableAll = 
         "select distinct rms.Room  " + 
         "from reservations rms " + 
         "where  " + 
         "'"+checkout+"' <= all  " + 
         "( " + 
         "   select r.CheckIn from reservations r where r.Room = rms.Room " +
            "and r.CheckOut > '"+checkin+"'" + 
         ") " + 
         "or " + 
         "'"+checkin+"' >= all " + 
         "( " + 
         "   select r.CheckOut from reservations r where r.Room = rms.Room " + 
             "and r.CheckIn <= '"+checkout+"'" + 
         ");"; 
      System.out.println(availableAll);
      try {
         System.out.println("available for all: ");
         Tables.prettyPrint(Tables.doQuery(availableAll, conn));
      } catch(SQLException e) {
            //System.out.println(e);
      }
      char reserve = InnReservations.reserveOrGoBack();
      if(reserve == 'r'){
         makeReservation();
      }
   }
   public static void makeReservation(){
      System.out.println("checkin (month day): ");
      String checkin = Tables.escape(InnReservations.getDate());
      System.out.println("checkout (month day): ");
      String checkout = Tables.escape(InnReservations.getDate());
      double mult = findRateMult(checkin, checkout);
      String roomRateQuery = 
         "select BasePrice * "+mult+" from rooms rm1 where rm1.RoomId = rms.Room";
      String availableAll = 
         "select distinct rms.Room, ("+roomRateQuery+")"+
         " as 'Nightly Rate' " + 
         "from reservations rms " + 
         "where  " + 
         "'"+checkout+"' <= all  " + 
         "( " + 
         "   select r.CheckIn from reservations r where r.Room = rms.Room " +
            "and r.CheckOut > '"+checkin+"'" + 
         ") " + 
         "or " + 
         "'"+checkin+"' >= all " + 
         "( " + 
         "   select r.CheckOut from reservations r where r.Room = rms.Room " + 
             "and r.CheckIn <= '"+checkout+"'" + 
         ");"; 
      try {
         Tables.prettyPrint(Tables.doQuery(availableAll, conn));
      } catch (SQLException e) {
         //System.out.println(e);
      }
   }
}
