package application;
/**
 * Filename: Main.java Project: group17 Authors: Rosalie Cai, Ruiqi Hu
 */


import java.sql.*;
import java.io.File;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
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

  // private GridPane userBut = new GridPane();
  // store current coordinates of nodes
  private Map<String, ArrayList<Double>> coordinate = new HashMap<String, ArrayList<Double>>();
  private List<String> args;
  private static final int WINDOW_WIDTH = 1024;// final value for pop-up window's width
  private static final int WINDOW_HEIGHT = 768;// final value for pop-up window's height

  private static final int CANVAS_WIDTH = 600;// final value for canvas's width
  private static final int CANVAS_HEIGHT = 600;// final value for canvas's width

  Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);// Create a canvas
  GraphicsContext gc = canvas.getGraphicsContext2D();// create a graphic context for this class
  Alert alert = new Alert(AlertType.NONE);// a general alert for this class

  // set title of this project
  private static final String APP_TITLE = "Social Network 007";

  public static MenuBar menuBar = new MenuBar();// general data manipulation
  public static VBox rightBox = new VBox();// create vBox of right box
  public static VBox leftBox = new VBox();// general data search

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
   * Get user at a coordinate and Get user name
   */
  private String userAt(double x, double y) {
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
   * set up the right box
   */
  private void setRightBox() {
    // set right side's background color
    rightBox.setBackground(new Background(
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
    rightBox.getChildren().add(viewHistory);
  }


  /**
   * The class will be called by launch(args) in main method
   * 
   * @param primaryStage
   * @throws FileNoteFoundException
   */
  @Override
  public void start(Stage primaryStage) throws FileNotFoundException {
    // save args
    args = this.getParameters().getRaw();
    // set color for graphic context
    gc.setFill(Color.BLUE);

    // Main layout is Border Pane
    // (this includes top,left,center,right,bottom)
    BorderPane root = new BorderPane();


    menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

    // set up right
    setRightBox();
    // VBox bottomBox = new VBox();
    // set up canvas
    setCanvas();

    // add to pane
    root.setTop(menuBar);
    root.setLeft(leftBox);
    root.setRight(rightBox);
    root.setCenter(canvas);

    // set scene
    Scene mainScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);


    final Stage dia = new Stage();
    dia.initModality(Modality.APPLICATION_MODAL);
    dia.initOwner(primaryStage);
    dia.setScene(new Scene(new GridPane(), 600, 200));
    dia.show();;

  }

  /**
   * Main method
   * 
   * @param args
   */
  public static void main(String[] args) {
    launch(args); // start the GUI, calls start method



    try (

        // Step 1: Allocate a database 'Connection' object
        Connection conn = DriverManager.getConnection(
            "jdbc:mysql://127.0.0.1:3306/?user=user&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
            "user", "CS564-G17");


        // Step 2: Allocate a 'Statement' object in the Connection
        Statement stmt = conn.createStatement();) {
      // Step 3: Execute a SQL SELECT query. The query result is returned in a 'ResultSet' object.
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

    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

}
