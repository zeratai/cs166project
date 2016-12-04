package cs166.cafe;

/**
 * Created by justinmoy on 11/29/16.
 */

import android.widget.EditText;
import android.widget.LinearLayout;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

public class Cafe {
    //login info for later use
    private static String authorisedUser = null;

    // reference to physical database connection.
    private Connection _connection = null;

    private EditText mLogin;
    private EditText mPassword;

    // handling the keyboard inputs through a BufferedReader
    // This variable can be global for convenience.
    static BufferedReader in = new BufferedReader(
            new InputStreamReader(System.in));

    /**
     * Creates a new instance of Cafe
     *
     * @param hostname the MySQL or PostgreSQL server hostname
     * @param database the name of the database
     * @throws java.sql.SQLException when failed to make a connection.
     */
    public Cafe (String dbname, String dbport) throws SQLException {

        System.out.print("Connecting to database...");
        try{
            // constructs the connection URL
            String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
            System.out.println ("Connection URL: " + url + "\n");

            String external = "jdbc:postgresql://192.168.42.253:" + dbport + "/" + dbname;

            // obtain a physical connection
            this._connection = DriverManager.getConnection(external,"justinmoy", "");
            System.out.println("Done");
        }catch (Exception e){
            System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
            System.out.println("Make sure you started postgres on this machine");
            System.exit(-1);
        }//end catch
    }//end Cafe

    /**
     * Method to execute an update SQL statement.  Update SQL instructions
     * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
     *
     * @param sql the input SQL string
     * @throws java.sql.SQLException when update failed
     */
    public void executeUpdate (String sql) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the update instruction
        stmt.executeUpdate (sql);

