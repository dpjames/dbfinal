import java.sql.*;
import java.util.*;

public class Owner {
   private static Connection conn;
   public static void setConn(Connection c){
      conn = c;
   }

   public static void occupancy_overview(){
      int numDates = InnReservations.getNumDates();
      List<String> dates = new ArrayList();
      for (int i = 0; i < numDates; i++) {
         System.out.print("Enter date (Month day): ");
         dates.add(InnReservations.getDate());
      }
   }
}