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

import com.mysql.cj.xdevapi.Table;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
import javafx.scene.chart.PieChart;
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
	private void setBeginPage() {
		try {
			if (!start) {
				clearPage();
			}
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

	private void setMenuPage() {
		clearPage();

		set_MenuPage_upBox();
		setMenuPage_rightBox();
		setMenuPage_leftBox();

	}

	private void setMenuPage_rightBox() {
		Image dogCat = new Image(getClass().getResource("dog_cat.jpg").toString(), true);
		ImageView dogCatImage = new ImageView(dogCat);

		dogCatImage.setPickOnBounds(true); // allows click on transparent areas
		dogCatImage.setOnMouseClicked((MouseEvent e) -> setSearchPage());

		dogCatImage.setFitHeight(305);
		dogCatImage.setFitWidth(542);

		Text dc = new Text();
		dc.setText("Click the image and search for cats and dogs!");
		dc.setFont(Font.font("Verdana", 20));

		rightBox.getChildren().addAll(dogCatImage, dc);

	}

	private void setMenuPage_leftBox() {
		Image others = new Image(getClass().getResource("others.jpg").toString(), true);
		ImageView othersImage = new ImageView(others);

		othersImage.setFitHeight(300);
		othersImage.setFitWidth(722);

		othersImage.setPickOnBounds(true); // allows click on transparent areas
		othersImage.setOnMouseClicked((MouseEvent e) -> setSearchPage());

		Text ot = new Text();
		ot.setText("Click the image and search for other pets!");
		ot.setFont(Font.font("Verdana", 20));

		leftBox.getChildren().addAll(othersImage, ot);

	}

	private void set_MenuPage_upBox() {
		Button userpage = new Button("USER ACCOUNT");

		userpage.setOnAction(ActionEvent -> {
			setUserAcountPage();
		});
		upBox.getChildren().add(userpage);

	}

	/**
	 * set up canvas
	 */
	private void setSearchPage() {
		clearPage();
		// set canvas on action when mouse clicks
		canvas.setOnMouseClicked(e -> {
			gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

		});

		leftBox.setPrefWidth(400);
		rightBox.setPrefWidth(400);
		upBox.setPrefHeight(300);
		// middleBox
		set_SearchPage_middleBox();

//    // Step 2: Allocate a 'Statement' object in the Connection
//    Statement stmt;
//    try {
//      stmt = conn.createStatement();
//      // Step 3: Execute a SQL SELECT query. The query result is returned in a
//      // 'ResultSet' object.
//      String strSelect = "select title, price, qty from books";
//      System.out.println("The SQL statement is: " + strSelect + "\n"); // Echo For debugging
//
//      ResultSet rset = stmt.executeQuery(strSelect);
//
//      // Step 4: Process the ResultSet by scrolling the cursor forward via next().
//      // For each row, retrieve the contents of the cells with getXxx(columnName).
//      System.out.println("The records selected are:");
//      int rowCount = 0;
//      while (rset.next()) { // Move the cursor to the next row, return false if no more row
//        String title = rset.getString("title");
//        double price = rset.getDouble("price");
//        int qty = rset.getInt("qty");
//        System.out.println(title + ", " + price + ", " + qty);
//        ++rowCount;
//      }
//      System.out.println("Total number of records = " + rowCount);
//    } catch (SQLException e1) {
//      // TODO Auto-generated catch block
//      e1.printStackTrace();
//    }

	}

	/**
	 * set up the bottom box
	 */
	private void set_SearchPage_middleBox() {
		// set input for search
		TextField textField = new TextField();
		textField.setPrefSize(200, 10);
		// textField
		// add button
		Button search = new Button("search");
		search.setOnAction(e -> setResultPage());

		// combine
		GridPane gridPane = new GridPane();
		gridPane.setMinSize(500, 500);
		gridPane.setPadding(new Insets(10, 10, 10, 10));
		gridPane.add(textField, 0, 0);
		gridPane.add(search, 1, 0);

		middleBox.getChildren().add(gridPane);

	}

	/**
	 * set up canvas
	 */
	private void setResultPage() {
		clearPage();
		set_ResultPage_leftBox();
		set_ResultPage_rightBox();
	}

	/**
	 * set up the bottom box
	 */
	private void set_ResultPage_leftBox() {

		// left part is for table
		leftBox.setPrefWidth(500);
		leftBox.setBackground(
				new Background((new BackgroundFill(Color.BURLYWOOD, new CornerRadii(100), new Insets(10)))));
		// add Button to go back to the menu page
		Button menu = new Button("menu");
		menu.setOnAction(e -> setMenuPage());
		// create table for search result
		TableView table = new TableView();
		table.setPrefSize(100, 600);
		TableColumn Temperament = new TableColumn("Temperament");
		Temperament.setPrefWidth(100);
		TableColumn petName = new TableColumn("PetName");
		TableColumn Location = new TableColumn("Location");
		TableColumn animal = new TableColumn("Animal");
		table.getColumns().addAll(Temperament, petName, Location, animal);
		table.getOnScroll();

		//
		VBox vbox = new VBox();
		vbox.setSpacing(15);
		vbox.setPadding(new Insets(10, 10, 0, 10));
		vbox.getChildren().add(table);
		// TODO get connection to mysql and add each datum into vbox

		leftBox.getChildren().addAll(menu, vbox);

	}

	private void set_ResultPage_rightBox() {
		// right part is for pie chart
		rightBox.setPrefWidth(500);
		rightBox.setBackground(
				new Background((new BackgroundFill(Color.BURLYWOOD, new CornerRadii(100), new Insets(10)))));
		ObservableList<PieChart.Data> pieChartData = FXCollections
				.observableArrayList(new PieChart.Data("put searched value into this arrayList", 0));
		PieChart chart = new PieChart(pieChartData);

		// button for the next page with view in detail
		Button view = new Button("view");
		view.setOnAction(e -> setFullAnimalRecordPage());

		rightBox.getChildren().addAll(chart, view);
	}

	/**
	 * set up canvas
	 */
	private void setFullAnimalRecordPage() {
		clearPage();
		set_FullAnimalRecordPage_LeftBox();
		set_FullAnimalRecordPage_RightBox();
		set_FullAnimalRecordPage_MiddleBox();
		set_FullAnimalRecordPage_BottomBox();
	}

	/**
	 * set up the bottom box
	 */
	private void set_FullAnimalRecordPage_LeftBox() {
		leftBox.setPrefWidth(200);
		// set right side's background color
		leftBox.setBackground(
				new Background((new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new Insets(10)))));
		// right side button of design
		Button viewAll = new Button("View All");
		// set on action of view History button
		viewAll.setOnAction(E -> setResultPage());

		// add in date
		// not so sure if this should be a button
		Button date = new Button("Date");
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
		rightBox.setPrefWidth(200);
		// set color
		rightBox.setBackground(
				new Background((new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new Insets(10)))));
		// Adopt information page
		Button adopt = new Button("Adopt");
		adopt.setPrefSize(80, 40);
		adopt.setOnAction(e -> setAdoptInfoPage());
		// user account
		Button user = new Button("user info");
		user.setPrefSize(80, 40);
		user.setOnAction(e -> setFullUserInfoPage());

		// add buttons to the right box
		rightBox.getChildren().addAll(adopt, user);
	}

	/**
	 * set up the bottom box
	 */
	private void set_FullAnimalRecordPage_MiddleBox() {
		// set color
		middleBox.setBackground(
				new Background((new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new Insets(10)))));
		GridPane gp = new GridPane();

		// pet
		Button pet = new Button("pet");
		pet.setPrefSize(80, 40);
		pet.setOnAction(e -> {
			// connect to the pet's information
		});
		// set up table
		TableView table = new TableView();
		TableColumn adopters = new TableColumn("recent adopter");
		adopters.setPrefWidth(100);
		TableColumn breed = new TableColumn("breed");
		breed.setPrefWidth(100);
		table.getColumns().addAll(adopters, breed);
		table.getOnScroll();
		table.setPrefSize(400, 500);

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
		gp.add(table, 1, 1);

		//
		VBox vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 0, 0, 10));
		vbox.getChildren().add(gp);
		// TODO get connection to mysql and add each datum into vbox

		middleBox.getChildren().addAll(pet, vbox);

	}

	/**
	 * set up the bottom box
	 */
	private void set_FullAnimalRecordPage_BottomBox() {
		// set color
		bottomBox.setBackground(
				new Background((new BackgroundFill(Color.CORNFLOWERBLUE, new CornerRadii(500), new Insets(10)))));
		// button for previous page
		Button previous = new Button("prev");
		previous.setOnAction(e -> {
			// TODO connect to the previous page if not null

		});
		// button for next page
		Button next = new Button("next");
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
	}

	/**
	 * set up the bottom box
	 */
	private void set_FullUserInfoPage_MiddleBox() {
		GridPane gp = new GridPane();
		// set up table

		//Button userHistory = new Button("userHistory");
		Text userHistory = new Text("userHistory");
		userHistory.setFont(Font.font("Verdana", 17));
		//userHistory.setOnAction(e -> {
			// TODO self join of user's phone and name

		//});

		TableView userHis = new TableView();
		TableColumn phone = new TableColumn("Adopter's phone");
		phone.setPrefWidth(120);
		TableColumn name = new TableColumn("Adopter's name");
		name.setPrefWidth(120);
		userHis.getColumns().addAll(phone, name);
		userHis.getOnScroll();
		userHis.setPrefSize(400, 400);
		//TODO load data into this table
		
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
		gp.add(userHis, 1, 0);

		//Button surrenderHistory = new Button("surrender's History");
		//surrenderHistory.setOnAction(e -> {
			// TODO connect to surrender's history

		//});
		Text surrenderHistory = new Text("surrenderHistory");
		surrenderHistory.setFont(Font.font("Verdana", 17));
		TableView surrenderer = new TableView();
		TableColumn surPhone = new TableColumn("Surrenderer's phone");
		surPhone.setPrefWidth(150);
		TableColumn surName = new TableColumn("Surrenderer's name");
		surName.setPrefWidth(150);
		surrenderer.getColumns().addAll(surPhone, surName);
		surrenderer.getOnScroll();
		surrenderer.setPrefSize(400, 400);
		//TODO load data into this table
		
		// Arranging all the nodes in the grid
		gp.add(surrenderHistory, 0, 1);
		gp.add(surrenderer, 1, 1);

		// add Button to go back to the menu page
		Button menu = new Button("menu");
		menu.setOnAction(e -> setMenuPage());
		middleBox.getChildren().addAll(menu, gp);
	}

	/**
	 * set up canvas
	 */
	private void setUserAcountPage() {
		clearPage();
		set_UserAcountPage_UpBox();
		set_UserAcountPage_rightBox();
	}

	/**
	 * set up the bottom box
	 */
	private void set_UserAcountPage_rightBox() {
		// add Button to go back to the menu page
		Button menu = new Button("menu");
		menu.setOnAction(e -> setMenuPage());
		rightBox.getChildren().add(menu);
	}

	private void set_UserAcountPage_UpBox() {

		Label label = new Label("User Account");
		// TODO user could change his or her user name
		Text userName = new Text("userName");
		TextField name = new TextField();

		// TODO user could change his or her phone number
		Text phone = new Text("phone");
		TextField phoneNumber = new TextField();

		// TODO user could change his or her address
		Text address = new Text("address");
		TextField addr = new TextField();

		// TODO save data
		Button save = new Button("save");

		// add these text and textfields to gridpane
		GridPane gridPane = new GridPane();
		// Setting size for the pane
		gridPane.setMinSize(400, 200);

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
		gridPane.add(save, 4, 4);

		// TODO load surrender's note from data
		// if the user is the surrenderer then
		String ownerNote = null;

		TextField note = new TextField();
		note.setEditable(true);
		note.setText(ownerNote);
		note.setPrefSize(300, 300);

		upBox.getChildren().addAll(gridPane, note);
	}

	/**
	 * set up canvas
	 */
	private void setAdoptInfoPage() {
		clearPage();
		rightBox.setPrefWidth(200);
		leftBox.setPrefWidth(200);
		setAdoptInfoPage_MiddleBox();
	}

	/**
	 * set up the bottom box
	 */
	private void setAdoptInfoPage_MiddleBox() {
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
		// set color for graphic context
		gc.setFill(Color.BLUE);

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

		root.setBackground(new Background(new BackgroundImage(
				new Image(getClass().getResource("bg.jpg").toString(), true), BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, new BackgroundPosition(Side.RIGHT, 0, false, Side.BOTTOM, 0, false),
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
