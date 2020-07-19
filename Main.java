package application;

/**
 * Filename: Main.java Project: group17 Authors: Rosalie Cai, Ruiqi Hu
 */
import java.sql.*;
import java.awt.Label;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.chrono.MinguoChronology;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 * Social network visualizer of ateam 7
 * 
 * @author ateam7
 */
public class Main extends Application {
  // see if the program just started

  private boolean start = true;
  // private GridPane userBut = new GridPane();
  // store current coordinates of nodes
  private Map<String, ArrayList<Double>> coordinate = new HashMap<String, ArrayList<Double>>();
  private List<String> args;
  private static final int WINDOW_WIDTH = 1200;// final value for pop-up window's width
  private static final int WINDOW_HEIGHT = 768;// final value for pop-up window's height

  static Stage pstage;
  static Stage dia;
  // set title of this project
  private static final String APP_TITLE = "Animal Shelter @ Austin  CS564-Group17";

  public static MenuBar menuBar;// general data manipulation
  public static VBox rightBox;// create vBox of right box
  public static VBox leftBox;// general data search
  public static VBox bottomBox;
  public static VBox middleBox;
  public static VBox upBox;

  Connection conn;
  Statement stmt;
  int nextUserID = 10030;
  int nextAnimalID = 176977;

  // check userName
  private static String userType = "ADOPTER";
  private static int userID = 0;
  private static String menuType;
  private static boolean newUser = false;

  int change = 0;

  ObservableList<String> row;// row selected from table
  String query;
  String groupByBY = null;

  // ----------------------------------------SQL--------------------------------------------
  /**
   * set up connection
   * 
   * @param type
   * @throws SQLException
   */
  private void setUpConnection() throws SQLException {
    String databasePrefix = "animal_shelter";
    String hostName = "localhost";
    String databaseURL = "jdbc:mysql://127.0.0.1:3306/" + databasePrefix + "?user=" + hostName
        + "&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
    String password = "CS564-G17";
    conn = DriverManager.getConnection(databaseURL, "admin", password);

    stmt = conn.createStatement();
  }