        // close the instruction
        stmt.close ();
    }//end executeUpdate

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and outputs the results to
     * standard out.
     *
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQueryAndPrintResult (String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
        ResultSetMetaData rsmd = rs.getMetaData ();
        int numCol = rsmd.getColumnCount ();
        int rowCount = 0;

        // iterates through the result set and output them to standard out.
        boolean outputHeader = true;
        while (rs.next()){
            if(outputHeader){
                for(int i = 1; i <= numCol; i++){
                    System.out.print(rsmd.getColumnName(i) + "\t");
                }
                System.out.println();
                outputHeader = false;
            }
            for (int i=1; i<=numCol; ++i)
                System.out.print (rs.getString (i) + "\t");
            System.out.println ();
            ++rowCount;
        }//end while
        stmt.close ();
        return rowCount;
    }//end executeQuery

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and returns the results as
     * a list of records. Each record in turn is a list of attribute values
     *
     * @param query the input query string
     * @return the query result as a list of records
     * @throws java.sql.SQLException when failed to execute the query
     */
    public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
        ResultSetMetaData rsmd = rs.getMetaData ();
        int numCol = rsmd.getColumnCount ();
        int rowCount = 0;

        // iterates through the result set and saves the data returned by the query.
        boolean outputHeader = false;
        List<List<String>> result  = new ArrayList<List<String>>();
        while (rs.next()){
            List<String> record = new ArrayList<String>();
            for (int i=1; i<=numCol; ++i)
                record.add(rs.getString (i));
            result.add(record);
        }//end while
        stmt.close ();
        return result;
    }//end executeQueryAndReturnResult

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and returns the number of results
     *
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQuery (String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery (query);

        int rowCount = 0;

        // iterates through the result set and count nuber of results.
        if(rs.next()){
            rowCount++;
        }//end while
        stmt.close ();
        return rowCount;
    }

    /**
     * Method to fetch the last value from sequence. This
     * method issues the query to the DBMS and returns the current
     * value of sequence used for autogenerated keys
     *
     * @param sequence name of the DB sequence
     * @return current value of a sequence
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int getCurrSeqVal(String sequence) throws SQLException {
        Statement stmt = this._connection.createStatement ();

        ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
        if (rs.next())
            return rs.getInt(1);
        return -1;
    }

    /**
     * Method to close the physical connection if it is open.
     */
    public void cleanup(){
        try{
            if (this._connection != null){
                this._connection.close ();
            }//end if
        }catch (SQLException e){
            // ignored.
        }//end try
    }//end cleanup

    /**
     * The main execution method
     *
     * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
     */
    /*
    public static void main (String[] args) {
        if (args.length != 2) {
            System.err.println (
                    "Usage: " +
                            "java [-classpath <classpath>] " +
                            Cafe.class.getName () +
                            " <dbname> <port>");
            return;
        }//end if

        Greeting();
        Cafe esql = null;
        try{
            // use postgres JDBC driver.
            Class.forName ("org.postgresql.Driver").newInstance ();
            // instantiate the Cafe object and creates a physical
            // connection.
            String dbname = args[0];
            String dbport = args[1];
            esql = new Cafe (dbname, dbport);

            boolean keepon = true;
            while(keepon) {
                // These are sample SQL statements
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Create user");
                System.out.println("2. Log in");
                System.out.println("9. < EXIT");
                authorisedUser = null;
                switch (readChoice()){
                    case 1: CreateUser(esql); break;
                    case 2: authorisedUser = LogIn(esql, String Login, String pass); break;
                    case 9: keepon = false; break;
                    default : System.out.println("Unrecognized choice!"); break;
                }//end switch
                if (authorisedUser != null) {
                    boolean usermenu = true;
                    String user_type = find_type(esql);
                    switch (user_type){
                        case "Customer":
                            while(usermenu) {
                                System.out.println("MAIN MENU");
                                System.out.println("---------");
                                System.out.println("1. Browse Menu by ItemName");
                                System.out.println("2. Browse Menu by Type");
                                System.out.println("3. Add Order");
                                System.out.println("4. Update Order");
                                System.out.println("5. View Order History");
                                System.out.println("6. View Order Status");
                                System.out.println("7. Update User Info");
                                System.out.println(".........................");
                                System.out.println("9. Log out");
                                switch (readChoice()){
                                    case 1: BrowseMenuName(esql); break;
                                    case 2: BrowseMenuType(esql); break;
                                    case 3: AddOrder(esql); break;
                                    case 4: UpdateOrder(esql); break;
                                    case 5: ViewOrderHistory(esql); break;
                                    case 6: ViewOrderStatus(esql); break;
                                    case 7: UpdateUserInfo(esql); break;
                                    case 9: usermenu = false; break;
                                    default : System.out.println("Unrecognized choice!"); break;
                                }//end switch
                            } break;
                        case "Employee":
                            while(usermenu) {
                                System.out.println("MAIN MENU");
                                System.out.println("---------");
                                System.out.println("1. Browse Menu by ItemName");
                                System.out.println("2. Browse Menu by Type");
                                System.out.println("3. Add Order");
                                System.out.println("4. Update Order");
                                System.out.println("5. View Current Orders");
                                System.out.println("6. View Order Status");
                                System.out.println("7. Update User Info");
                                System.out.println(".........................");
                                System.out.println("9. Log out");
                                switch (readChoice()){
                                    case 1: BrowseMenuName(esql); break;
                                    case 2: BrowseMenuType(esql); break;
                                    case 3: AddOrder(esql); break;
                                    case 4: EmployeeUpdateOrder(esql); break;
                                    case 5: ViewCurrentOrder(esql); break;
                                    case 6: ViewOrderStatus(esql); break;
                                    case 7: UpdateUserInfo(esql); break;
                                    case 9: usermenu = false; break;
                                    default : System.out.println("Unrecognized choice!"); break;
                                }//end switch
                            } break;
                        case "Manager ":
                            while(usermenu) {
                                System.out.println("MAIN MENU");
                                System.out.println("---------");
                                System.out.println("1. Browse Menu by ItemName");
                                System.out.println("2. Browse Menu by Type");
                                System.out.println("3. Add Order");
                                System.out.println("4. Update Order");
                                System.out.println("5. View Current Orders");
                                System.out.println("6. View Order Status");
                                System.out.println("7. Update User Info");
                                System.out.println("8. Update Menu");
                                System.out.println(".........................");
                                System.out.println("9. Log out");
                                switch (readChoice()){
                                    case 1: BrowseMenuName(esql); break;
                                    case 2: BrowseMenuType(esql); break;
                                    case 3: AddOrder(esql); break;
                                    case 4: EmployeeUpdateOrder(esql); break;
                                    case 5: ViewCurrentOrder(esql); break;
                                    case 6: ViewOrderStatus(esql); break;
                                    case 7: ManagerUpdateUserInfo(esql); break;
                                    case 8: UpdateMenu(esql); break;
                                    case 9: usermenu = false; break;
                                    default : System.out.println("Unrecognized choice!"); break;
                                }//end switch
                            } break;
                    }//end switch
                }//end if
            }//end while
        }catch(Exception e) {
            System.err.println (e.getMessage ());
        }finally{
            // make sure to cleanup the created table and close the connection.
            try{
                if(esql != null) {
                    System.out.print("Disconnecting from database...");
                    esql.cleanup ();
                    System.out.println("Done\n\nBye !");
                }//end if
            }catch (Exception e) {
                // ignored.
            }//end try
        }//end try
    }//end main
    */
    public static void Greeting(){
        System.out.println(
                "\n\n*******************************************************\n" +
                        "              User Interface                         \n" +
                        "*******************************************************\n");
    }//end Greeting

    /*
     * Reads the users choice given from the keyboard
     * @int
     **/
    public static int readChoice() {
        int input;
        // returns only if a correct value is given.
        do {
            System.out.print("Please make your choice: ");
            try { // read the integer, parse it and break.
                input = Integer.parseInt(in.readLine());
                break;
            }catch (Exception e) {
                System.out.println("Your input is invalid!");
                continue;
            }//end try
        }while (true);
        return input;
    }//end readChoice

    /*
     * Creates a new user with privided login, passowrd and phoneNum
     **/
    public static void CreateUser(Cafe esql){
        try{
            System.out.print("\tEnter user login: ");
            String login = in.readLine();
            System.out.print("\tEnter user password: ");
            String password = in.readLine();
            System.out.print("\tEnter user phone: ");
            String phone = in.readLine();

            String type="Customer";
            String favItems="";

            String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

            esql.executeUpdate(query);
            System.out.println ("User successfully created!");
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }//end

    /*
     * Check log in credentials for an existing user
     * @return User login or null is the user does not exist
     **/
    public static String LogIn(Cafe esql, String login, String password){
        try{
            /*System.out.print("\tEnter user login: ");
            String login = in.readLine();
            System.out.print("\tEnter user password: ");
            String password = in.readLine();*/

            String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s'", login, password);
            int userNum = esql.executeQuery(query);
            if (userNum > 0)
                return login;
            return null;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    public static String find_type(Cafe esql){
        // Your code goes here.
        // ...
        // ...
        //System.out.print(authorisedUser);
        try{
            String query = String.format("SELECT Users.type FROM Users WHERE Users.login = '%s'", authorisedUser);

            List<List<String>> l = esql.executeQueryAndReturnResult(query);
            //System.out.print(l.get(0).get(0));
            return l.get(0).get(0);

        }catch(Exception e){
            System.err.println (e.getMessage());
        }

        return "Employee";

    }

    public static void BrowseMenuName(Cafe esql){
        // Your code goes here.
        // ...
        // ...

        try{
            System.out.print("What item are you looking for?");
            String input = in.readLine();
            String query = String.format("SELECT * FROM Menu WHERE Menu.itemName = '%s'", input);

            esql.executeQueryAndPrintResult(query);

        }catch(Exception e){
            System.err.println (e.getMessage());
        }

    }//end

    public static void BrowseMenuType(Cafe esql){
        // Your code goes here.
        // ...
        // ...
        try{
            System.out.print("What type are you looking for?");
            String input = in.readLine();
            String query = String.format("SELECT * FROM Menu WHERE Menu.type = '%s'", input);

            esql.executeQueryAndPrintResult(query);

        }catch(Exception e){
            System.err.println (e.getMessage());
        }

    }//end

    public static Integer AddOrder(Cafe esql){
        // Your code goes here.
        // ...
        // ...
        Integer orderid=0;
        try{
            System.out.print("What item are you adding for your order?");
            String input = in.readLine();
            String query = String.format("SELECT Menu.price FROM Menu WHERE Menu.itemName = '%s'", input);

            List<List<String>> l = esql.executeQueryAndReturnResult(query);

            String p = l.get(0).get(0).trim();

            //System.out.print("Converted String to Int");

            double price = Double.parseDouble(p);

            //System.out.print(price + '\n');
            boolean isPaid = false;
            Date date = new Date();
            long time = date.getTime();
            Timestamp timeStamp = new Timestamp(time);
            String insert = String.format("INSERT INTO Orders (orderid, login, paid, timeStampRecieved, total) VALUES (DEFAULT,'%s','%s','%s','%s')", authorisedUser.trim(), isPaid, timeStamp, price);

            esql.executeUpdate(insert);

            orderid = esql.getCurrSeqVal("Orders_orderid_seq");
            System.out.println(orderid);

            String status = "Pending";

            System.out.println("Comments for your order: ");

            String comments = in.readLine();

            String insertItemStatus = String.format("INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status, comments) VALUES ('%s', '%s', '%s', '%s', '%s')", orderid, input, timeStamp, status, comments);

            esql.executeUpdate(insertItemStatus);

            //System.out.println ("Updated Order!");

        }catch(Exception e){
            System.err.println (e.getMessage());
        }
        return orderid;
    }//end

    public static void UpdateOrder(Cafe esql){
        // Your code goes here.
        // ...
        // ...
        System.out.print("Please enter the orderid you wish to update: " + '\n');

        try {
            String orderid = in.readLine();
            double total = 0;
            boolean options = false;

            String checkOrder = String.format("SELECT Orders.login FROM Orders WHERE orderid = '%s'", orderid);

            String cOrder = (esql.executeQueryAndReturnResult(checkOrder)).get(0).get(0).trim();

            //System.out.println("cOrder result: " + cOrder + " . Authorized User: " + authorisedUser);

            if(cOrder.equals(authorisedUser.trim())) {
                options = true;
            }

            else {
                System.out.println("Invalid Order ID!" + '\n' + "Did you enter the correct orderid?");
            }

            while(options) {
                System.out.println("Update Order Options");
                System.out.println("---------");
                System.out.println("1. Add an Item");
                System.out.println("2. Delete an Item");
                System.out.println("3. Cancel Order");
                System.out.println(".........................");
                System.out.println("9. Finished");

                int choice = readChoice();

                if(choice == 1) {
                    System.out.println("What item are you adding for your order?");
                    String item = in.readLine();
                    String query = String.format("SELECT Menu.price FROM Menu WHERE Menu.itemName = '%s'", item);

                    List<List<String>> l = esql.executeQueryAndReturnResult(query);

                    String addItemPrice = l.get(0).get(0).trim();

                    //System.out.print("Converted String to Int");

                    double price = Double.parseDouble(addItemPrice);

                    String priceQuery = String.format("SELECT total FROM Orders WHERE orderid = '%s'", orderid);

                    List<List<String>> totalPriceResult = esql.executeQueryAndReturnResult(priceQuery);

                    total = price + Double.parseDouble(totalPriceResult.get(0).get(0).trim());

                    Date date = new Date();
                    long time = date.getTime();
                    Timestamp timeStamp = new Timestamp(time);
                    String insert = String.format("UPDATE Orders SET total = '%s' WHERE orderid = '%s'", total, orderid);

                    esql.executeUpdate(insert);

                    String status = "Pending";

                    System.out.println("Comments for your additional order: ");

                    String comments = in.readLine();

                    String insertItemStatus = String.format("INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status, comments) VALUES ('%s', '%s', '%s', '%s', '%s')", orderid, item, timeStamp, status, comments);

                    esql.executeUpdate(insertItemStatus);
                }
                else if(choice == 2) {
                    System.out.println("What item are you deleting for your order?");
                    String itemDelete = in.readLine();

                    String checkItem = String.format("SELECT itemName FROM ItemStatus WHERE orderid = '%s' AND itemName = '%s'", orderid, itemDelete);

                    //System.out.println("execute query");

                    if( (esql.executeQuery(checkItem)) <= 0) {
                        System.out.println("Item does not exist in order");
                        break;
                    }

                    String queryItem = String.format("SELECT Menu.price FROM Menu WHERE Menu.itemName = '%s'", itemDelete);

                    String addItemPrice1 = (esql.executeQueryAndReturnResult(queryItem)).get(0).get(0).trim();

                    double price = Double.parseDouble(addItemPrice1);

                    String priceQuery = String.format("SELECT total FROM Orders WHERE orderid = '%s'", orderid);

                    List<List<String>> totalPriceResult = esql.executeQueryAndReturnResult(priceQuery);

                    total = Double.parseDouble(totalPriceResult.get(0).get(0)) - price;

                    System.out.println("The updated total is: " + total);

                    Date date = new Date();
                    long time = date.getTime();
                    Timestamp timeStamp = new Timestamp(time);
                    String insert = String.format("UPDATE Orders SET total = '%s' WHERE orderid = '%s'", total, orderid);

                    esql.executeUpdate(insert);

                    //System.out.println("update order executed");

                    String updateItemStatus = String.format("DELETE FROM ItemStatus WHERE ItemStatus.itemName = '%s' AND ItemStatus.orderid = '%s'", itemDelete, orderid);

                    esql.executeUpdate(updateItemStatus);
                }
                else if(choice == 3) {
                    String deleteItemStatus = String.format("DELETE FROM ItemStatus WHERE orderid = '%s'", orderid);

                    esql.executeUpdate(deleteItemStatus);

                    String query = String.format("DELETE FROM Orders WHERE orderid = '%s'", orderid);

                    esql.executeUpdate(query);
                }
                else if(choice == 9) {
                    options = false;
                }
                else {
                    System.out.println("Unrecognized input");
                }

            }

        }catch (Exception e){
            System.err.println (e.getMessage());
        }
    }//end

    public static void EmployeeUpdateOrder(Cafe esql){
        // Your code goes here.
        // ...
        // ...
        System.out.print("Please enter the orderid you wish to update: " + '\n');

        try {
            String orderid = in.readLine();
            double total = 0;
            boolean options = true;
            while(options) {
                System.out.println("Update Order Options");
                System.out.println("---------");
                System.out.println("1. Add an Item");
                System.out.println("2. Delete an Item");
                System.out.println("3. Cancel Order");
                System.out.println(".........................");
                System.out.println("9. Finished");

                int choice = readChoice();

                if(choice == 1) {
                    System.out.println("What item are you adding for your order?");
                    String item = in.readLine();
                    String query = String.format("SELECT Menu.price FROM Menu WHERE Menu.itemName = '%s'", item);

                    List<List<String>> l = esql.executeQueryAndReturnResult(query);

                    String addItemPrice = l.get(0).get(0).trim();

                    //System.out.print("Converted String to Int");

                    double price = Double.parseDouble(addItemPrice);

                    String priceQuery = String.format("SELECT total FROM Orders WHERE orderid = '%s'", orderid);

                    List<List<String>> totalPriceResult = esql.executeQueryAndReturnResult(priceQuery);

                    total = price + Double.parseDouble(totalPriceResult.get(0).get(0).trim());

                    Date date = new Date();
                    long time = date.getTime();
                    Timestamp timeStamp = new Timestamp(time);
                    String insert = String.format("UPDATE Orders SET total = '%s' WHERE orderid = '%s'", total, orderid);

                    esql.executeUpdate(insert);

                    String status = "Pending";

                    System.out.println("Comments for your additional order: ");

                    String comments = in.readLine();

                    String insertItemStatus = String.format("INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status, comments) VALUES ('%s', '%s', '%s', '%s', '%s')", orderid, item, timeStamp, status, comments);

                    esql.executeUpdate(insertItemStatus);
                }
                else if(choice == 2) {
                    System.out.println("What item are you deleting for your order?");
                    String itemDelete = in.readLine();

                    String checkItem = String.format("SELECT itemName FROM ItemStatus WHERE orderid = '%s' AND itemName = '%s'", orderid, itemDelete);

                    //System.out.println("execute query");

                    if( (esql.executeQuery(checkItem)) <= 0) {
                        System.out.println("Item does not exist in order");
                        break;
                    }

                    String queryItem = String.format("SELECT Menu.price FROM Menu WHERE Menu.itemName = '%s'", itemDelete);

                    String addItemPrice1 = (esql.executeQueryAndReturnResult(queryItem)).get(0).get(0).trim();

                    double price = Double.parseDouble(addItemPrice1);

                    String priceQuery = String.format("SELECT total FROM Orders WHERE orderid = '%s'", orderid);

                    List<List<String>> totalPriceResult = esql.executeQueryAndReturnResult(priceQuery);

                    total = Double.parseDouble(totalPriceResult.get(0).get(0)) - price;

                    System.out.println("The updated total is: " + total);

                    Date date = new Date();
                    long time = date.getTime();
                    Timestamp timeStamp = new Timestamp(time);
                    String insert = String.format("UPDATE Orders SET total = '%s' WHERE orderid = '%s'", total, orderid);

                    esql.executeUpdate(insert);

                    //System.out.println("update order executed");

                    String updateItemStatus = String.format("DELETE FROM ItemStatus WHERE ItemStatus.itemName = '%s' AND ItemStatus.orderid = '%s'", itemDelete, orderid);

                    esql.executeUpdate(updateItemStatus);
                }
                else if(choice == 3) {
                    String deleteItemStatus = String.format("DELETE FROM ItemStatus WHERE orderid = '%s'", orderid);

                    esql.executeUpdate(deleteItemStatus);

                    String query = String.format("DELETE FROM Orders WHERE orderid = '%s'", orderid);

                    esql.executeUpdate(query);
                }
                else if(choice == 9) {
                    options = false;
                }
                else {
                    System.out.println("Unrecognized input");
                }

            }

        }catch (Exception e){
            System.err.println (e.getMessage());
        }
    }//end

    public static void ViewOrderHistory(Cafe esql){
        // Your code goes here.
        // ...
        // ...
        try{
            System.out.println("Listing 5 of the most recent orders");
            String query = String.format("SELECT * FROM Orders WHERE login = '%s' ORDER BY orderid DESC LIMIT 5", authorisedUser);

            esql.executeQueryAndPrintResult(query);

        }catch(Exception e){
            System.err.println (e.getMessage());
        }
    }//end

    public static void UpdateUserInfo(Cafe esql){
        // Your code goes here.
        // ...
        // ...
        try {
            boolean options = true;
            while(options) {
                System.out.println("Update User Options");
                System.out.println("---------");
                System.out.println("1. Update Phone Number");
                System.out.println("2. Update Password");
                System.out.println("3. Update Favorite Items");
                System.out.println(".........................");
                System.out.println("9. Finished");

                int choice = readChoice();

                if(choice == 1) {
                    System.out.println("Enter in your phone number: ");
                    String phoneNumber = in.readLine();
                    String query = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s'", phoneNumber, authorisedUser);

                    esql.executeUpdate(query);

                }
                else if(choice == 2) {
                    System.out.println("Enter in your new password: ");
                    String passW = in.readLine();
                    String query = String.format("UPDATE Users SET password = '%s' WHERE login = '%s'", passW, authorisedUser);

                    esql.executeUpdate(query);
                }
                else if(choice == 3) {
                    System.out.println("Enter in your favorite Items: ");
                    String items = in.readLine();
                    String query = String.format("UPDATE Users SET favItems = '%s' WHERE login = '%s'", items, authorisedUser);

                    esql.executeUpdate(query);
                }
                else if(choice == 9) {
                    options = false;
                }
                else {
                    System.out.println("Unrecognized input");
                }
            }

        }catch (Exception e){
            System.err.println (e.getMessage());
        }
    }//end

    public static void ManagerUpdateUserInfo(Cafe esql){
        // Your code goes here.
        // ...
        // ...
        try {
            boolean options = true;
            System.out.println("Which user would you like to update?");
            String selectUser = in.readLine();
            String sUser = String.format("SELECT Users.login FROM Users WHERE login = '%s'", selectUser);
            if( (esql.executeQuery(sUser)) <= 0) {
                System.out.println("User does not exist");
                options = false;
            }
            while(options) {
                System.out.println("Update User Options");
                System.out.println("---------");
                System.out.println("1. Update Phone Number");
                System.out.println("2. Update Password");
                System.out.println("3. Update Favorite Items");
                System.out.println("4. Type of User");
                System.out.println(".........................");
                System.out.println("9. Finished");

                int choice = readChoice();

                if(choice == 1) {
                    System.out.println("Enter in your phone number: ");
                    String phoneNumber = in.readLine();
                    String query = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s'", phoneNumber, selectUser);

                    esql.executeUpdate(query);
                }
                else if(choice == 2) {
                    System.out.println("Enter in your new password: ");
                    String passW = in.readLine();
                    String query = String.format("UPDATE Users SET password = '%s' WHERE login = '%s'", passW, selectUser);

                    esql.executeUpdate(query);
                }
                else if(choice == 3) {
                    System.out.println("Enter in your favorite Items: ");
                    String items = in.readLine();
                    String query = String.format("UPDATE Users SET favItems = '%s' WHERE login = '%s'", items, selectUser);

                    esql.executeUpdate(query);
                }
                else if(choice == 4) {
                    System.out.println("Enter the user type: ");
                    String type = in.readLine();
                    String query = String.format("UPDATE Users SET type = '%s' WHERE login = '%s'", type, selectUser);

                    esql.executeUpdate(query);
                }
                else if(choice == 9) {
                    options = false;
                }
                else {
                    System.out.println("Unrecognized input");
                }
            }

        }catch (Exception e){
            System.err.println (e.getMessage());
        }
    }//end

    public static void UpdateMenu(Cafe esql){
        // Your code goes here.
        // ...
        // ...
        try {
            boolean options = true;
            System.out.println("Options for the menu: ");
            while(options) {
                System.out.println("Update Menu Options");
                System.out.println("---------");
                System.out.println("1. Add an Item");
                System.out.println("2. Edit item type");
                System.out.println("3. Edit price of the item");
                System.out.println(".........................");
                System.out.println("9. Finished");

                int choice = readChoice();

                if(choice == 1) {
                    System.out.println("What item are you adding to the menu?");
                    String item = in.readLine();
                    System.out.println("What item type is this?");
                    String type = in.readLine();
                    System.out.println("What is the price of the item?");
                    double price = Double.parseDouble(in.readLine());
                    System.out.println("How would you describe the item?");
                    String desc = in.readLine();
                    System.out.println("What imageURL of the item?");
                    String image = in.readLine();
                    String query = String.format("INSERT INTO Menu (itemName, type, price, description, imageURL) VALUES('%s', '%s', '%s', '%s', '%s')", item, type, price, desc, image);

                    esql.executeUpdate(query);

                }
                else if(choice == 2) {
                    System.out.println("What item would you like to edit?");
                    String item = in.readLine();

                    String itemCheck = String.format("SELECT itemName FROM Menu WHERE itemName = '%s'", item);

                    if( (esql.executeQuery(itemCheck)) <= 0) {
                        System.out.println("Item does not exist");
                        break;
                    }

                    System.out.println("Update item type to: ");
                    String type = in.readLine();

                    String query = String.format("UPDATE Menu SET type = '%s' WHERE itemName = '%s'", type, item);

                    esql.executeUpdate(query);

                }
                else if(choice == 3) {
                    System.out.println("What item would you like to edit?");
                    String item = in.readLine();

                    String itemCheck = String.format("SELECT itemName FROM Menu WHERE itemName = '%s'", item);

                    if( (esql.executeQuery(itemCheck)) <= 0) {
                        System.out.println("Item does not exist");
                        break;
                    }

                    System.out.println("Update item price to: ");
                    Double price = Double.parseDouble(in.readLine());

                    String query = String.format("UPDATE Menu SET price = '%s' WHERE itemName = '%s'", price, item);

                    esql.executeUpdate(query);
                }
                else if(choice == 9) {
                    options = false;
                }
                else {
                    System.out.println("Unrecognized input");
                }
            }

        }catch (Exception e){
            System.err.println (e.getMessage());
        }
    }//end

    public static void ViewOrderStatus(Cafe esql){
        // Your code goes here.
        // ...
        // ...
        System.out.println("Which order would you like to view?");
        try {
            int orderid = Integer.parseInt(in.readLine());

            String query = String.format("SELECT DISTINCT ItemStatus.orderid, ItemStatus.itemName, ItemStatus.lastUpdated, ItemStatus.status, ItemStatus.comments FROM ItemStatus, Orders WHERE ItemStatus.orderid = '%s' AND Orders.login = '%s'", orderid, authorisedUser);

            esql.executeQueryAndPrintResult(query);
        }catch (Exception e){
            System.err.println (e.getMessage());
        }
    }//end

    public static void ViewCurrentOrder(Cafe esql){
        // Your code goes here.
        // ...
        // ...
        System.out.println("Which order would you like to view?");
        try {
            int orderid = Integer.parseInt(in.readLine());

            String query = String.format("SELECT DISTINCT ItemStatus.orderid, ItemStatus.itemName, ItemStatus.lastUpdated, ItemStatus.status, ItemStatus.comments FROM ItemStatus, Orders WHERE ItemStatus.orderid = '%s'", orderid);

            esql.executeQueryAndPrintResult(query);
        }catch (Exception e){
            System.err.println (e.getMessage());
        }
    }//end

    public static void Query6(Cafe esql){
        // Your code goes here.
        // ...
        // ...
    }//end Query6
}
