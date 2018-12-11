import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Calendar;

public class Guest {
   private static Connection conn;
   public static void setConn(Connection c){
      conn = c;
   }
   public static void showDetails(String rid){
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
      showDetails(rid);
      

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
         ") group by rms.Room having"+
         " rms.Room = '"+rid.toUpperCase()+"';"; 
      try {
         ResultSet r = Tables.doQuery(availableAll, conn);
         if(r.next()){
            char reserve = InnReservations.reserveOrGoBack();
            if(reserve == 'r'){
               placeReservation(rid, checkin, checkout);
            }
         }
      } catch(SQLException e) {
            //System.out.println(e);
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
      String rid = Tables.escape(InnReservations.getRoomCodeOrQ());
      if(rid.charAt(0) == 'q'){
         return;
      }
      showDetails(rid);
      char reserve = InnReservations.reserveOrGoBack();
      if(reserve == 'r'){
         placeReservation(rid, checkin, checkout);
      }
   }
   public static final String[][] discounts = {{"AARP",".85"},{"AAA",".9"}};
   public static double discountMod(String dc){
      for(int i = 0; i < discounts.length; i++){
         if(dc.equals(discounts[i][0])){
            return Double.parseDouble(discounts[i][1]);
         }
      }
      System.out.println("no discount applied");
      return 1;
   }
   public static double getBaseCost(String rid){
      String query = "select BasePrice from rooms where RoomId = '"+rid+"'";
      ResultSet res = null;
      try {
         res = Tables.doQuery(query, conn);
         if(res.next()){
            return Double.parseDouble(res.getString(1));
         }
      } catch (SQLException e){
         System.out.println(e);
      }
      return -1; //this should be imnpossible through the system.

   }
   public static int getUID(){
      String query = "select max(Code) from reservations";
      ResultSet res = null;
      try {
         res = Tables.doQuery(query, conn);
         if(res.next()){
            return Integer.parseInt(res.getString(1)) + 1;
         }
      } catch (SQLException e){
         System.out.println(e);
      }
      return -1; //this should be imnpossible through the system.
   }
   public static int getMaxPpl(String rid){
      String query = "select MaxOcc from rooms where RoomId = '"+rid+"'";
      ResultSet res = null;
      try {
         res = Tables.doQuery(query, conn);
         if(res.next()){
            return Integer.parseInt(res.getString(1));
         }
      } catch (SQLException e){
         System.out.println(e);
      }
      return -1; //this should be imnpossible through the system.
   }
   public static void placeReservation(String rid, String checkin, String checkout){
      System.out.println("Place a reservation for " + rid + 
                        "\nFrom "+checkin+" to "+checkout+".");
      String fname = InnReservations.getFirstName();
      String lname = InnReservations.getLastName();
      int maxppl = getMaxPpl(rid);
      int tppl = 0;
      int nadult = 0;
      int nchild = 0;
      do{
         nadult = InnReservations.getNumAdults();
         nchild = InnReservations.getNumChildren();
         tppl = nadult + nchild;
         if(tppl > maxppl){
            System.out.println("too many people. max occ is: "+maxppl);
         }
      } while(tppl > maxppl);
      String dc = InnReservations.getDiscount();
      double rateMod = findRateMult(checkin,checkout) * discountMod(dc);
      double base = getBaseCost(rid);
      char go = InnReservations.reserveOrGoBack();
      if(go == 'r'){
         insertReservation(Tables.escape(fname), 
                           Tables.escape(lname),
                           nadult,
                           nchild,
                           base * rateMod,
                           checkin,
                           checkout,
                           rid);
      }
   }

   public static void insertReservation(String fname, String lname, int nadult, int nchild, double price, String checkin, String checkout, String rid){
      int code = getUID();
      System.out.println(code);
      String query = 
         "insert into reservations "+
         "(Code, Room, CheckIn, CheckOut, Rate, LastName, FirstName, Adults, Kids) " +
         "VALUES " +
         "('"+code+"','"+rid+"','"+checkin+"','"+checkout+"',"+price+",'"+lname+"','"+
         fname+"',"+nadult+","+nchild+")";
      Tables.doUpdate(query, conn);
      System.out.println("reservation made. Details:\n");
      try {
         Tables.prettyPrint(Tables.doQuery("select * from reservations where Code = '"+code+"'", conn));
      } catch (SQLException e){
         System.out.println(e);
      }
   }
}












