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

  private void add_Animal(int Animal_ID, String Animal_Name, String Classification_Breed_Name,
      String Base_Color, String Outcome_type, String Sex, int Temp_Combination_Number,
      String Date_of_Birth) throws SQLException {
    // query for grouping animal
    String query =
        "INSERT INTO Animal (Animal_ID,Animal_Name,Classification_Breed_Name,Base_Color,  Outcome_type, Sex,  Temp_Combination_Number, Date_of_Birth) VALUES ('"
            + Animal_ID + "','" + Animal_Name + "','" + Classification_Breed_Name + "','"
            + Base_Color + "','" + Outcome_type + "','" + Sex + "','" + Temp_Combination_Number
            + "','" + Date_of_Birth + "');";
    stmt.executeQuery(query);
    nextAnimalID++;
  }

  private void remove_Animal(int ID) throws SQLException {
    // query for grouping animal
    String query = "delete from Animal " + "Where Animal_ID = " + ID;
    stmt.executeQuery(query);
  }

  private void add_User(String Name, String Phone, String Address,
      String Notes_and_Criteria_for_adoption, float Overall_Neighbor_Rating,
      int Total_Number_of_Pets_up_to_now) throws SQLException {

    String query = "insert into All_Users (User_ID,Name,Phone,Address) values(" + nextUserID + ",'"
        + Name + "','" + Phone + "','" + Address + "');";
    stmt.executeQuery(query);

    if (userType.equals("SURRENDER")) {
      String query1 = "insert into Surrender_Owner (User_ID,Overall_Neighbor_Rating) values("
          + nextUserID + ",'" + Notes_and_Criteria_for_adoption + "');";
      stmt.executeQuery(query1);

    } else if (userType.equals("ADOPTOR")) {
      String query2 =
          "insert into Potential_Adopter (A_User_ID,Overall_Neighbor_Rating,Total_Number_of_Pets_up_to_now) values("
              + nextUserID + "," + Overall_Neighbor_Rating + "," + Total_Number_of_Pets_up_to_now
              + ");";
      stmt.executeQuery(query2);
    }
    nextUserID++;
  }

  private void remove_User(int ID) throws SQLException {
    if (userType.equals("ADMIN")) {
      // query for grouping animal
      String query = "delete from All_Users " + "Where User_ID = " + ID;
      stmt.executeQuery(query);
    }
  }


  private String link_account(String Name, String Phone) throws SQLException {
    String query = "Select u1.UserID,u2.UserID " + "From All_Users u1, All_Users u2 "
        + "Where u1.Name = u2.Name AND NOT(u1.Phone = u2.Phone) AND strcmp(u1.Phone, u2.Phone)<0 AND u1.UserID in (select A_User_ID from Potential_Adopter);"
        + "Order by u1.UserID";
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
   * present results based on whehter the user selected dog_cat or others to start searching
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
    myCallStmt.getMoreResults();

    rs1 = myCallStmt.getResultSet();
    if (rs1.next())
      results +=
          "     The total number of users registered at our shelter is " + rs1.getInt(1) + "\n";
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


  /**
   * get result based on search conditions
   * 
   * @param stringAttributes
   * @return
   */
  private ResultSet animal_Search(String stringAttributes, int intAttribute) {
    // ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
    try {
      String query;
      if (menuType.equals("dog_cat")) {

        query = "SELECT " + stringAttributes + intAttribute + " FROM Animal "
            + " WHERE Classification_Breed_Name IN (select Breed_Name " + "From Classification "
            + "where Specis_Name = 'Dog' or Specis_Namee = 'Cat'));";
      } else {
        query = "SELECT " + stringAttributes + intAttribute + " FROM Animal "
            + " WHERE Classification_Breed_Name IN (select Breed_Name " + "From Classification"
            + "where NOT(Specis_Name = 'Dog' or Specis_Name = 'Cat'));";
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

    ResultSet rs = myCallStmt.executeQuery();
    if (rs.next())
      results = "Take_in_Date: " + rs.getString(1) + "\nIntake_Type: " + rs.getString(2)
          + "\nFound_Address" + rs.getString(3) + "\nl.Intake_Condition: " + rs.getString(4);

    return results;
  }



  /**
   * for observable list
   * 
   * @param attributes
   * @return
   */
  private ObservableList<PieChart.Data> pieChart_Animal_Search(String attributes) {
    ObservableList data = FXCollections.observableArrayList();
    try {
      String query;
      if (menuType.equals("dog_cat")) {

        query = "SELECT " + attributes + ", count(*)" + " FROM Animal "
            + " WHERE breed_Name IN (select breed_name " + "From Classification "
            + "where species_name = 'Dog' or species_name = 'Cat')) " + "Group by breed_Name;";
      } else {
        query = "SELECT " + attributes + ", count(*)" + " FROM Animal "
            + " WHERE breed_Name IN (select breed_name " + "From Classification"
            + "where NOT(species_name = 'Dog' or species_name = 'Cat')) " + "Group by breed_Name;";
      }

      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        // adding data on piechart data
        data.add(new PieChart.Data(rs.getString(1), rs.getInt(2)));
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
      // clear the page for setting up new pages if this is not the first time the program has
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
      getID();
    });

    adminlogin.setOnAction(ActionEvent -> {
      // set up the user type for future uses
      userType = "ADMIN";
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

  private void setMenuPage() {
    clearPage();

    set_MenuPage_MiddleBox();
    setMenuPage_upBox();


  }

  private void setMenuPage_upBox() {
    upBox = new VBox();

    Button back = new Button("Back");
    back.setFont(new Font("Copperplate", 20));
    back.setPrefSize(70, 40);
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


    start(pstage);
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
    search.setOnAction(e -> setPrelimSearchPage());
    statistic.setOnAction(e -> {
      // connect on statistic method
      setStatsPage();
    });

    userAccount.setOnAction(e -> setUserAcountPage());
    middleBox.getChildren().addAll(search, statistic, userAccount);
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
    back.setPrefSize(70, 40);
    back.setOnAction(e -> {
      setBeginPage();

    });
    upBox.getChildren().add(back);

    Text pick = new Text("Click the Button Below to be an Adopter or a Surrender");
    Button adopter = new Button("Adopter");
    adopter.setFont(new Font("Copperplate", 20));
    adopter.setPrefSize(70, 40);
    adopter.setOnAction(e -> {
      userType = "ADOPTER";
      setUserAcountPage();
    });
    Button surrender = new Button("Surrender");
    surrender.setFont(new Font("Copperplate", 20));
    surrender.setPrefSize(90, 40);
    surrender.setOnAction(e -> {
      userType = "SURRENDER";
      setUserAcountPage();
    });
    middleBox.getChildren().addAll(pick, adopter, surrender);

    start(pstage);
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

    Button userpage = new Button("USER ACCOUNT");
    userpage.setFont(new Font("Copperplate", 20));

    Text tx = null;
    try {
      tx = new Text(storedProcedure_Prelim_Sort_userType());
      tx.setFont(Font.font("Copperplate", 20));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    userpage.setOnAction(ActionEvent -> {
      setUserAcountPage();
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


    start(pstage);

  }

  /**
   * set up the bottom box
   */
  private void set_SearchPage_middleBox() {
    middleBox = new VBox();
    // set input for search
    TextField textField = new TextField();
    textField.setPrefSize(200, 10);
    // textField
    // add button
    Button search = new Button("search");
    search.setFont(new Font("Copperplate", 20));
    search.setOnAction(e -> setResultPage());

    // combine
    GridPane gridPane = new GridPane();
    gridPane.setMinSize(620, 100);
    gridPane.setPadding(new Insets(10, 10, 10, 10));
    gridPane.add(textField, 0, 0);
    gridPane.add(search, 1, 0);

    VBox vb = new VBox();
    TableView<ObservableList<String>> tb = new TableView<ObservableList<String>>();
    // TableColumn<ObservableList, String> animalID = new TableColumn("Animal ID");
    // animalID.setPrefWidth(100);
    // TableColumn<ObservableList, String> animalName = new TableColumn("Animal Name");
    // animalName.setPrefWidth(100);
    // TableColumn<ObservableList, String> sex = new TableColumn("Sex of the Animal");
    // sex.setPrefWidth(150);
    // TableColumn<ObservableList, String> date = new TableColumn("Date of Birth");
    // date.setPrefWidth(100);
    // TableColumn<ObservableList, String> outcome = new TableColumn("Outcome Type");
    // outcome.setPrefWidth(100);
    // TableColumn<ObservableList, String> breed = new TableColumn("Breed Name");
    // breed.setPrefWidth(100);


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

    tb.setItems(data);;
    tb.setOnMouseClicked((MouseEvent e) -> {
      int index = tb.getSelectionModel().selectedIndexProperty().get();
      row = tb.getItems().get(index);// .selectionModelProperty().
      setFullAnimalRecordPage(row);

    });

    // tb.getColumns().addAll(animalID, animalName, sex, date, outcome, breed);

    vb.getChildren().addAll(tb);



    // ------
    String Animal_attribute = null;
    String location = null;
    int Temperatment = 0;
    String breed = null;
    query = "Select * " + "From "
        + ((Temperatment == 0) ? "Animal a" : "(Animal a inner join Temperatment t)")
        + ((breed == null) ? "" : "inner join Classification c") + " " + "Where "
        + ((Temperatment == 0) ? "not (a.Animal_ID =0)"
            : "(a.Temp_Combination_Number=" + Temperatment + ")")
        + " and "
        + ((breed == null) ? "not (a.Animal_ID =0)"
            : "(a.Classification_Breed_Name=" + Temperatment + ")")
        + " AND "
        + ((location == null) ? "not (a.Animal_ID =0);"
            : "a.Animal_ID in (select At_Animal_Animal_ID from At where Location_Found_Address='"
                + location + "');");



    middleBox.getChildren().addAll(gridPane, vb);

  }

  /**
   * set up canvas
   */
  private void setResultPage() {
    clearPage();
    set_ResultPage_LeftBox();
    set_ResultPage_RightBox();
    start(pstage);
  }

  private void set_ResultPage_RightBox() {
    rightBox = new VBox();
    // right part is for pie chart
    rightBox.setPrefWidth(500);
    // rightBox.setBackground(new Background(
    // (new BackgroundFill(Color.BURLYWOOD, new CornerRadii(100), new Insets(10)))));
    ObservableList<PieChart.Data> pieChartData = pieChart_Animal_Search("attributes"); // TODO: get
                                                                                       // an input
                                                                                       // of
                                                                                       // attibute,
                                                                                       // seperated
                                                                                       // by comma
    PieChart chart = new PieChart(pieChartData);

    // button for the next page with view in detail
    Button view = new Button("view");
    view.setFont(new Font("Copperplate", 20));
    view.setOnAction(e -> setFullAnimalRecordPage(null));

    rightBox.getChildren().addAll(chart, view);
  }

  /**
   * set up the bottom box
   */
  private void set_ResultPage_LeftBox() {
    leftBox = new VBox();

    // left part is for table
    leftBox.setPrefWidth(500);
    // leftBox.setBackground(new Background(
    // (new BackgroundFill(Color.BURLYWOOD, new CornerRadii(100), new Insets(10)))));
    // add Button to go back to the menu page
    Button menu = new Button("menu");
    menu.setFont(new Font("Copperplate", 20));
    menu.setOnAction(e -> setMenuPage());
    // create table for search result
    TableView<ObservableList<String>> table = new TableView();
    table.setPrefSize(100, 600);
    // TableColumn Temperament = new TableColumn("Temperament");
    // Temperament.setPrefWidth(100);
    // TableColumn petName = new TableColumn("PetName");
    // TableColumn Location = new TableColumn("Location");
    // TableColumn animal = new TableColumn("Animal");
    // table.getColumns().addAll(Temperament, petName, Location, animal);
    table.getOnScroll();

    ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

    try {

      ResultSet rs = stmt.executeQuery(query);

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
    } catch (

    SQLException e1) {
      e1.printStackTrace();
    }

    //
    VBox vbox = new VBox();
    vbox.setSpacing(15);
    vbox.setPadding(new Insets(10, 10, 0, 10));
    vbox.getChildren().add(table);
    // TODO get connection to mysql and add each datum into vbox

    leftBox.getChildren().addAll(menu, vbox);

  }

  /**
   * set up canvas
   * 
   * @param list
   */
  private void setFullAnimalRecordPage(ObservableList<String> list) {
    clearPage();
    set_FullAnimalRecordPage_LeftBox();
    set_FullAnimalRecordPage_RightBox();
    set_FullAnimalRecordPage_MiddleBox(list);
    set_FullAnimalRecordPage_BottomBox();
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
    // (new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new Insets(10)))));
    // right side button of design
    Button viewAll = new Button("View All");
    viewAll.setPrefSize(80, 40);
    viewAll.setFont(new Font("Copperplate", 20));
    // set on action of view History button
    viewAll.setOnAction(E -> setResultPage());

    // add in date
    // not so sure if this should be a button
    Button date = new Button("Date");
    date.setFont(new Font("Copperplate", 20));
    date.setPrefSize(80, 40);
    date.setOnAction(e -> {
      // TODO connect to the result's add in date

    });

    // add buttons to the right box
    leftBox.getChildren().addAll(viewAll, date);

  }

  /**
   * set up the bottom box
   */
  private void set_FullAnimalRecordPage_RightBox() {
    rightBox = new VBox();

    rightBox.setPrefWidth(200);
    // set color
    // rightBox.setBackground(new Background(
    // (new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new Insets(10)))));
    // Adopt information page
    Button adopt = new Button("Adopt");
    adopt.setFont(new Font("Copperplate", 20));
    adopt.setPrefSize(80, 40);
    adopt.setOnAction(e -> setAdoptInfoPage());
    // user account
    Button user = new Button("user info");
    user.setFont(new Font("Copperplate", 20));
    user.setPrefSize(80, 40);
    user.setOnAction(e -> setFullUserInfoPage());

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
    // set color
    // middleBox.setBackground(new Background(
    // (new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new Insets(10)))));
    GridPane gp = new GridPane();
    ScrollPane sp = new ScrollPane();
    sp.setContent(gp);// TODO: set scrolling
    // pet
    Button pet = new Button("pet");
    pet.setFont(new Font("Copperplate", 20));
    pet.setPrefSize(80, 40);
    pet.setOnAction(e -> {
      // connect to the pet's information
    });

    Text tx = new Text();
    String record = "";
    for (int i = 0; i < list.size(); i++) {
      record += list.get(i);
    }
    tx.setText(record);


    // TableColumn adopters = new TableColumn("recent adopter");
    // adopters.setPrefWidth(100);
    // TableColumn breed = new TableColumn("breed");
    // breed.setPrefWidth(100);
    // table.getColumns().addAll(adopters, breed);


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
    gp.add(pet, 0, 1);
    gp.add(tx, 1, 1);

    //
    VBox vbox = new VBox();
    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 0, 0, 10));
    vbox.getChildren().add(sp);
    // TODO get connection to mysql and add each datum into vbox

    middleBox.getChildren().addAll(pet, vbox);

  }


  /**
   * set up the bottom box
   */
  private void set_FullAnimalRecordPage_BottomBox() {
    bottomBox = new VBox();
    // set color
    // bottomBox.setBackground(new Background(
    // (new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new Insets(10)))));
    // button for previous page
    Button previous = new Button("prev");
    previous.setFont(new Font("Copperplate", 20));
    previous.setPrefSize(80, 40);
    previous.setOnAction(e -> {
      // TODO connect to the previous page if not null

    });
    // button for next page
    Button next = new Button("next");
    next.setFont(new Font("Copperplate", 20));
    next.setPrefSize(80, 40);
    next.setOnAction(e -> {
      // TODO connect to the next page if not null

    });
    bottomBox.getChildren().addAll(previous, next);
  }

  /**
   * set up canvas
   */
  private void setFullUserInfoPage() {
    clearPage();
    set_FullUserInfoPage_MiddleBox();
    start(pstage);
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
    // userHistory.setOnAction(e -> {
    // TODO self join of user's phone and name

    // });

    Text tx = new Text();

    String record = "";

    try {
      record = link_account("", ""); // TODO: pass in name and phone of the user
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
    menu.setPrefSize(80, 40);
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
    menu.setPrefSize(80, 40);
    menu.setOnAction(e -> setMenuPage());
    upBox.getChildren().add(menu);
  }



  private void set_UserAcountPage_middleBox() {
    middleBox = new VBox();

    // TODO user could change his or her user name
    Text userName = new Text("userName");
    TextField name = new TextField();

    // user could change his or her phone number
    Text phone = new Text("phone");
    TextField phoneNumber = new TextField();

    // user could change his or her address
    Text address = new Text("address");
    TextField addr = new TextField();

    // save data
    Button save = new Button("save");
    save.setFont(new Font("Copperplate", 20));
    save.setPrefSize(80, 40);

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
    gridPane.add(save, 5, 5);

    // adopter's two buttons
    if (userType.equals("ADOPTER")) {
      Text rating = new Text("self rating");
      TextField rate = new TextField();
      Text petNum = new Text("owning pets number");
      TextField pet = new TextField();
      gridPane.add(rating, 0, 3);
      gridPane.add(rate, 1, 3);
      gridPane.add(petNum, 0, 4);
      gridPane.add(pet, 1, 4);


    } else if (userType.equals("SURRENDER")) {
      Text note = new Text("note for the adopter");
      TextField noteAdopt = new TextField();
      gridPane.add(note, 0, 3);
      gridPane.add(noteAdopt, 1, 3);
    }



    middleBox.getChildren().addAll(gridPane);
  }

  /**
   * set up canvas
   */
  private void setAdoptInfoPage() {
    clearPage();
    rightBox = new VBox();
    leftBox = new VBox();
    rightBox.setPrefWidth(200);
    leftBox.setPrefWidth(200);

    setAdoptInfoPage_MiddleBox();

    start(pstage);
  }

  /**
   * set up the bottom box
   */
  private void setAdoptInfoPage_MiddleBox() {
    middleBox = new VBox();
    bottomBox = new VBox();
    TextField address = new TextField();
    // could not modify the found address
    address.setEditable(false);
    address.setPrefSize(300, 300);
    // TODO load data into address to check if this animal is at Austin animal
    // shelter if true

    // TODO set up owner address if not
    TextField ownerAddr = new TextField();
    ownerAddr.setEditable(false);
    ownerAddr.setPrefSize(300, 300);

    // add Button to go back to the menu page
    Button menu = new Button("menu");
    menu.setFont(new Font("Copperplate", 20));
    menu.setPrefSize(80, 40);
    menu.setOnAction(e -> setMenuPage());
    bottomBox.getChildren().add(menu);
    middleBox.getChildren().addAll(menu, address, ownerAddr);
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