  // Use generic types
  private BarChart<String, Number> getItemGraphStatistics(ResultSet rs) {
    final CategoryAxis xAxis = new CategoryAxis();
    final NumberAxis yAxis = new NumberAxis();
    final BarChart<String, Number> bc = new BarChart<String, Number>(xAxis, yAxis);

    bc.setTitle("Shelter Summary");
    bc.setAnimated(true);
    bc.setMaxSize(600, 600);
    bc.setVerticalGridLinesVisible(false);
    xAxis.setLabel("Species");
    xAxis.setTickLabelRotation(40);
    yAxis.setLabel("Number");

    try {
      while (rs.next()) {
        String item = rs.getString(1);
        int count = rs.getInt(2);
        XYChart.Series series = new XYChart.Series();
        series.getData().add(new XYChart.Data(item, count));
        bc.getData().add((series));

      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return bc;
  }

  /**
   * present results based on whehter the user selected dog_cat or others to start searching
   * 
   * @return
   * @throws SQLException
   */
  private ResultSet storedProcedure_Prelim_Sort_menuType() throws SQLException {
    CallableStatement myCallStmt = conn.prepareCall("call " + "basicAnimalSelection(?)");
    myCallStmt.setString(1, menuType);
    ResultSet total = myCallStmt.executeQuery();
    return total;

  }

  private void add_Animal(String Animal_Name, String Classification_Breed_Name, String Base_Color,
      String Outcome_type, String Sex, int Temp_Combination_Number, String Date_of_Birth)
      throws SQLException {
    // query for grouping animal
    String query = "INSERT INTO Animal (Animal_ID,Animal_Name,Classification_Breed_Name,Base_Color,"
        + "Outcome_type, Sex,  Temp_Combination_Number, Date_of_Birth) VALUES (" + nextAnimalID
        + ",'" + Animal_Name + "','" + Classification_Breed_Name + "','" + Base_Color + "','"
        + Outcome_type + "','" + Sex + "'," + Temp_Combination_Number + ",'" + Date_of_Birth
        + "');";
    stmt.executeUpdate(query);
    nextAnimalID++;
  }

  private void remove_Animal(int ID) throws SQLException {
    // query for grouping animal
    String query = "delete from Animal " + "Where Animal_ID = " + ID;
    stmt.executeUpdate(query);
  }

  private void add_User(String Name, String Phone, String Address,
      String Notes_and_Criteria_for_adoption, float Overall_Neighbor_Rating,
      int Total_Number_of_Pets_up_to_now) throws SQLException {

    String query = "insert into All_Users (User_ID,Name,Phone,Address) values(" + userID + ",'"
        + Name + "','" + Phone + "','" + Address + "');";
    stmt.executeUpdate(query);

    if (userType.equals("SURRENDER")) {
      String query1 = "insert into Surrender_Owner (User_ID,Overall_Neighbor_Rating) values("
          + userID + ",'" + Notes_and_Criteria_for_adoption + "');";
      stmt.executeUpdate(query1);

    } else if (userType.equals("ADOPTER")) {
      String query2 =
          "insert into Potential_Adopter (A_User_ID,Overall_Neighbor_Rating,Total_Number_of_Pets_up_to_now) values("
              + userID + "," + Overall_Neighbor_Rating + "," + Total_Number_of_Pets_up_to_now
              + ");";
      stmt.executeUpdate(query2);
    }
  }
  
  


  private void alter_User(String Name, String Phone, String Address,
      String Notes_and_Criteria_for_adoption, float Overall_Neighbor_Rating,
      int Total_Number_of_Pets_up_to_now) throws SQLException {
    String query = "update All_Users " + "set Name = '" + Name + "'," + "Phone='" + Phone + "',"
        + "Address='" + Address + "'  " + "Where User_ID = " + userID + ";";
    stmt.executeUpdate(query);

    if (userType.equals("SURRENDER")) {
      query = "update Surrender_Owner " + "set Notes_and_Criteria_for_adoption = '"
          + Notes_and_Criteria_for_adoption + "' " + "Where User_ID = " + userID + ";";
    } else if (userType.equals("ADOPTOR")) {

      query = "update Potential_Adopter " + "set Overall_Neighbor_Rating = "
          + Overall_Neighbor_Rating + "," + "Total_Number_of_Pets_up_to_now="
          + Total_Number_of_Pets_up_to_now + "  Where A_User_ID = " + userID + ";";
    }
  }

  // TODO
  private void remove_User(int ID) throws SQLException {
    if (userType.equals("ADMIN")) {
      // query for grouping animal
      String query = "delete from All_Users " + "Where User_ID = " + ID;
      stmt.executeUpdate(query);
    }
  }

  
  private ArrayList<String> full_user_info() throws SQLException {
    ArrayList<String> result=new ArrayList<>();
   
    String query = "Select Name,Phone,Address " + "From All_Users " + "Where User_ID = " + userID + ";";
    ResultSet rs = stmt.executeQuery(query);
    if (rs.next()) {
      result.add(rs.getString(1));
      result.add(rs.getString(2));
      result.add(rs.getString(3));
    }
    if(userType.equals("ADOPTER")) {
    query = "Select Overall_Neighbor_Rating, Total_Number_of_Pets_up_to_now " + "From Potential_Adopter " + "Where A_User_ID = " + userID + ";";
    rs = stmt.executeQuery(query);
    if (rs.next()) {
      result.add(rs.getFloat(1)+"");
      result.add(rs.getInt(2)+"");
    }
    }
    if(userType.equals("SURRENDER")) {
    query = "Notes_and_Criteria_for_adoption " + "From Surrender_Owner " + "Where User_ID = " + userID + ";";
    rs = stmt.executeQuery(query);
    if (rs.next()) {
      result.add(rs.getString(1));
    }
    }
    return result;
  }
  
  
  private String[] user_info() throws SQLException {
    String query = "Select Name,Phone " + "From All_Users " + "Where User_ID = " + userID + ";";
    ResultSet rs = stmt.executeQuery(query);
    if (rs.next()) {
      return new String[] {rs.getString(1), rs.getString(2)};
    }
    return null;
  }
  
  

  private String link_account() throws SQLException {
    String[] rss = user_info();
    String Name = rss[0];
    String Phone = rss[1];
    String query = "Select u1.User_ID,u2.User_ID " + "From All_Users u1, All_Users u2 "
        + "Where u1.Name = u2.Name AND u1.Phone = u2.Phone AND strcmp(u1.Phone, u2.Phone)<0 "
        + "AND u1.User_ID in (select A_User_ID from Potential_Adopter) AND u1.Name ='"
        + Name + "' AND u1.Phone ='" + Phone + "' " + "Order by u1.User_ID;";
    ResultSet rs = stmt.executeQuery(query);
    if (rs.next()) {
      return connectUserInfo(rs.getInt(1), rs.getInt(2));
    }
    return null;

  }

  private String connectUserInfo(int AdoptorId, int SurrID) throws SQLException {

    String results = "";
    CallableStatement myCallStmt = conn.prepareCall("call " + "connectUserInfo(?,?)");

    myCallStmt.setInt(1, AdoptorId);
    myCallStmt.setInt(2, SurrID);

    ResultSet total = myCallStmt.executeQuery();
    if (total.next()) {
      results = "\n    Phone: " + total.getString(1) + "\n    Address: " + total.getString(2);
    }
    myCallStmt.getMoreResults();

    total = myCallStmt.getResultSet();
    if (total.next())
      results += "     Adopter Account:\n" + "Overall_Neighbor_Rating: +" + total.getInt(1)
          + "\nTotal_Number_of_Pets_up_to_now: " + total.getInt(2) + "\nAdopt_Animal_Animal_ID: "
          + total.getInt(3) + "\nAdopt_Time: " + total.getString(4);

    myCallStmt.getMoreResults();
    total = myCallStmt.getResultSet();
    if (total.next())
      results += "     Surrender Account:\n" + "Animal_Animal_ID: +" + total.getInt(1)
          + "\nGives_up_Time: " + total.getString(4);

    return results;
  }

  /**
   * present results based on whehter the user selected or others to start searching
   * 
   * @return
   * @throws SQLException
   */
  private String storedProcedure_Prelim_Sort_userType() throws SQLException {
    String results = "";
    CallableStatement myCallStmt = conn.prepareCall("call " + "basicUserSelection(?)");

    myCallStmt.setInt(1, userID);

    ResultSet total = myCallStmt.executeQuery();
    if (total.next()) {
      results =
          "\n    User ID: " + total.getInt("User_Id") + "\n    Name: " + total.getString("Name");
    }
    return results;
  }

  /**
   * present results based on whehter the user selected dog_cat or others to start searching
   * 
   * @return
   * @throws SQLException
   */
  private String storedProcedure_basicDebrief() throws SQLException {
    String results = "";
    CallableStatement myCallStmt = conn.prepareCall("call " + "basicDebrief");

    ResultSet rs1 = myCallStmt.executeQuery();
    if (rs1.next())
      results = "     The total number of animals in our shelter is " + rs1.getInt(1) + " ,from "
          + rs1.getInt(2) + " species\n";
    nextAnimalID = rs1.getInt(1) + 100000 + 49;
    myCallStmt.getMoreResults();

    rs1 = myCallStmt.getResultSet();
    if (rs1.next())
      results +=
          "     The total number of users registered at our shelter is " + rs1.getInt(1) + "\n";
    nextUserID = rs1.getInt(1) + 10000;
    myCallStmt.getMoreResults();


    rs1 = myCallStmt.getResultSet();
    if (rs1.next())
      results += "           " + rs1.getInt(1) + " are Potential_Adopters\n";

    myCallStmt.getMoreResults();
    rs1 = myCallStmt.getResultSet();
    if (rs1.next())
      results += "           " + rs1.getInt(1) + " are Surrender_Owners";

    return results;
  }



  private String get_Animal_Temperament(int Animal_ID) throws SQLException {
    String Animal_attribute = null;
    String location = null;
    int Temperatment = 0;
    String breed = null;
    query = "Select t.Is_loving, t.Is_calm, t.Is_shy " + "From Animal a, Temperament t "
        + "Where a.Animal_ID =" + Animal_ID
        + " and a.Temp_Combination_Number= t.Temp_Combination_Number;";
    ResultSet rs = stmt.executeQuery(query);

    String result = "";
    if (rs.next()) {
      result += "Is_loving: " + (rs.getInt(1) == 1 ? "YES\n" : "NO\n") + "Is_calm: "
          + (rs.getInt(2) == 1 ? "YES\n" : "NO\n") + "Is_shy : "
          + (rs.getInt(3) == 1 ? "YES\n" : "NO\n");
    }
    return result;
  }

  /**
   * get result based on search conditions
   * 
   * @param stringAttributes
   * @return
   */
  private ResultSet animal_Search(String Classification_Breed_Name, String Base_Color,
      String Outcome_type, String Sex, String location) {
    // ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>()
    String attr = "";
    if (Classification_Breed_Name.length() != 0 && Classification_Breed_Name != null) {
      attr += "Classification_Breed_Name='" + Classification_Breed_Name + "'";
    }
    if (Base_Color.length() != 0 && Base_Color != null) {
      if (attr.length() > 0) {
        attr += " AND Base_Color='" + Base_Color + "'";
      } else {
        attr += "Base_Color='" + Base_Color + "'";
      }
    }
    if (Outcome_type.length() != 0 && Outcome_type != null) {
      if (attr.length() > 0) {
        attr += " AND Outcome_type='" + Outcome_type + "'";
      } else {
        attr += "Outcome_type='" + Outcome_type + "'";
      }
    }
    if (Sex.length() != 0 && Sex != null) {
      if (attr.length() > 0) {
        attr += " AND Sex='" + Sex + "'";
      } else {
        attr += "Sex='" + Sex + "'";
      }
    }
    if (attr.length() > 0) {
      attr += ";";
    }

    String loc = "";
    if (location.length() != 0 && location != null) {
      loc = "Animal_ID in (select At_Animal_Animal_ID from At where Location_Found_Address = '"
          + location + "') and";
    }

    try {
      String query;

      if (menuType.equals("dog_cat")) {
        if (attr.length() > 0) {
          query = "SELECT * " + " FROM Animal " + " WHERE " + loc
              + " Classification_Breed_Name IN (select Breed_Name " + "From Classification "
              + "where Specis_Name = 'Dog' or Specis_Name = 'Cat')" + "and " + attr;
        } else {
          query = "SELECT * " + " FROM Animal " + " WHERE " + loc
              + " Classification_Breed_Name IN (select Breed_Name " + "From Classification "
              + "where Specis_Name = 'Dog' or Specis_Name = 'Cat');";
        }
      } else {
        if (attr.length() > 0) {
          query = "SELECT *  FROM Animal " + " WHERE " + loc
              + " Classification_Breed_Name IN (select Breed_Name " + "From Classification "
              + "where NOT(Specis_Name = 'Dog' or Specis_Name = 'Cat')) " + "and " + attr;
        } else {
          query = "SELECT *  FROM Animal " + " WHERE " + loc
              + " Classification_Breed_Name IN (select Breed_Name " + "From Classification "
              + "where NOT(Specis_Name = 'Dog' or Specis_Name = 'Cat')); ";
        }
      }

      ResultSet rs = stmt.executeQuery(query);
      // while (rs.next()) {
      // ArrayList<String> row = new ArrayList<String>();
      // for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
      // row.add(rs.getString(i));
      // }
      // result.add(row);
      // }
      return rs;
    } catch (Exception e) {
      System.out.println("Error on animal_Search");

    }
    // return result;
    return null;

  }

  private String storedProcedure_search_move_Date_and_history(int ID) throws SQLException {
    String results = "";
    CallableStatement myCallStmt = conn.prepareCall("call " + "search_move_Date_and_history(?)");
    myCallStmt.setInt(1, ID);

    ResultSet rs = myCallStmt.executeQuery();
    if (rs.next())
      results = "Take_in_Date: " + rs.getString(1) + "\nIntake_Type: " + rs.getString(2)
          + "\nFound_Address" + rs.getString(3) + "\nl.Intake_Condition: " + rs.getString(4);

    return results;
  }

  /**
   * for observable list
   * 
   * @param groupBy2
   * @param string4
   * @param string5
   * @param string3
   * @param string2
   * 
   * @param attributes
   *
   * @return*@throws SQLException
   */

  ///// breedName.getText(), baseColor.getText(), outcomeType.getText(),
  // gender.getText(), animalLoc.getText()

  private ObservableList<PieChart.Data> pieChart_Animal_Search(String Classification_Breed_Name,
      String Base_Color, String Outcome_type, String Sex, String groupBy) throws SQLException {
    ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

    String attr = "";
    if (Classification_Breed_Name.length() != 0 && Classification_Breed_Name != null) {
      attr += "Classification_Breed_Name='" + Classification_Breed_Name + "'";
    }
    if (Base_Color.length() != 0 && Base_Color != null) {
      if (attr.length() > 0) {
        attr += " AND Base_Color='" + Base_Color + "'";
      } else {
        attr += "Base_Color='" + Base_Color + "'";
      }
    }
    if (Outcome_type.length() != 0 && Outcome_type != null) {
      if (attr.length() > 0) {
        attr += " AND Outcome_type='" + Outcome_type + "'";
      } else {
        attr += "Outcome_type='" + Outcome_type + "'";
      }
    }
    if (Sex.length() != 0 && Sex != null) {
      if (attr.length() > 0) {
        attr += " AND Sex='" + Sex + "'";
      } else {
        attr += "Sex='" + Sex + "'";
      }
    }
    if (attr.length() > 0) {
      attr = " Where " + attr;
    }

    String query = "Select " + groupBy + ", count(" + groupBy + ") "
        + "From (Animal inner join Temperament) join Classification on "
        + "Classification_Breed_Name = Breed_Name "
        + attr + " " + "Group by " + groupBy + ";";

    ResultSet rs = stmt.executeQuery(query);

    try {
      while (rs.next()) {
        // adding data on piechart data
        if (groupBy.equals("Animal_ID")) {
          data.add(new PieChart.Data(rs.getInt(1) + "", rs.getInt(2)));
        } else {
          data.add(new PieChart.Data(rs.getString(1), rs.getInt(2)));
        }
      }
    } catch (Exception e) {
      System.out.println("Error on DB connection");
    }

    return data;
  }

  // ------------------------------------GUI----------------------------------------------
  /**
   * prepare the stage for loading a new page
   */
  private void clearPage() {
    menuBar = null;// general data manipulation
    rightBox = null;// create vBox of right box
    leftBox = null;// general data search
    bottomBox = null;
    middleBox = null;
    upBox = null;
    dia.close();
  }

  /**
   * set up BeginPage
   */
  private void setBeginPage() {
    try {
      // clear the page for setting up new pages if this is not the first time the
      // program has
      // started
      if (!start) {
        clearPage();
      }
      // if this program just started, set up connection to the database
      else {
        setUpConnection();
      }

      set_BeginPage_MiddleBox();
      bottomBox = new VBox();
      bottomBox.setPrefHeight(100);

      // update the stage with newly set up GUI
      if (!start) {
        start(pstage);
      }

    } catch (SQLException e1) {
      System.out.println("Loading database failed");
    }

  }

  /**
   * set up the bottom box
   * 
   * @throws FileNotFoundException
   */
  private void set_BeginPage_MiddleBox() {
    middleBox = new VBox();

    // setting up the page
    Image title = new Image(getClass().getResource("Title.png").toString(), true);
    ImageView titleImage = new ImageView(title);
    middleBox.getChildren().add(titleImage);

    Image begin = new Image(getClass().getResource("begin.jpg").toString(), true);
    ImageView beginImage = new ImageView(begin);
    middleBox.getChildren().add(beginImage);

    // set a login button beside the userName
    Button adoptlogin = new Button("ADOPTER");
    adoptlogin.setFont(new Font("Copperplate", 20));
    adoptlogin.setPrefSize(300, 40);
    Button surrenderlogin = new Button("SURRENDER");
    surrenderlogin.setFont(new Font("Copperplate", 20));
    surrenderlogin.setPrefSize(300, 40);
    Button adminlogin = new Button("ADMIN");
    adminlogin.setFont(new Font("Copperplate", 20));
    adminlogin.setPrefSize(300, 40);
    Button register = new Button("Register as a new User");
    register.setPrefSize(300, 40);
    register.setFont(new Font("Copperplate", 20));

    // set up functionality
    adoptlogin.setOnAction(ActionEvent -> {
      // set up the user type for future uses
      userType = "ADOPTER";
      getID();
    });

    surrenderlogin.setOnAction(ActionEvent -> {
      // set up the user type for future uses
      userType = "SURRENDER";
      // setMenuAddAnimalPage();
      getID();
    });

    adminlogin.setOnAction(ActionEvent -> {
      // set up the user type for future uses
      userType = "ADMIN";
      // setMenuAddAnimalPage();
      setMenuPage();
    });

    register.setOnAction(ActionEvent -> {
      // set up the user type for future uses
      // Todo:.....new page
      newUser = true;
      newUserPage();

    });

    middleBox.getChildren().add(adoptlogin);
    middleBox.getChildren().add(surrenderlogin);
    middleBox.getChildren().add(adminlogin);
    middleBox.getChildren().add(register);
    middleBox.setAlignment(Pos.CENTER);
  }

  /**
   * get the login id for user
   */
  private void getID() {
    clearPage();
    upBox = new VBox();
    middleBox = new VBox();

    Button back = new Button("Back");
    back.setFont(new Font("Copperplate", 20));
    back.setPrefSize(150, 40);
    back.setOnAction(e -> {
      setBeginPage();

    });

    upBox.getChildren().add(back);

    // setting up the page
    Image title = new Image(getClass().getResource("Title.png").toString(), true);
    ImageView titleImage = new ImageView(title);
    middleBox.getChildren().add(titleImage);

    Image begin = new Image(getClass().getResource("begin.jpg").toString(), true);
    ImageView beginImage = new ImageView(begin);
    middleBox.getChildren().add(beginImage);

    Button prompt = new Button("Hello there! Please type your userID!");
    prompt.setFont(new Font("Copperplate", 20));
    prompt.setPrefSize(500, 40);
    TextField userId = new TextField();
    userId.setMaxSize(500, 50);
    userId.setMinSize(500, 50);
    Button submit = new Button("Submit & Begin");
    submit.setFont(new Font("Copperplate", 20));
    submit.setPrefSize(500, 40);
    submit.setOnAction(e -> {
      if ((userId.getText() != null && !userId.getText().isEmpty())) {
        userID = Integer.parseInt(userId.getText());
        setMenuPage();

      }
    });
    middleBox.getChildren().addAll(prompt, userId, submit);
    middleBox.setAlignment(Pos.CENTER);

    start(pstage);

  }

  private void setMenuAddAnimalPage() {
    clearPage();
    leftBox = new VBox();
    upBox = new VBox();
    middleBox = new VBox();
    leftBox.setPrefWidth(400);
    upBox.setPrefHeight(150);

    Button back = new Button("Back");
    back.setFont(new Font("Copperplate", 20));
    back.setPrefSize(150, 40);
    back.setOnAction(e -> {
      // if(userType.equals("ADMIN"))
      setMenuPage();

    });
    upBox.getChildren().add(back);

    GridPane gp = new GridPane();
    gp.setMinSize(620, 400);
    gp.setPadding(new Insets(10, 10, 10, 10));

    // TODO add animal
    Text name = new Text("Animal name");
    name.setFont(new Font("Copperplate", 20));
    TextField animalName = new TextField();

    Text breed = new Text("Breed");
    breed.setFont(new Font("Copperplate", 20));
    TextField breedName = new TextField();

    Text color = new Text("Color");
    color.setFont(new Font("Copperplate", 20));
    TextField baseColor = new TextField();

    Text type = new Text("Outcome Type");
    type.setFont(new Font("Copperplate", 20));
    TextField outcomeType = new TextField();

    Text sex = new Text("Sex");
    sex.setFont(new Font("Copperplate", 20));
    TextField gender = new TextField();

    Text num = new Text("Number");
    num.setFont(new Font("Copperplate", 20));
    TextField tempNum = new TextField();

    Text birth = new Text("Date of Birth");
    birth.setFont(new Font("Copperplate", 20));
    TextField birthday = new TextField();

    Button addAnimal = new Button("add animal");
    addAnimal.setFont(Font.font("Copperplate", 20));
    addAnimal.setPrefSize(200, 40);

    addAnimal.setOnAction(e -> {
      try {

        add_Animal(animalName.getText(), breedName.getText(), baseColor.getText(),
            outcomeType.getText(), gender.getText(), Integer.parseInt(tempNum.getText()),
            birthday.getText());
        // setUserAcountPage();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    });

    Text ID = new Text("Animal ID");
    ID.setFont(Font.font("Copperplate", 20));
    TextField animalId = new TextField();

    // remove animal
    Button removeAnimal = new Button("remove animal");
    removeAnimal.setFont(Font.font("Copperplate", 20));
    removeAnimal.setPrefSize(200, 40);

    removeAnimal.setOnAction(e -> {
      try {
        remove_Animal(Integer.parseInt(animalId.getText()));

      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    });



    gp.add(name, 0, 0);
    gp.add(animalName, 1, 0);
    gp.add(breed, 0, 1);
    gp.add(breedName, 1, 1);
    gp.add(color, 0, 2);
    gp.add(baseColor, 1, 2);
    gp.add(type, 0, 3);
    gp.add(outcomeType, 1, 3);
    gp.add(sex, 0, 4);
    gp.add(gender, 1, 4);
    gp.add(num, 0, 5);
    gp.add(tempNum, 1, 5);
    gp.add(birth, 0, 6);
    gp.add(birthday, 1, 6);
    gp.add(addAnimal, 3, 7);

    gp.add(ID, 0, 8);
    gp.add(animalId, 1, 8);
    gp.add(removeAnimal, 3, 9);


    middleBox.getChildren().addAll(gp);
    start(pstage);
  }

  private void setMenuPage() {
    clearPage();

    set_MenuPage_MiddleBox();
    setMenuPage_upBox();
    start(pstage);

  }

  private void setMenuPage_upBox() {
    upBox = new VBox();

    Button back = new Button("Back");
    back.setFont(new Font("Copperplate", 20));
    back.setPrefSize(100, 40);
    back.setOnAction(e -> {
      setBeginPage();

    });
    Text tx = null;
    try {
      tx = new Text(storedProcedure_Prelim_Sort_userType());
      tx.setFont(Font.font("Copperplate", 20));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    leftBox = new VBox();
    rightBox = new VBox();
    leftBox.setPrefWidth(400);
    upBox.setPrefHeight(250);

    upBox.getChildren().addAll(back, tx);

  }

  private void set_MenuPage_MiddleBox() {

    middleBox = new VBox();

    Button search = new Button("Start Searching Animal");
    search.setPrefSize(300, 40);
    search.setFont(new Font("Copperplate", 20));
    Button statistic = new Button("Statistic for Animals");
    statistic.setPrefSize(300, 40);
    statistic.setFont(new Font("Copperplate", 20));
    Button userAccount = new Button("User Account");
    userAccount.setFont(new Font("Copperplate", 20));
    userAccount.setPrefSize(300, 40);

    search.setOnAction(e -> {

      setPrelimSearchPage();

    });
    statistic.setOnAction(e -> {
      // connect on statistic method
      setStatsPage();
    });

    userAccount.setOnAction(e -> {
      setUserAcountPage();
    });

    middleBox.getChildren().addAll(search, statistic, userAccount);

    if (userType.equals("ADMIN") || userType.equals("SURRENDER")) {
      Button addRemove = new Button("Add or Remove Pets");
      addRemove.setFont(new Font("Copperplate", 20));
      addRemove.setPrefSize(300, 40);
      addRemove.setOnAction(e -> setMenuAddAnimalPage());
      middleBox.getChildren().add(addRemove);
    }
    if (userType.equals("ADMIN")) {
      Button arUser = new Button("Remove User");
      arUser.setFont(new Font("Copperplate", 20));
      arUser.setPrefSize(300, 40);
      arUser.setOnAction(e -> set_RemoveUserPage());
      middleBox.getChildren().add(arUser);
    }

  }

  private void setPrelimSearchPage() {
    clearPage();
    set_PrelimSearchPage_middleBox();

    set_PrelimSearch_Page_upBox();
    start(pstage);

  }

  private void set_PrelimSearchPage_middleBox() {
    middleBox = new VBox();

    // new girdPane
    GridPane gp = new GridPane();
    gp.setMinSize(650, 350);
    gp.setPadding(new Insets(10, 10, 10, 10));

    // --------------------------------------set up dog image
    Image dogCat = new Image(getClass().getResource("dog_cat.jpg").toString(), true);
    ImageView dogCatImage = new ImageView(dogCat);

    dogCatImage.setPickOnBounds(true); // allows click on transparent areas
    dogCatImage.setOnMouseClicked((MouseEvent e) -> {
      menuType = "dog_cat";
      setSearchPage();
    });

    RotateTransition rotate = new RotateTransition(Duration.seconds(3), dogCatImage);
    rotate.setByAngle(10);
    rotate.setCycleCount(Animation.INDEFINITE);
    rotate.setInterpolator(Interpolator.LINEAR);
    dogCatImage.setOnMouseEntered((MouseEvent e) -> {
      rotate.play();
    });
    dogCatImage.setOnMouseExited((MouseEvent e) -> {
      rotate.stop();
      dogCatImage.setRotate(0);
    });

    dogCatImage.setFitHeight(305 / 1.2);
    dogCatImage.setFitWidth(542 / 1.2);

    // -----------------------------------------
    Text dc = new Text();
    dc.setText("Click the image and search for cats and dogs!");
    dc.setFont(Font.font("Copperplate", 20));

    gp.add(dogCatImage, 0, 0);
    gp.add(dc, 1, 0);

    // ------------------------------------set up cat image
    Image others = new Image(getClass().getResource("others.jpg").toString(), true);
    ImageView othersImage = new ImageView(others);

    othersImage.setFitHeight(300 / 1.2);
    othersImage.setFitWidth(722 / 1.2);

    othersImage.setPickOnBounds(true); // allows click on transparent areas
    othersImage.setOnMouseClicked((MouseEvent e) -> {
      menuType = "others";
      setSearchPage();
    });

    RotateTransition rotate2 = new RotateTransition(Duration.seconds(3), othersImage);
    rotate2.setByAngle(10);
    rotate2.setCycleCount(Animation.INDEFINITE);
    rotate2.setInterpolator(Interpolator.LINEAR);
    othersImage.setOnMouseEntered((MouseEvent e) -> {
      rotate2.play();
    });
    othersImage.setOnMouseExited((MouseEvent e) -> {
      rotate2.stop();
      othersImage.setRotate(0);
    });

    // -----------------------------------------
    Text ot = new Text();
    ot.setText("Click the image and search for other pets!");
    ot.setFont(Font.font("Copperplate", 20));

    gp.add(othersImage, 0, 1);
    gp.add(ot, 1, 1);

    middleBox.getChildren().add(gp);
  }

  private void newUserPage() {
    clearPage();

    leftBox = new VBox();
    leftBox.setPrefWidth(450);
    upBox = new VBox();
    upBox.setPrefHeight(300);
    middleBox = new VBox();

    Button back = new Button("Back");
    back.setFont(new Font("Copperplate", 20));
    back.setPrefSize(100, 40);
    back.setOnAction(e -> {
      setBeginPage();

    });
    upBox.getChildren().add(back);

    Text pick = new Text("Click the Button Below to be an Adopter or a Surrender");
    pick.setStyle("-fx-font: 20 Copperplate;");
    Button adopter = new Button("Adopter");
    adopter.setFont(new Font("Copperplate", 20));
    adopter.setPrefSize(150, 40);
    adopter.setOnAction(e -> {
      userType = "ADOPTER";
      userID = nextUserID;
try {
  add_User("", "", "", "", 0f, 0);
} catch (SQLException e1) {
  // TODO Auto-generated catch block
  e1.printStackTrace();
}
      nextUserID++;
      setUserAcountPage();
    });
    Button surrender = new Button("Surrender");
    surrender.setFont(new Font("Copperplate", 20));
    surrender.setPrefSize(150, 40);
    surrender.setOnAction(e -> {
      userType = "SURRENDER";
      userID = nextUserID;
      try {
        add_User("", "", "", "", 0f, 0);
      } catch (SQLException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      nextUserID++;
      setUserAcountPage();
    });
    middleBox.getChildren().addAll(pick, adopter, surrender);

    start(pstage);
  }

  private void set_RemoveUserPage() {
    clearPage();

    leftBox = new VBox();
    upBox = new VBox();
    middleBox = new VBox();

    leftBox.setPrefWidth(300);
    upBox.setPrefHeight(350);

    // add back button to go back to menu page
    Button back = new Button("Back");
    back.setFont(new Font("Copperplate", 20));
    back.setPrefSize(150, 40);
    back.setOnAction(e -> {
      setMenuPage();
    });
    upBox.getChildren().add(back);


    GridPane gridPane = new GridPane();
    gridPane.setMinSize(620, 100);
    gridPane.setPadding(new Insets(10, 10, 10, 10));


    Text ID = new Text("User ID");
    ID.setFont(new Font("Copperplate", 20));
    TextField userID = new TextField();
    Button remove = new Button("Remove User");
    remove.setFont(new Font("Copperplate", 20));
    remove.setPrefSize(100, 4);

    gridPane.add(ID, 0, 0);
    gridPane.add(userID, 1, 0);
    gridPane.add(remove, 2, 2);

    middleBox.getChildren().add(gridPane);

  }

  private void setStatsPage() {
    clearPage();

    set_StatsPage_middleBox();
    set_PrelimSearch_Page_upBox();

    start(pstage);
  }

  private void set_StatsPage_middleBox() {
    middleBox = new VBox();
    leftBox = new VBox();
    leftBox.setPrefWidth(300);

    Text summary = new Text();
    try {
      summary.setText("\n\n\n" + storedProcedure_basicDebrief() + "\n\n\n");
    } catch (SQLException e) {
      e.printStackTrace();
    }
    summary.setFont(Font.font("Copperplate", 20));

    // add preliminary searchable examples
    try {
      // query for grouping animal
      String query = "select c.Specis_Name, count(*) " + "from Animal a, Classification c "
          + "where a.Classification_Breed_Name = c.Breed_Name " + "group by c.Specis_Name";
      middleBox.getChildren().add(getItemGraphStatistics(stmt.executeQuery(query)));

    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    middleBox.resize(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 3);
    middleBox.getChildren().add(summary);
  }

  private void set_PrelimSearch_Page_upBox() {
    upBox = new VBox();

    Button userpage = new Button("BACK");
    userpage.setFont(new Font("Copperplate", 20));
    userpage.setPrefSize(200, 40);

    Text tx = null;
    try {
      tx = new Text(storedProcedure_Prelim_Sort_userType());
      tx.setFont(Font.font("Copperplate", 20));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    userpage.setOnAction(ActionEvent -> {
      setMenuPage();
    });
    upBox.getChildren().addAll(userpage, tx);

  }

  /**
   * set up canvas
   */
  private void setSearchPage() {
    clearPage();

    // middleBox
    set_SearchPage_middleBox();


    leftBox = new VBox();
    rightBox = new VBox();
    upBox = new VBox();
    leftBox.setPrefWidth(250);
    rightBox.setPrefWidth(400);
    // upBox.setPrefHeight(300);

    set_SearchPage_upBox();

    start(pstage);

  }

  private void set_SearchPage_upBox() {
    upBox = new VBox();
    Button back = new Button("Back");
    back.setFont(new Font("Copperplate", 20));
    back.setPrefSize(150, 40);
    back.setOnAction(e -> {
      setMenuPage();

    });
    upBox.getChildren().add(back);
  }

  /**
   * set up the bottom box
   */
  private void set_SearchPage_middleBox() {
    middleBox = new VBox();


    // search animal attributes

    Text ID = new Text("Animal ID");
    ID.setFont(new Font("Copperplate", 20));
    TextField animalID = new TextField();

    Text breedNam = new Text("Breed");
    breedNam.setFont(new Font("Copperplate", 20));
    TextField breedName = new TextField();

    Text color = new Text("Color");
    color.setFont(new Font("Copperplate", 20));
    TextField baseColor = new TextField();

    Text type = new Text("Outcome Type");
    type.setFont(new Font("Copperplate", 20));
    TextField outcomeType = new TextField();

    Text sex = new Text("Sex");
    sex.setFont(new Font("Copperplate", 20));
    TextField gender = new TextField();

    Text num = new Text("Number");//
    num.setFont(new Font("Copperplate", 20));//
    TextField tempNum = new TextField();//


    Text loc = new Text("Location");
    loc.setFont(new Font("Copperplate", 20));
    TextField animalLoc = new TextField();

    Text group = new Text("Group by");
    group.setFont(new Font("Copperplate", 20));
    TextField groupby = new TextField();


    // textField
    // add button
    Button search = new Button("search");
    search.setFont(new Font("Copperplate", 20));
    search.setPrefSize(150, 40);
    search.setOnAction(e -> {
      // if ((groupby.getText() != null && !groupby.getText().isEmpty())) {
      setResultPage(breedName.getText(), baseColor.getText(), outcomeType.getText(),
          gender.getText(), animalLoc.getText(), groupby.getText());
      // }
    });

    // combine
    GridPane gridPane = new GridPane();
    gridPane.setMinSize(620, 100);
    gridPane.setPadding(new Insets(10, 10, 10, 10));
    gridPane.add(ID, 0, 0);
    gridPane.add(animalID, 1, 0);
    gridPane.add(loc, 2, 0);
    gridPane.add(animalLoc, 3, 0);
    gridPane.add(breedNam, 0, 1);
    gridPane.add(breedName, 1, 1);
    gridPane.add(color, 2, 1);
    gridPane.add(baseColor, 3, 1);
    gridPane.add(type, 0, 2);
    gridPane.add(outcomeType, 1, 2);
    gridPane.add(sex, 2, 2);
    gridPane.add(gender, 3, 2);
    gridPane.add(num, 0, 3);
    gridPane.add(tempNum, 1, 3);
    gridPane.add(group, 2, 3);
    gridPane.add(groupby, 3, 3);
    gridPane.add(search, 4, 3);

    VBox vb = new VBox();
    TableView<ObservableList<String>> tb = new TableView<ObservableList<String>>();

    ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

    try {
      ResultSet rs = storedProcedure_Prelim_Sort_menuType();

      for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
        // We are using non property style for making dynamic table
        final int j = i;
        TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
        col.setCellValueFactory(
            new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
              public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
                return new SimpleStringProperty(param.getValue().get(j).toString());
              }
            });

        tb.getColumns().addAll(col);
      }

      while (rs.next()) {
        // Iterate Row
        ObservableList<String> row = FXCollections.observableArrayList();

        row.add(rs.getInt(1) + "");

        for (int i = 2; i <= rs.getMetaData().getColumnCount(); i++) {
          // Iterate Column
          row.add(rs.getString(i));
        }

        data.add(row);

      }
    } catch (

    SQLException e1) {
      e1.printStackTrace();
    }

    tb.setItems(data);
    tb.setOnMouseClicked((MouseEvent e) -> {
      int index = tb.getSelectionModel().selectedIndexProperty().get();
      if (index >= 0) {
        row = tb.getItems().get(index);// .selectionModelProperty().
        setFullAnimalRecordPage(row);
      }
    });

    // tb.getColumns().addAll(animalID, animalName, sex, date, outcome, breed);

    vb.getChildren().addAll(tb);



    middleBox.getChildren().addAll(gridPane, vb);

  }

  /**
   * set up canvas
   * 
   * @param string4
   * @param string3
   * @param string2
   * @param string
   * 
   * @param rs
   * @param string5
   * @param string4
   * @param object
   * 
   * 
   * 
   *                setResultPage(breedName.getText(), baseColor.getText(), outcomeType.getText(),
   *                gender.getText(),GROUPBY string, animalLoc.getText());
   * 
   */
  private void setResultPage(String string, String string2, String string3, String string5,
      String string4, String groupby) {
    clearPage();
    set_ResultPage_LeftBox(string, string2, string3, string5, string4);
    set_ResultPage_RightBox(string, string2, string3, string5, string4, groupby);
    start(pstage);
  }

  private void set_ResultPage_RightBox(String string, String string2, String string3,
      String string5, String string4, String groupBy) {
    rightBox = new VBox();
    // right part is for pie chart
    rightBox.setPrefWidth(500);

    //
    // GridPane gridPane = new GridPane();
    // // Setting size for the pane
    // gridPane.setMinSize(450, 250);
    //
    // // Setting the padding
    // gridPane.setPadding(new Insets(10, 10, 10, 10));
    //
    // // Setting the vertical and horizontal gaps between the columns
    // gridPane.setVgap(5);
    // gridPane.setHgap(5);
    //
    // // Setting the Grid alignment
    // gridPane.setAlignment(Pos.TOP_CENTER);
    //
    //
    // Text group = new Text("Group by");
    // group.setFont(new Font("Copperplate", 20));
    // TextField groupby = new TextField();


    // textField
    // add button
    // Button submit = new Button("submit");
    // submit.setFont(new Font("Copperplate", 20));
    // submit.setPrefSize(150, 40);
    // submit.setOnAction(e -> {
    // if ((groupby != null && !groupby.getText().isEmpty())) {
    // groupByBY = groupby.getText();

    if (groupBy == null || groupBy.length() == 0) {
      groupBy = "Specis_Name";
    }
    ObservableList<PieChart.Data> pieChartData = null;
    try {
      pieChartData = pieChart_Animal_Search(string, string2, string3, string5, groupBy);
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    PieChart chart = new PieChart();
    for (int i = 0; i < pieChartData.size(); i++) {
      chart.getData().add(pieChartData.get(i));
    }


    chart.resize(250, 250);
    rightBox.getChildren().add(chart);



    // Arranging all the nodes in the grid
    // gridPane.add(group, 0, 0);
    // gridPane.add(groupby, 1, 0);
    // gridPane.add(submit, 2, 1);

    // TODO add pie chart



    // rightBox.getChildren().add(gridPane);
  }

  /**
   * set up the bottom box
   * 
   * @param rs
   */
  private void set_ResultPage_LeftBox(String string, String string2, String string3, String string5,
      String string4) {

    leftBox = new VBox();

    // left part is for table
    leftBox.setPrefWidth(700);
    // leftBox.setBackground(new Background(
    // (new BackgroundFill(Color.BURLYWOOD, new CornerRadii(100), new
    // Insets(10)))));
    // add Button to go back to the menu page
    Button menu = new Button("menu");
    menu.setFont(new Font("Copperplate", 20));
    menu.setPrefSize(100, 40);
    menu.setOnAction(e -> setMenuPage());

    // button for the next page with view in detail
    Button view = new Button("View All");
    view.setFont(new Font("Copperplate", 20));
    view.setPrefSize(150, 40);
    view.setOnAction(e -> setSearchPage());

    // create table for search result
    TableView<ObservableList<String>> table = new TableView();
    table.setPrefSize(600, 650);

    table.getOnScroll();

    ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

    try {
      ResultSet rs = animal_Search(string, string2, string3, string5, string4);

      for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
        // We are using non property style for making dynamic table
        final int j = i;
        TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
        col.setCellValueFactory(
            new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
              public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
                return new SimpleStringProperty(param.getValue().get(j).toString());
              }
            });

        table.getColumns().addAll(col);
      }

      while (rs.next()) {
        // Iterate Row
        ObservableList<String> row = FXCollections.observableArrayList();

        row.add(rs.getInt(1) + "");

        for (int i = 2; i <= rs.getMetaData().getColumnCount(); i++) {
          // Iterate Column
          row.add(rs.getString(i));
        }

        data.add(row);

      }


      table.setItems(data);
      table.setOnMouseClicked((MouseEvent e) -> {
        int index = table.getSelectionModel().selectedIndexProperty().get();
        if (index >= 0) {
          row = table.getItems().get(index);// .selectionModelProperty().
          setFullAnimalRecordPage(row);
        }
      });

    } catch (

    SQLException e1) {
      e1.printStackTrace();
    }

    //
    VBox vbox = new VBox();
    vbox.setSpacing(15);
    vbox.setPadding(new Insets(10, 10, 0, 10));
    vbox.getChildren().add(table);

    leftBox.getChildren().addAll(menu, view, vbox);

  }

  /**
   * set up canvas
   * 
   * @param list
   */
  private void setFullAnimalRecordPage(ObservableList<String> list) {
    clearPage();
    set_FullAnimalRecordPage_LeftBox();
    set_FullAnimalRecordPage_RightBox(list);
    set_FullAnimalRecordPage_MiddleBox(list);
    // set_FullAnimalRecordPage_BottomBox();
    start(pstage);
  }

  /**
   * set up the bottom box
   */
  private void set_FullAnimalRecordPage_LeftBox() {
    leftBox = new VBox();

    leftBox.setPrefWidth(200);
    // set right side's background color
    // leftBox.setBackground(new Background(
    // (new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new
    // Insets(10)))));
    // right side button of design
    Button viewAll = new Button("View All");
    viewAll.setPrefSize(150, 40);
    viewAll.setFont(new Font("Copperplate", 20));
    // set on action of view History button
    viewAll.setOnAction(E -> setSearchPage());


    // add buttons to the right box
    leftBox.getChildren().add(viewAll);

  }

  /**
   * set up the bottom box
   * 
   * @param list
   */
  private void set_FullAnimalRecordPage_RightBox(ObservableList<String> list) {
    rightBox = new VBox();

    rightBox.setPrefWidth(200);
    // set color
    // rightBox.setBackground(new Background(
    // (new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new
    // Insets(10)))));
    // Adopt information page
    Button adopt = new Button("Adopt");
    adopt.setFont(new Font("Copperplate", 20));
    adopt.setPrefSize(120, 40);
    adopt.setOnAction(e -> setAdoptInfoPage(list));
    // user account
    Button user = new Button("user info");
    user.setFont(new Font("Copperplate", 20));
    user.setPrefSize(150, 40);
    user.setOnAction(e -> setUserAcountPage());

    // add buttons to the right box
    rightBox.getChildren().addAll(adopt, user);
  }

  /**
   * set up the bottom box
   * 
   * @param list
   */
  private void set_FullAnimalRecordPage_MiddleBox(ObservableList<String> list) {
    middleBox = new VBox();


    // GET SPECIES NAME
    String species_Name = null;
    int index = 5;
    if (list.size() == 8) {
      index = 2;
    }
    try {
      String query = "Select Specis_Name " + "from Classification " + "where Breed_Name  ='"
          + list.get(index) + "';";
      ResultSet rs1 = stmt.executeQuery(query);
      if (rs1.next())
        species_Name = rs1.getString(1);

    } catch (SQLException e2) {
      e2.printStackTrace();
    }



    GridPane gp = new GridPane();
    ScrollPane sp = new ScrollPane();
    sp.setContent(gp);
    // set vertical scrolling
    sp.setHbarPolicy(ScrollBarPolicy.NEVER);
    sp.setVbarPolicy(ScrollBarPolicy.ALWAYS);

    // pet
    // StackPane stack = new StackPane();

    Image animal = new Image(getClass()
        .getResource(species_Name + "_" + Integer.parseInt(list.get(0)) % 6 + ".jpg").toString(),
        true);
    ImageView animalImage = new ImageView(animal);
    animalImage.setFitHeight(500);
    animalImage.setFitWidth(500);
    // stack.getChildren().add(animalImage);
    // stack.setPrefSize(300, 300);
    // animalImage.setFitHeight(400);



    Text tx = new Text();

    try {

      String record = "";
      for (int i = 0; i < list.size(); i++) {
        record += list.get(i) + "\n";
      }

      record += get_Animal_Temperament(Integer.parseInt(list.get(0))) + "\n";
      record += "\n";
      record += storedProcedure_search_move_Date_and_history(Integer.parseInt(list.get(0))) + "\n";
      record += "\n";

      tx.setText(record);
      tx.setFont(Font.font("Copperplate", 20));
    } catch (NumberFormatException e1) {

      e1.printStackTrace();
    } catch (SQLException e1) {

      e1.printStackTrace();
    }

    // Setting size for the pane
    gp.setMinSize(300, 600);

    // Setting the padding
    gp.setPadding(new Insets(10, 10, 10, 10));

    // Setting the vertical and horizontal gaps between the columns
    gp.setVgap(5);
    gp.setHgap(5);

    // Setting the Grid alignment
    gp.setAlignment(Pos.TOP_CENTER);

    // Arranging all the nodes in the grid

    gp.add(tx, 0, 1);
    // gp.add(previous, 0, 5);
    // gp.add(next, 1, 5);

    //
    VBox vbox = new VBox();
    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 0, 0, 10));
    vbox.getChildren().add(sp);
    // get connection to mysql and add each datum into vbox

    middleBox.getChildren().addAll(animalImage, vbox);

  }


  /**
   * set up the bottom box
   */
  private void set_FullUserInfoPage_MiddleBox() {
    middleBox = new VBox();

    GridPane gp = new GridPane();
    // set up table

    // Button userHistory = new Button("userHistory");
    Text userHistory = new Text("userHistory");
    userHistory.setFont(Font.font("Copperplate", 17));


    Text tx = new Text();

    String record = "";

    try {
      record = link_account();
      // TODO: pass in name and phone of the user
    } catch (SQLException e1) {

      e1.printStackTrace();
    } //

    tx.setText(record);

    // Setting size for the pane
    gp.setMinSize(500, 600);

    // Setting the padding
    gp.setPadding(new Insets(10, 10, 10, 10));

    // Setting the vertical and horizontal gaps between the columns
    gp.setVgap(5);
    gp.setHgap(10);

    // Setting the Grid alignment
    gp.setAlignment(Pos.TOP_CENTER);

    // Arranging all the nodes in the grid
    gp.add(userHistory, 0, 0);
    gp.add(tx, 1, 0);

    // Button surrenderHistory = new Button("surrender's History");
    // surrenderHistory.setOnAction(e -> {
    // TODO connect to surrender's history

    // });
    Text surrenderHistory = new Text("surrenderHistory");
    surrenderHistory.setFont(Font.font("Copperplate", 17));
    TableView surrenderer = new TableView();
    TableColumn surPhone = new TableColumn("Surrenderer's phone");
    surPhone.setPrefWidth(150);
    TableColumn surName = new TableColumn("Surrenderer's name");
    surName.setPrefWidth(150);
    surrenderer.getColumns().addAll(surPhone, surName);
    surrenderer.getOnScroll();
    surrenderer.setPrefSize(400, 400);
    // TODO load data into this table

    // Arranging all the nodes in the grid
    gp.add(surrenderHistory, 0, 1);
    gp.add(surrenderer, 1, 1);

    // add Button to go back to the menu page
    Button menu = new Button("menu");
    menu.setFont(new Font("Copperplate", 20));
    menu.setPrefSize(100, 40);
    menu.setOnAction(e -> setMenuPage());
    middleBox.getChildren().addAll(menu, gp);
  }

  /**
   * set up canvas
   */
  private void setUserAcountPage() {
    clearPage();
    set_UserAcountPage_middleBox();
    set_UserAcountPage_upBox();
    start(pstage);
  }

  /**
   * set up the bottom box
   */
  private void set_UserAcountPage_upBox() {
    upBox = new VBox();
    // add Button to go back to the menu page
    Button menu = new Button("menu");
    menu.setFont(new Font("Copperplate", 20));
    menu.setPrefSize(100, 40);
    menu.setOnAction(e -> setMenuPage());
    upBox.getChildren().add(menu);
  }


  private void set_UserAcountPage_middleBox() {
    middleBox = new VBox();
    
    try {
      ArrayList<String> info = full_user_info();
    } catch (SQLException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    // user could change his or her user name
    Text userName = new Text("userName");
    TextField name = new TextField();

    // user could change his or her phone number
    Text phone = new Text("phone");
    TextField phoneNumber = new TextField();

    // user could change his or her address
    Text address = new Text("address");
    TextField addr = new TextField();

    // TODO add string
    name.setPromptText("");
    // TODO add string
    phoneNumber.setPromptText("");
    // TODO add string
    addr.setPromptText("");


    // TODO get connect to the user's name phone and address


    // save data
    Button save = new Button("save");
    save.setOnAction(E -> {
      if ((name.getText() != null && !name.getText().isEmpty())
          && (phoneNumber.getText() != null && !phoneNumber.getText().isEmpty())
          && (addr.getText() != null && !addr.getText().isEmpty())) {


        // get input
        if (newUser) {

          try {
            alter_User(name.getText(), phoneNumber.getText(), addr.getText(), null, 0, 0);
          } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

          newUser = false;
        } else {
          try {
            alter_User(name.getText(), phoneNumber.getText(), addr.getText(), null, 0, 0);
          } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }

      }
    });

    ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();



    save.setFont(new Font("Copperplate", 20));
    save.setPrefSize(80, 40);

    // TableView tb = new TableView();
    // TableColumn surDonate = new TableColumn("donate pets history");
    // surDonate.setPrefWidth(300);
    // TableColumn surAdopt = new TableColumn("adopt pets history");
    // surAdopt.setPrefWidth(300);
    // tb.getColumns().addAll(surDonate, surAdopt);
    // tb.getOnScroll();
    // tb.setPrefSize(600, 600);
    // TODO: present history?
    String hoistory = null;
    try {
      hoistory = link_account();
    } catch (SQLException e) {

      e.printStackTrace();
    }

    Text ttext = new Text(hoistory);

    // TODO:add hoistory to text field

    // add these text and textfields to gridpane
    GridPane gridPane = new GridPane();
    // Setting size for the pane
    gridPane.setMinSize(450, 250);

    // Setting the padding
    gridPane.setPadding(new Insets(10, 10, 10, 10));

    // Setting the vertical and horizontal gaps between the columns
    gridPane.setVgap(5);
    gridPane.setHgap(5);

    // Setting the Grid alignment
    gridPane.setAlignment(Pos.TOP_CENTER);

    // Arranging all the nodes in the grid
    gridPane.add(userName, 0, 0);
    gridPane.add(name, 1, 0);
    gridPane.add(phone, 0, 1);
    gridPane.add(phoneNumber, 1, 1);
    gridPane.add(address, 0, 2);
    gridPane.add(addr, 1, 2);
    gridPane.add(ttext, 2, 0);
    gridPane.add(save, 5, 5);

    // adopter's two buttons
    if (userType.equals("ADOPTER")) {
      Text rating = new Text("self rating");
      TextField rate = new TextField();
      Text petNum = new Text("owning pets number");
      TextField pet = new TextField();

      // TODO add string
      rate.setPromptText("");
      pet.setPromptText("");

      // TODO get connect self rating and owing pets number


      gridPane.add(rating, 0, 3);
      gridPane.add(rate, 1, 3);
      gridPane.add(petNum, 0, 4);
      gridPane.add(pet, 1, 4);

    } else if (userType.equals("SURRENDER")) {
      Text note = new Text("note for the adopter");
      TextField noteAdopt = new TextField();


      // TODO add string
      noteAdopt.setPromptText("");

      // TODO get connect to the note

      gridPane.add(note, 0, 3);
      gridPane.add(noteAdopt, 1, 3);
    }

    middleBox.getChildren().addAll(gridPane);
  }

  /**
   * set up canvas
   * 
   * @param list
   */
  private void setAdoptInfoPage(ObservableList<String> list) {
    clearPage();
    rightBox = new VBox();
    leftBox = new VBox();
    rightBox.setPrefWidth(250);
    leftBox.setPrefWidth(250);

    setAdoptInfoPage_MiddleBox(list);

    start(pstage);
  }

  /**
   * set up the bottom box
   * 
   * @param list
   */
  private void setAdoptInfoPage_MiddleBox(ObservableList<String> list) {
    middleBox = new VBox();
    bottomBox = new VBox();
    TextField adopt = new TextField();
    // could not modify the found address
    adopt.setEditable(false);
    adopt.setPrefSize(200, 200);
    // load data into address to check if this animal is at Austin animal
    // shelter if true

    // set up owner address if not
    TextField ownerAddr = new TextField();
    ownerAddr.setEditable(false);
    ownerAddr.setPrefSize(200, 200);

    // add Button to go back to the menu page
    Button menu = new Button("menu");
    menu.setFont(new Font("Copperplate", 20));
    menu.setPrefSize(100, 40);
    menu.setOnAction(e -> setMenuPage());
    bottomBox.getChildren().add(menu);
    middleBox.getChildren().addAll(menu, adopt, ownerAddr);


    String result = "";
    String query = "Select Name,Phone,Address,Notes_and_Criteria_for_adoption "
        + "From All_Users inner join Surrender_Owner " + "Where User_ID =" + userID
        + " AND User_ID IN (Select Surrender_Owner_User_ID "
        + "from Gives_up where Animal_Animal_ID = " + list.get(0) + ");";
    try {
      ResultSet rs = stmt.executeQuery(query);
      if (rs.next()) {
        result += "Name: " + rs.getString(1);
        result += "\nPhone: " + rs.getString(2);
        result += "\nAddress: " + rs.getString(3);
        result += "\nNotes_and_Criteria_for_adoption: " + rs.getString(4);

      }

    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    if (result.length() == 0) {
      adopt.setText("Adoptable straight from Animal Shelter");
      adopt.setStyle("-fx-font: 20 Copperplate;");
    } else {
      ownerAddr.setText(result);
      ownerAddr.setStyle("-fx-font: 20 Copperplate;");
    }


  }

  /**
   * Get user at a coordinate and Get user name
   */
  private String photoAt(double x, double y) {
    for (Map.Entry<String, ArrayList<Double>> p : coordinate.entrySet()) {
      // get x coordinate
      double xc = p.getValue().get(0);
      // get y coordinate
      double yc = p.getValue().get(1);

      double diff = (x - xc) * (x - xc) + (y - yc) * (y - yc);
      // set scope of mouse click
      if (diff <= 50 * 50) {
        return p.getKey();
      }
    }
    return null;
  }

  /**
   * The class will be called by launch(args) in main method
   * 
   * @param primaryStage
   * @throws FileNoteFoundException
   */
  @Override
  public void start(Stage primaryStage) {
    Main.pstage = primaryStage;
    // save args
    args = this.getParameters().getRaw();

    // Main layout is Border Pane
    // (this includes top,left,center,right,bottom)
    BorderPane root = new BorderPane();

    // menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

    // setup start page
    if (start) {
      setBeginPage();
      start = false;
    }

    // add to pane
    root.setTop(upBox);
    root.setLeft(leftBox);
    root.setRight(rightBox);
    root.setCenter(middleBox);
    root.setBottom(bottomBox);

    root.setBackground(new Background(
        new BackgroundImage(new Image(getClass().getResource("bg.jpg").toString(), true),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            new BackgroundPosition(Side.RIGHT, 0, false, Side.BOTTOM, 0, false),
            new BackgroundSize(500, 334, false, false, false, false))));

    // set scene
    Scene mainScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    dia = new Stage();
    dia.initModality(Modality.APPLICATION_MODAL);
    dia.initOwner(primaryStage);
    dia.setScene(mainScene);
    dia.show();

  }

  /**
   * Main method
   * 
   * @param args
   */
  public static void main(String[] args) {
    launch(args); // start the GUI, calls start method
  }

}
