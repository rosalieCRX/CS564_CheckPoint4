package application;

/**
 * Filename: Main.java Project: group17 Authors: Rosalie Cai, Ruiqi Hu
 */

import java.sql.*;
import java.awt.Label;
import java.awt.TextField;
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
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Social network visualizer of ateam 7
 * 
 * @author ateam7
 */
public class Main extends Application {
//see if the program just started
  private boolean start =true;
  // private GridPane userBut = new GridPane();
  // store current coordinates of nodes
  private Map<String, ArrayList<Double>> coordinate = new HashMap<String, ArrayList<Double>>();
  private List<String> args;
  private static final int WINDOW_WIDTH = 1200;// final value for pop-up window's width
  private static final int WINDOW_HEIGHT = 768;// final value for pop-up window's height

  static Stage pstage;
  static Stage dia;
  
  private static final int CANVAS_WIDTH = 600;// final value for canvas's width
  private static final int CANVAS_HEIGHT = 600;// final value for canvas's width

  Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);// Create a canvas
  GraphicsContext gc = canvas.getGraphicsContext2D();// create a graphic context for this class
  Alert alert = new Alert(AlertType.NONE);// a general alert for this class

  // set title of this project
  private static final String APP_TITLE = "Animal Shelter @ Austin  CS564-Group17";

  public static MenuBar menuBar;// general data manipulation
  public static VBox rightBox;// create vBox of right box
  public static VBox leftBox;// general data search
  public static VBox bottomBox;
  public static VBox middleBox;
  public static VBox upBox;

  Connection conn;


  // check userName
  private static String userType = "ADOPTER";

  // TODO: probably not needed
  /**
   * set up user based on their user type
   * 
   * @param type
   * @throws SQLException
   */
  private void setUpConnection(String type) throws SQLException {
    if (type.equals("ADOPTER") || type.equals("SURRENDER")) {
      conn = DriverManager.getConnection(
          "jdbc:mysql://127.0.0.1:3306/?user=user&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
          "user", "CS564-G17");
    } else if (type.equals("ADMIN")) {
      conn = DriverManager.getConnection(
          "jdbc:mysql://127.0.0.1:3306/?user=user&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
          "user", "CS564-G17");
    }


  }

  /**
   * prepare the stage for loading a new page
   */
  private void clearPage() {
    
    menuBar = new MenuBar();// general data manipulation
    rightBox = new VBox();// create vBox of right box
    leftBox = new VBox();// general data search
    bottomBox = new VBox();
    middleBox = new VBox();
    upBox = new VBox();
    dia.close();
    start(pstage);
  }

  /**
   * set up canvas
   */
  private void setCanvas() {
    // set canvas on action when mouse clicks
    canvas.setOnMouseClicked(e -> {
      gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

    });
  }

  /**
   * set up canvas
   */
  private void setBeginPage() {
    try {
      if(!start) {
      clearPage();}
      set_BeginPage_MiddleBox();
      setUpConnection(userType);
    } catch (FileNotFoundException | SQLException e1) {

      e1.printStackTrace();
    }

  }

  /**
   * set up the bottom box
   * 
   * @throws FileNotFoundException
   */
  private void set_BeginPage_MiddleBox() throws FileNotFoundException {
    middleBox = new VBox();
   
    Image title = new Image(getClass().getResource("Title.png").toString(), true);
    ImageView titleImage = new ImageView(title);
    middleBox.getChildren().add(titleImage);
    
    Image begin = new Image(getClass().getResource("begin.jpg").toString(), true);
    ImageView beginImage = new ImageView(begin);
    middleBox.getChildren().add(beginImage);
    

    // set a login button beside the userName
    Button adoptlogin = new Button("ADOPTER");
    Button surrenderlogin = new Button("SURRENDER");
    Button adminlogin = new Button("ADMIN");

    adoptlogin.setOnAction(ActionEvent -> {
      userType = "ADOPTER";
      setMenuPage();
    });

    surrenderlogin.setOnAction(ActionEvent -> {
      userType = "SURRENDER";
      setMenuPage();
    });

    adminlogin.setOnAction(ActionEvent -> {
      userType = "ADMIN";
      setMenuPage();
    });

    middleBox.getChildren().add(adoptlogin);
    middleBox.getChildren().add(surrenderlogin);
    middleBox.getChildren().add(adminlogin);
    middleBox.setAlignment(Pos.CENTER);
  }

  /**
   * set up canvas
   */
  private void setMenuPage() {
    clearPage();
    // set canvas on action when mouse clicks
    Button userAccount = new Button();
    userAccount.setOnAction(e -> setUserAcountPage());
    canvas.setOnMouseClicked(e -> {

      // move to next page with userName login
      setBeginPage();
      gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

    });
  }



  /**
   * set up canvas
   */
  private void setSearchPage() {
    // set canvas on action when mouse clicks
    canvas.setOnMouseClicked(e -> {
      gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

    });

    // Step 2: Allocate a 'Statement' object in the Connection
    Statement stmt;
    try {
      stmt = conn.createStatement();
      // Step 3: Execute a SQL SELECT query. The query result is returned in a
      // 'ResultSet' object.
      String strSelect = "select title, price, qty from books";
      System.out.println("The SQL statement is: " + strSelect + "\n"); // Echo For debugging

      ResultSet rset = stmt.executeQuery(strSelect);

      // Step 4: Process the ResultSet by scrolling the cursor forward via next().
      // For each row, retrieve the contents of the cells with getXxx(columnName).
      System.out.println("The records selected are:");
      int rowCount = 0;
      while (rset.next()) { // Move the cursor to the next row, return false if no more row
        String title = rset.getString("title");
        double price = rset.getDouble("price");
        int qty = rset.getInt("qty");
        System.out.println(title + ", " + price + ", " + qty);
        ++rowCount;
      }
      System.out.println("Total number of records = " + rowCount);
    } catch (SQLException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }



    set_SearchPage_BottomBox();
  }

  /**
   * set up the bottom box
   */
  private void set_SearchPage_BottomBox() {

    // table for result page
    TableView table = new TableView();
    TableColumn Temperament = new TableColumn("Temperament");
    TableColumn petName = new TableColumn("PetName");
    TableColumn Location = new TableColumn("Location");
    TableColumn animal = new TableColumn("Animal");
  }

  /**
   * set up canvas
   */
  private void setResultPage() {
    set_ResultPage_BottomBox();
  }

  /**
   * set up the bottom box
   */
  private void set_ResultPage_BottomBox() {
    // table.getColumns().addAll(Temperament, petName, Location, animal);
    VBox vbox = new VBox();
    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 0, 0, 10));

    // TODO connect to mysql and add each datum into vbox
  }

  /**
   * set up canvas
   */
  private void setFullAnimalRecordPage() {
    set_FullAnimalRecordPage_LeftBox();
    set_FullAnimalRecordPage_RightBox();
    set_FullAnimalRecordPage_MiddleBox();
    set_FullAnimalRecordPage_BottomBox();
  }

  /**
   * set up the bottom box
   */
  private void set_FullAnimalRecordPage_LeftBox() {
    // set right side's background color
    leftBox.setBackground(new Background(
        (new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new Insets(10)))));
    // right side button of design
    Button viewHistory = new Button("View History");
    // set on action of view History button
    viewHistory.setOnAction(E -> {
      Alert alert1 = new Alert(AlertType.NONE);
      // set alert type
      alert1.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
      // if (history.size() != 0)
      // alert1.setContentText(history.toString());
      alert1.showAndWait();
    });

    // add button to the right box
    leftBox.getChildren().add(viewHistory);

  }

  /**
   * set up the bottom box
   */
  private void set_FullAnimalRecordPage_RightBox() {
    // set color
    rightBox.setBackground(new Background(
        (new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new Insets(10)))));

  }

  /**
   * set up the bottom box
   */
  private void set_FullAnimalRecordPage_MiddleBox() {
    // set color
    rightBox.setBackground(new Background(
        (new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new Insets(10)))));

  }

  /**
   * set up the bottom box
   */
  private void set_FullAnimalRecordPage_BottomBox() {
    // set color
    rightBox.setBackground(new Background(
        (new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new Insets(10)))));

  }

  /**
   * set up canvas
   */
  private void setFullUserInfoPage() {
    Label label = new Label("User Information");
    set_FullUserInfoPage_BottomBox();
  }

  /**
   * set up the bottom box
   */
  private void set_FullUserInfoPage_BottomBox() {
    Button userHistory = new Button();
    userHistory.setOnAction(e -> {
      TableView t = new TableView();
      TableColumn adopt = new TableColumn();
      TableColumn history = new TableColumn();
      // table.getColumns().addAll(adopt, history);
      VBox vbox = new VBox();
      vbox.setSpacing(5);
      vbox.setPadding(new Insets(10, 0, 0, 10));
      // TODO self join and add to table
      // TODO adopt and total history and add to table
    });
  }

  /**
   * set up canvas
   */
  private void setUserAcountPage() {
    set_UserAcountPage_BottomBox();
    set_UserAcountPage_UpBox();
  }

  /**
   * set up the bottom box
   */
  private void set_UserAcountPage_BottomBox() {

  }

  /**
   * set up the bottom box
   */
  private void set_UserAcountPage_UpBox() {

  }

  /**
   * set up canvas
   */
  private void setAdoptInfoPage() {
    set_UserAcountPage_MiddleBox();
  }

  /**
   * set up the bottom box
   */
  private void set_UserAcountPage_MiddleBox() {
    TextField tf = new TextField();
    // could modify user's description
    tf.setEditable(true);
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
  public void start(Stage primaryStage){
    Main.pstage=primaryStage;
    // save args
    args = this.getParameters().getRaw();
    // set color for graphic context
    gc.setFill(Color.BLUE);

    // Main layout is Border Pane
    // (this includes top,left,center,right,bottom)
    BorderPane root = new BorderPane();

 //   menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

    // setup start page
    if(start) {
    setBeginPage();
    start = false;
    }
    
    // add to pane
    root.setTop(upBox);
    root.setLeft(leftBox);
    root.setRight(rightBox);
    root.setCenter(middleBox);
    root.setBottom(bottomBox);
    
   root.setBackground(new Background(new BackgroundImage(new Image(getClass().getResource("bg.jpg").toString(),true),
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, new BackgroundPosition(Side.RIGHT, 0, false, Side.BOTTOM, 0, false),
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
