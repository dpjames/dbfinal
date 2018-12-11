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
         if (roomCode.equals("q")) {
            return true;
         }
         query =
            "SELECT RoomName, re.* \n" +
            "FROM reservations re, rooms \n" +
            "WHERE Room = '" + roomCode +
               "' AND CheckIn <= '" + dates[0] + 
               "' AND CheckOut > '" + dates[0] + "' AND RoomId = Room;";

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
         if (resCode.equals("q")) {
            return true;
         }
         query =
            "SELECT RoomName, re.*\n" +
            "FROM reservations re, rooms \n" +
            "WHERE Code = " + resCode + " AND RoomId = Room;";

         results = Tables.doQuery(query, conn);
         try{
            Tables.prettyPrint(results);
         }catch(SQLException e){
            System.out.println(e);
         }

         return false;
      }
   }

   public static void Revenue(char resOpt) throws SQLException{
      int resOption;
      if (resOpt == 'c') {
         resOption = 3;
      }
      else if (resOpt == 'd') {
         resOption = 4;
      }
      else if (resOpt == 'r') {
         resOption = 5;
      }
      else if (resOpt == 'q') {
         return;
      }
      else {
         System.out.println("Invalid data option.");
         Revenue(InnReservations.revenueData());
         return;
      }

      String query = 
         "SELECT RoomName, MONTHNAME(CheckOut) 'Month', COUNT(*) as resCount, " +
            "SUM(DATEDIFF(CheckOut, CheckIn)) as daysOcc, " +
            "SUM(DATEDIFF(CheckOut, CheckIn) * Rate) as revenue\n" +
         "FROM reservations, rooms ro\n" +
         "WHERE ro.RoomId = Room AND YEAR(CheckOut) = '2010'\n" +
         "GROUP BY RoomName, MONTHNAME(CheckOut), MONTH(CheckOut)\n" +
         "ORDER BY RoomName, MONTH(CheckOut);";

      ResultSet res = Tables.doQuery(query, conn);

      ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
      ArrayList<String> cnames = new ArrayList<String>();
      cnames.add("Room");
      ArrayList<Integer> totalRow = new ArrayList<Integer>();
      for (int i = 0; i < 13; i++) {
         totalRow.add(0);
      }
      ArrayList<String> totalRowStr = new ArrayList<String>();
      totalRowStr.add("Total: ");
      res.next();
      for (int j = 0; j < 10; j++){
         ArrayList<String> row = new ArrayList<String>();
         row.add(res.getString(1));
         int sum = 0;

         for (int i = 1; i <= 12; i++){
            if (cnames.size() < 13) {
               cnames.add(res.getString(2));
            }

            row.add(res.getString(resOption));
            sum += res.getInt(resOption);
            totalRow.set(i-1, totalRow.get(i-1) + res.getInt(resOption));
            res.next();
         }
         if (table.size() == 0) {
            cnames.add("Total");
            table.add(cnames);
         }

         row.add(Integer.toString(sum));
         totalRow.set(12, totalRow.get(12) + sum);
         table.add(row);
      }

      for (Integer i : totalRow) {
         totalRowStr.add(i.toString());
      }
      table.add(totalRowStr);

      for(int i = 0; i < 171; i++){
         System.out.print("-");
      }
      int maxlens[] = {25, 10, 10, 6, 6, 5, 5, 5, 8, 10, 10, 10, 10, 8};
      System.out.println("");
      for(ArrayList<String> row : table) {
         String fmtStr = "";
         for(int i = 0; i < 14; i++){
            fmtStr+=("| %-"+maxlens[i]+"s ");
         }
         Object varparams[] = row.toArray(new String[row.size()]);
         System.out.format(fmtStr, varparams);
         System.out.println("|");
         for(int i = 0; i < 171; i++){
            System.out.print("-");
         }
         System.out.println("");
      }
   }

   public static void Reservations(String roomPassed) {
      while (true) {

      String roomCode = roomPassed;
      String query;
      ResultSet results;
      String dates[] = {"2010-01-01", "2010-12-31"};

      if (roomPassed == null) {
         int numDates = 2;
         for (int i = 0; i < numDates; i++) {
            System.out.print("Enter date (Month day): ");
            dates[i] = InnReservations.getDate();
         }

         query =
            "SELECT Code, Room, CheckIn, CheckOut \n" +
            "FROM reservations \n" +
            "WHERE CheckIn >= '" + dates[0] + 
               "' AND CheckIn <= '" + dates[1] + "'\n" +
            "ORDER BY CheckIn;";

         results = Tables.doQuery(query, conn);
         try{
            Tables.prettyPrint(results);
         }catch(SQLException e){
            System.out.println(e);
         }

         roomCode = InnReservations.getRoomCodeOrQ();
         if (roomCode.equals("q")) {
            return;
         }
      }

      query = "SELECT Code, Room, CheckIn, CheckOut \n" +
         "FROM reservations \n" +
         "WHERE Room = '" + roomCode + 
            "' AND CheckIn >= '" + dates[0] + 
            "' AND CheckIn <= '" + dates[1] + "'\n" +
         "ORDER BY CheckIn;";

      try {
         Tables.prettyPrint(Tables.doQuery(query, conn));
      }catch(SQLException e){
         System.out.println(e);
      }

      String resCode = InnReservations.getReservCodeOrQ();
      if (resCode.equals("q")) {
         return;
      }
      query =
         "SELECT RoomName, re.*\n" +
         "FROM reservations re, rooms \n" +
         "WHERE Code = " + resCode + " AND RoomId = Room;";

      results = Tables.doQuery(query, conn);
      try{
         Tables.prettyPrint(results);
      }catch(SQLException e){
         System.out.println(e);
      }

      if (InnReservations.askIfGoBack() == 'b') {
         return;
      }

      }

   }

   public static void Rooms() {
      String query =
         "SELECT RoomId, RoomName \n" +
         "FROM rooms \n;";

      ResultSet results = Tables.doQuery(query, conn);
      try{
         Tables.prettyPrint(results);
      }catch(SQLException e){
         System.out.println(e);
      }

      String option_code = InnReservations.viewRooms();
      if (option_code.charAt(0) == 'q') {
         return;
      }

      Scanner oc_scanner = new Scanner(option_code);

      String option = oc_scanner.next();

      String code = oc_scanner.next();

      oc_scanner.close();

      if (option.equals("v")) {
         query = 
         "SELECT SUM(DATEDIFF(re.CheckOut, re.CheckIn)) 'Nights Occupied', " +
           "(SUM(DATEDIFF(re.CheckOut, re.CheckIn))/365) * 100 '% Time Occupied', " +
           "SUM(DATEDIFF(re.CheckOut, re.CheckIn) * re.Rate) '2010 Revenue', " +
           "(SUM(DATEDIFF(re.CheckOut, re.CheckIn) * re.Rate)/" +
             "(" +
               "SELECT SUM(DATEDIFF(re2.CheckOut, re2.CheckIn) * re2.Rate)\n" +
               "FROM rooms ro2, reservations re2 \n" +
               "WHERE ro2.RoomId = re2.Room AND YEAR(re2.CheckOut) = '2010' \n" +
             ")" +
           ") * 100 '% 2010 Revenue'\n" +
         "FROM rooms ro, reservations re \n" +
         "WHERE ro.RoomId = " + code + " AND ro.RoomId = re.Room " +
           "AND YEAR(re.CheckOut) = '2010';";
         results = Tables.doQuery(query, conn);
         try{
            Tables.prettyPrint(results);
         }catch(SQLException e){
            System.out.println(e);
         }
      }
      else if (option.equals("r")) {
         Reservations(code.replace("'", ""));
      }
      else {
         System.out.println("Option: '" + option + "' is invalid.");
      }
   }

}