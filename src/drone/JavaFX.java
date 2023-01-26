package drone;

import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Callable;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class JavaFX extends Application{

	private Scene scene;		//The main scene
	private Canvas canvas;		//Canvas to use to draw the contents of the drone arena
	private MyCanvas mc;		//MyCanvas class instance to control the canvas
	private AnimationTimer animation;	//The animation timer that controls the update every frame
	private boolean playing = true;		//Flag to store whether the animation is currently playing or not
	private VBox rPanel;				//Right hand side panel to display drone arena information
	private DroneArena arena;			//The drone arena being used
	private ArrayList<TimelineEntry> timeline = new ArrayList<TimelineEntry>();	//Time line to store the instances of the drone arena
	private BorderPane bp;											//Border pane to put the scene content inside
	private double maxCanvasWidth() {return bp == null ? 500 : bp.getWidth() - 311;}	//Maximum canvas width calculated by border pane width
	private double maxCanvasHeight() {return bp == null ? 500 : bp.getHeight() - 111;}	//Maximum canvas height calculated by border pane height
	private double canvasSizeX() {return (arena.getW() * scale());}		//Canvas width calculated by the scale and the width of the arena
	private double canvasSizeY() {return (arena.getH() * scale());}		//Canvas height calculated by the scale and the height of the arena
	private double scale() {									//Calculate the scale to be applied to the drone arena items when showing on the canvas
		double s = (maxCanvasWidth()/arena.getW()); 			//Calculate scale by fitting by width
		if (s * arena.getH() <= maxCanvasHeight()) return s; 	//Return the scale if it won't make the arena overflow vertically 
		s = (maxCanvasHeight()/arena.getH()); 					//Calculate scale by fitting by height
		if (s * arena.getW() <= maxCanvasWidth()) return s; 	//Return the scale if it won't make the arena overflow horizontally
		return 1;												//Return the scale of 1 by default to match the size of the arena
	}
	private Boolean updateScale = false;			//Flag to trigger an update on the current scale and recalculate it
	private Stage saveLoadFileWindow = new Stage();	//A separate window to show the JFileChooser dialog
	private enum FileOperation {SAVE, LOAD;};		//Enum to determine the type of file IO operation
	private FileOperation saveORload;				//To store the file operation type
    public static Slider speed = new Slider();		//Slider to set the speed of the Entities
    public static Slider zoom = new Slider();		//Slider to set the zoom
    private Text speedVal = new Text();				//To display the current speed value
    private Text zoomVal = new Text();				//To display the current zoom value
    private double tempZoom;						//To temporarily store the zoom value to detect a change
    private BorderPane canvasBorder;				//The border pane around the canvas
    private ScrollPane scrollCanvas;				//Scroll pane wrapping the canvas to use sliders when zoomed in
    private ArrayList<Image> bgImages = new ArrayList<Image>();	//To store the frames of the animated background image
    private Text rPanelText;						//Text to be displayed on the right hand panel
    private double tempBpSize = 0;					//Temporary value to detect a change in the border pane size
    private CheckBox showIDs;						//Check box to determine whether to show IDs or not
    private int frame = 0;							//Frame counter
    private int timelinePos = 0;					//Position in the time line
    private long tStart;							//Store the start time of the animation
    private long pausedAt; 							//Store the time the animation is paused at
    private int timelineInterval = 2;				//Amount of timeline saves in a second
    private int timelineCapacity = 100;				//Capacity of the timeline
    private double globalT;							//To track the time globally 
    private Image playPauseIcon;					//The icon for the play pause buuton
    private Image resetIcon;						//The icon for the reset buuton
    
	private static String extension = ".droneArena";	//File extension to save the DroneArena files with	 
	static JFileChooser dialog;		//The JFileChooser dialog
	
	//The filter for the dialog
	private FileFilter filter = new FileFilter() {
	
		@Override
		public boolean accept(File f) {
			return f.getAbsolutePath().endsWith(extension) || f.isDirectory();	//if is a directory or a file with the given extension
		}
	
		@Override
		public String getDescription() {
			return extension;
		}
		 
	};

	final long second = 1000000000; 	// 1 second
	public final static long FPS = 30;	//FPS rate to run the animation in
	int cycles = 0;						//Cycle count of the animation
		
	@Override
	public void start(Stage stage){
		stage.setTitle("Drone Simulator");	//Set widow title
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {	//When requested to close the program window
			@Override
			public void handle(WindowEvent arg0) {
				saveLoadFileWindow.close();		//Close the JFileChooser window if opened
				System.exit(0);		//Exit program
			}
		});
		
		loadBgImages();	//Load background images
		//Load icons
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/icon.png")));	//Window icon
		playPauseIcon = new Image(getClass().getResourceAsStream("/assets/playbutton.png"));	//Play pause button icon
		resetIcon = new Image(getClass().getResourceAsStream("/assets/reset.png"));				//Reset button icon
		
		Background bgFill = new Background(new BackgroundFill(new Color(0.08,0.05,0.35,1), CornerRadii.EMPTY, Insets.EMPTY));
		
		bp = new BorderPane();
		bp.setBackground(bgFill);
		scene = new Scene(bp, Screen.getPrimary().getBounds().getWidth()*0.8, Screen.getPrimary().getBounds().getHeight()*0.82); //Set the window size to ~80% of the screen size
		scene.getStylesheets().add("style.css");	//Apply style from the CSS file
		Group root = new Group();
		
		arena = new DroneArena(750, 460);		//Initial arena size
		
	    bp.setPadding(new Insets(5, 20, 10, 20));
	    bp.setTop(menus());
		canvas = new Canvas(canvasSizeX(), canvasSizeY());
		
		canvasBorder = new BorderPane(canvas);
		scrollCanvas = new ScrollPane(canvasBorder);
		BorderPane canvasOuterBorder = new BorderPane(scrollCanvas);
		BorderStroke[] innerBorders = {new BorderStroke(Color.INDIANRED, BorderStrokeStyle.SOLID, null, null)};		//Inner canvas border
		BorderStroke[] outerBorders = {new BorderStroke(Color.AQUA, BorderStrokeStyle.SOLID, null, null)};			//Outer canvas border
		canvasBorder.setBorder(new Border(innerBorders));
		canvasOuterBorder.setBorder(new Border(outerBorders));
		root.getChildren().add(canvasOuterBorder);
		bp.setLeft(root);
		mc = new MyCanvas(canvas.getGraphicsContext2D(), canvasSizeX(), canvasSizeY());
		
		//Set the right hand side panel to display the drone arena information
		rPanel = new VBox();
		rPanel.setAlignment(Pos.TOP_LEFT);
		rPanel.setPadding(new Insets(2, 5, 5, 2));
		rPanel.setPrefWidth(280);
		ScrollPane rScrollPanel = new ScrollPane(rPanel);
		rScrollPanel.setHbarPolicy(ScrollBarPolicy.NEVER);
 		bp.setRight(rScrollPanel);
 		
 		rPanelText = new Text();
 		rPanelText.setFont(Font.font("Verdana", FontWeight.BOLD, 10));		//Set info panel font
 		rPanelText.setFill(Color.GREENYELLOW);								//Set font colour
 		
 		bp.setBottom(toolbar());
 		
 		String defaultDir = System.getProperty("user.home") + System.getProperty("file.separator")+ "Desktop";	//The default directory to open in JFileChooser dialog
 		defaultDir += (new File(defaultDir + System.getProperty("file.separator")+ "Drone Arenas").isDirectory()) ? System.getProperty("file.separator") + "Drone Arenas" : "";	//If given folder exists, set it as default directory
		dialog = new JFileChooser(defaultDir);
		dialog.setFileFilter(filter);
		dialog.removeChoosableFileFilter(dialog.getAcceptAllFileFilter()); //Removes the "All Files" filter option
		dialog.addActionListener(DialogActions);		//Set the given action listener to deal with the actions made on the dialog (save/load/button clicks)
		
	    tStart = System.nanoTime();	//Initial time

		animation = new AnimationTimer() {	//Animation timer that runs every frame when playing

			@Override
			public void handle(long tNow) {
				//do {cycles--;} while(tNow <= tStart + (second/FPS)*cycles); //Catch up with the cycles if went back in timeline
				globalT = (tNow - tStart) / 1000000000.0;		//Calculate the seconds passed
				if (tNow >= tStart + (second/FPS)*cycles) {	//Check if the time passed since the last frame is more than the wanted amount to control the frame rate.
					//If the time period between the last frame and this frame is bigger than the time period wanted between frames, catch up by increasing the cycle count.
					//Happens when pausing or when the program is running slower then the given frame rate.
					do {cycles++;} while(tNow >= tStart + (second/FPS)*cycles); 
					onCanvasUpdate(globalT, cycles);		//Canvas update
					logicUpdate();					//Logic update
					draw();							//Draw contents
				}
			}
			
		};
		
		//Bind the scene width and height to the border pane's
		bp.prefHeightProperty().bind(scene.heightProperty());
        bp.prefWidthProperty().bind(scene.widthProperty());
		stage.setScene(scene);
		stage.show(); 
		
		//Try to load an example drone arena file from the start
		File f = new File ("default.droneArena");
		if (f.isFile()) {
			FileInputStream fIn;
			try {
				fIn = new FileInputStream(f);
		 	 	ObjectInputStream oIn = new ObjectInputStream(fIn);
		 	 	arena = (DroneArena) oIn.readObject();
		 	 	oIn.close();
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("Could not load the default arena file.");
			}
		}else System.out.println("Could not load the default arena file.");
		
		animation.start();
		
		////////////////////// Define Save Load File Window ///////////////////
		saveLoadFileWindow = new Stage();
		saveLoadFileWindow.setAlwaysOnTop(true);
		
		saveLoadFileWindow.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent arg0) {	//When the dialog is closed
				if (!playing) {//play if paused
					play();
					playing = true;
				}
			}
		});
		
		Group saveLoadRoot = new Group();

		SwingNode saveLoadDialog = new SwingNode();
 		saveLoadDialog.setContent(dialog);
		
 		saveLoadRoot.getChildren().add(saveLoadDialog);
 		
		saveLoadFileWindow.setScene(new Scene(saveLoadRoot));
		saveLoadFileWindow.setHeight(360);
		saveLoadFileWindow.setWidth(510);
		///////////////////////////////////////////////////////////////////////
		
	}
	
	/** Pause the animation
	 * 
	 */
	private void pause() {
		pausedAt = System.nanoTime();	//Mark the time paused
		animation.stop();
	}
	
	/** Play the animation
	 * 
	 */
	private void play() {
		if (pausedAt > tStart)	//If paused after the start time
			tStart += System.nanoTime() - pausedAt;	//Add the difference to start time
		else tStart = System.nanoTime();	//If the start time was reset after pausing, set start time to now
		animation.start();
	}
	
	private void resetTimer() {
		tStart =  System.nanoTime();	//Reset timer
		cycles = 0;						//Reset animation cycles
	}
	
	/**
	 * Generates the menu with the file, help and arena menu
	 * @return returns the menu generated to be added to the scene.
	 */
	private MenuBar menus() {
		MenuBar menus = new MenuBar();
		
		//File Menu
		Menu fileMenu = new Menu("File");
		
		//Exit option
		MenuItem optExit = new MenuItem("Exit");
		optExit.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
	        	pause();
		        System.exit(0);
		    }
		});
		
		//Save option
		MenuItem optSaveToFile = new MenuItem("Save");
	    optSaveToFile.setOnAction(new EventHandler<ActionEvent>() {
	    	@Override
	    	public void handle(ActionEvent event) {
				saveLoadFile(FileOperation.SAVE);
	    	}
	    });
	    
	    //Load option
	    MenuItem optLoadFromFile = new MenuItem("Load");
	    optLoadFromFile.setOnAction(new EventHandler<ActionEvent>() {
	    	@Override
	    	public void handle(ActionEvent event) {
				saveLoadFile(FileOperation.LOAD);
	    	}
	    });

		fileMenu.getItems().addAll(optSaveToFile, optLoadFromFile, optExit);	
		
		//Help menu
		Menu helpMenu = new Menu("Help");
		
		//About option
		MenuItem optAbout = new MenuItem("About");
		optAbout.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				showAbout();
			}
		});
		helpMenu.getItems().addAll(optAbout);
		
		//Arena menu
		Menu arenaMenu = new Menu("Arena");
		
		//New arena option
		MenuItem optNewArena = new MenuItem("New Arena");
		optNewArena.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				if (playing) { //pause if playing
					pause();
					playing = false;
				}
				showNewArena();
			}
		});
		arenaMenu.getItems().addAll(optNewArena);
		
		menus.getMenus().addAll(fileMenu, helpMenu, arenaMenu);
		
		return menus;
	}
	
	/** 
	 * 	Displays the About dialog.
	 */
	private void showAbout() {
		Alert info = new Alert(AlertType.INFORMATION);
		info.setTitle("About");
		info.setHeaderText(null);
		info.setContentText("Made by Mustafa Demir.\n\nGithub: mademir");
		info.showAndWait();
	}
	
	/**
	 * Shows the new arena dialog to get the new size values from the user
	 */
	private void showNewArena() {
		Dialog<ArrayList<Integer>> diag = new Dialog<ArrayList<Integer>>();
		diag.setTitle("New Arena Size");
		diag.setHeaderText(null);
		diag.setContentText("Please enter the new arena size.");
		
		TextField wInpField = new TextField(String.format("%d", arena.getW()));
		TextField hInpField = new TextField(String.format("%d", arena.getH()));
		
		GridPane grid = new GridPane();
		grid.add(new Label("Width (50-1000):"), 1, 1);
		grid.add(new Label("Height (50-1000):"), 2, 1);
		grid.add(wInpField, 1, 2);
		grid.add(hInpField, 2, 2);
		diag.getDialogPane().setContent(grid);
		
		ButtonType okBtn = new ButtonType("Okay", ButtonData.OK_DONE);
		ButtonType cancelBtn = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		diag.getDialogPane().getButtonTypes().addAll(okBtn, cancelBtn);
		diag.getDialogPane().lookupButton(okBtn).disableProperty().bind(Bindings.createBooleanBinding(	//Only enable the button when the input for new width and height values are valid
				new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						int w = -1;
						int h = -1;	//Default values are set to -1 so when the attempt to convert the string to integer fails as the input is non numeric, the condition will fail
						try {
							w = Integer.valueOf(wInpField.getText());
							h = Integer.valueOf(hInpField.getText());
						}catch(Exception e) {};
						return !(w >= 50 && w <= 1000 && h >= 50 && h <= 1000);	//Checks if the inputs are in the range
					}
			
		}, wInpField.textProperty(), hInpField.textProperty())); 		
		diag.setResultConverter(new Callback<ButtonType, ArrayList<Integer>>() {
		    @Override
		    public ArrayList<Integer> call(ButtonType btnType) {
		    	
		    	//Pass the values entered if submitted
		        if (btnType == okBtn) {
		        	ArrayList<Integer> size = new ArrayList<Integer>();
		        	size.add(0, Integer.valueOf(wInpField.getText()));
		        	size.add(1, Integer.valueOf(hInpField.getText()));
		            return new ArrayList<Integer>(size);
		        }
		 
		        return null;
		    }
		});
		
		Optional<ArrayList<Integer>> res = diag.showAndWait();
		if (!playing) {//play if paused
			play();
			playing = true;
		}
		if (res.isPresent()) {
			int w = res.get().get(0);
			int h = res.get().get(1);
			if (w >= 50 && w <= 1000 && h >= 50 && h <= 1000) {
				arena = new DroneArena(w, h);						//Create new arena from the given values
				//Reset the counters
				resetTimer();
				Explorer.explorerCt = 0;
				Attacker.attackerCt = 0;
				Obstacle.obstacleCt = 0;
				Planet.planetCt = 0;
				zoom.setValue(scale());				//Reset the scale for the new arena
				updateScale= true;
			}
			else System.out.println("Arena size is not in range!");		//Alert if arena size is too big
		}
	}
	
	/** Creates a toolbar to display the buttons, sliders and check boxes inside.
	 * @return The toolbar to be displayed.
	 */
	private HBox toolbar() {
		//The play/pause button to control the animation
	    Button btnPlayPause = new Button();
	    ImageView playBtnImage = new ImageView(playPauseIcon);
	    playBtnImage.setFitHeight(35);
	    playBtnImage.setFitWidth(35);
	    btnPlayPause.setGraphic(playBtnImage);
	    btnPlayPause.getStyleClass().add("iconButton");	//Load style for the button
	    btnPlayPause.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent event) {
	        	if (!playing) play();
	        	else pause();
	        	playing = !playing;
	       }
	    });
	    
	    //Adds an explorer drone on a random location
	    Button btnAddExplorer = new Button();
	    ImageView addExplorerBtnImage = new ImageView(MyCanvas.droneOrange);
	    addExplorerBtnImage.setFitHeight(35);
	    addExplorerBtnImage.setFitWidth(35);
	    btnAddExplorer.setGraphic(addExplorerBtnImage);
	    btnAddExplorer.getStyleClass().add("iconButton");	//Load style for the button
	    btnAddExplorer.setOnAction(new EventHandler<ActionEvent>() {
	    	@Override
	    	public void handle(ActionEvent event) {
	    		arena.addDrone(Drone.Types.EXPLORER);
				onCanvasUpdate(globalT, cycles); //Update canvas
				draw();
	    	}
	    });
	    
	  //Adds an attacker drone on a random location
	    Button btnAddAttacker = new Button();
	    ImageView addAttackerBtnImage = new ImageView(MyCanvas.droneAttacker);
	    addAttackerBtnImage.setFitHeight(35);
	    addAttackerBtnImage.setFitWidth(35);
	    btnAddAttacker.setGraphic(addAttackerBtnImage);
	    btnAddAttacker.getStyleClass().add("iconButton");	//Load style for the button
	    btnAddAttacker.setOnAction(new EventHandler<ActionEvent>() {
	    	@Override
	    	public void handle(ActionEvent event) {
	    		arena.addDrone(Drone.Types.ATTACKER);
	    		onCanvasUpdate(globalT, cycles); //Update canvas
	    		draw();
	    	}
	    });
	    
	    //Adds an obstacle on a random location
	    Button btnAddObstacle = new Button();
	    ImageView addObstacleBtnImage = new ImageView(MyCanvas.obstacle1);
	    addObstacleBtnImage.setFitHeight(35);
	    addObstacleBtnImage.setFitWidth(35);
	    btnAddObstacle.setGraphic(addObstacleBtnImage);
	    btnAddObstacle.getStyleClass().add("iconButton");	//Load style for the button
	    btnAddObstacle.setOnAction(new EventHandler<ActionEvent>() {
	    	@Override
	    	public void handle(ActionEvent event) {
	    		arena.addObstacle();
				onCanvasUpdate(globalT, cycles); //Update canvas
				draw();
	    	}
	    });
	    
	    //Adds planet on a random location
	    Button btnAddPlanet = new Button();
	    ImageView addPlanetBtnImage = new ImageView(MyCanvas.planet1Images.get(0));
	    addPlanetBtnImage.setFitHeight(35);
	    addPlanetBtnImage.setFitWidth(35);
	    btnAddPlanet.setGraphic(addPlanetBtnImage);
	    btnAddPlanet.getStyleClass().add("iconButton");	//Load style for the button
	    btnAddPlanet.setOnAction(new EventHandler<ActionEvent>() {
	    	@Override
	    	public void handle(ActionEvent event) {
	    		arena.addPlanet();
				onCanvasUpdate(globalT, cycles); //Update canvas
				draw();
	    	}
	    });
	    
	    //Adds mine on a random location
	    Button btnAddMine = new Button();
	    ImageView addMineBtnImage = new ImageView(MyCanvas.mine);
	    addMineBtnImage.setFitHeight(35);
	    addMineBtnImage.setFitWidth(35);
	    btnAddMine.setGraphic(addMineBtnImage);
	    btnAddMine.getStyleClass().add("iconButton");	//Load style for the button
	    btnAddMine.setOnAction(new EventHandler<ActionEvent>() {
	    	@Override
	    	public void handle(ActionEvent event) {
	    		arena.addMine();
	    		onCanvasUpdate(globalT, cycles); //Update canvas
	    		draw();
	    	}
	    });
	    
	    //Resets the arena and clears it
	    Button btnReset = new Button();
	    ImageView resetBtnImage = new ImageView(resetIcon);
	    resetBtnImage.setFitHeight(35);
	    resetBtnImage.setFitWidth(35);
	    btnReset.setGraphic(resetBtnImage);
	    btnReset.getStyleClass().add("iconButton");	//Load style for the button
	    btnReset.setOnAction(new EventHandler<ActionEvent>() {
	    	@Override
	    	public void handle(ActionEvent event) {
				arena = new DroneArena(arena.getW(), arena.getH());	//Make a new arena with the current size
				zoom.setValue(scale());
				//Reset counters
				Explorer.explorerCt = 0;
				Attacker.attackerCt = 0;
				Obstacle.obstacleCt = 0;
				Planet.planetCt = 0;
				resetTimer(); 		//Reset the timer
				speed.setValue(1);	//Reset speed
				onCanvasUpdate(0, cycles); //Update canvas
	    	}
	    });
	    
	    //Rewind button to go back in the timeline of the arena
	    Button btnBack2 = new Button("<2s");
	    btnBack2.setOnAction(new EventHandler<ActionEvent>() {
	    	@Override
	    	public void handle(ActionEvent event) {
	    		int sec = 2;
	    		int offset = sec * timelineInterval;
	    		try {
	    			if (timelinePos - offset >= 0 && globalT > sec) {
		    			timelinePos = timelinePos - offset;	//If data exists in the timeline pos requested, set the pos to that
						arena = (DroneArena) new ObjectInputStream(new ByteArrayInputStream(timeline.get(timelinePos).save)).readObject();	//Read the arena data from the byte array in the timeline
						tStart += sec * second;
						cycles -= sec * FPS;
						clearTimelineForward();
					}
				} catch (ClassNotFoundException | IOException e) {}
	    		onCanvasUpdate(globalT, cycles); //Update canvas
	    		draw();
	    	}
	    });
	    
	    //Rewind button to go back in the timeline of the arena
	    Button btnBack5 = new Button("<5s");
	    btnBack5.setOnAction(new EventHandler<ActionEvent>() {
	    	@Override
	    	public void handle(ActionEvent event) {
	    		int sec = 5;
	    		int offset = sec * timelineInterval;
	    		try {
	    			if (timelinePos - offset >= 0 && globalT > sec) {
		    			timelinePos = timelinePos - offset;	//If data exists in the timeline pos requested, set the pos to that
						arena = (DroneArena) new ObjectInputStream(new ByteArrayInputStream(timeline.get(timelinePos).save)).readObject();	//Read the arena data from the byte array in the timeline
						tStart += sec * second;
						cycles -= sec * FPS;
						clearTimelineForward();
					}
				} catch (ClassNotFoundException | IOException e) {}
	    		onCanvasUpdate(globalT, cycles); //Update canvas
	    		draw();
	    	}
	    });
	    
	    Font font = Font.font("Verdana", FontWeight.BOLD, 13);		//Font for the labels
	    

	    //Rewind Label
	    Text rewindLabel = new Text(" | Rewind:");
	    rewindLabel.setFill(new Color(0.9,0.9,0.2,1));
	    rewindLabel.setFont(font);
	    
	    //To display the speed value
	    Text speedLabel = new Text(" | Speed:");
	    speedLabel.setFill(new Color(0.9,0.9,0.2,1));
	    speedLabel.setFont(font);
	    speedLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
	    	@Override
	    	public void handle(MouseEvent event) {
				speed.setValue(1);	//Reset speed
	    	}
	    });
	    
	    //Slider for speed settings
	    speedVal.setFont(font);
	    speedVal.setFill(Color.WHITE);
	    speed.setMin(0.1);
	    speed.setMax(5);
	    speed.setValue(1);
	    speed.setShowTickLabels(true);
	    speed.setShowTickMarks(true);
	    speed.setMinorTickCount(1);
	    speed.setBlockIncrement(0.1);
	    
	    //To display the zoom value
	    Text zoomLabel = new Text(" | Zoom:");
	    zoomLabel.setFill(new Color(0.9,0.9,0.2,1));
	    zoomLabel.setFont(font);
	    zoomLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
	    	@Override
	    	public void handle(MouseEvent event) {
				zoom.setValue(scale());	//Reset zoom
	    	}
	    });
	    
	    //Slider for the zoom option
	    zoomVal.setFont(font);
	    zoomVal.setFill(Color.WHITE);
	    zoom.setMin(0);
	    zoom.setMax(10);
	    zoom.setValue(scale());
	    zoom.setShowTickLabels(true);
	    zoom.setShowTickMarks(true);
	    zoom.setMinorTickCount(1);
	    zoom.setBlockIncrement(1);
	    
	    //Show IDs check box
	    showIDs = new CheckBox();
	    showIDs.getStyleClass().add("showIDcheckbox");	//Load style for the check box
	    Text showIDsLabel = new Text("Show IDs");
	    showIDsLabel.setFont(font);
	    showIDsLabel.setFill(Color.WHITE);
	    
	    Text txtAdd = new Text("Add: ");
	    txtAdd.setFont(font);
	    txtAdd.setFill(new Color(0.9,0.9,0.2,1));
	    
	    HBox toolbarHB = new HBox(btnPlayPause, txtAdd, btnAddExplorer, btnAddAttacker, btnAddObstacle, btnAddPlanet, btnAddMine, new Label("\t"), btnReset, speedLabel, speed, speedVal, zoomLabel, zoom, zoomVal, showIDs, showIDsLabel, rewindLabel, btnBack2, btnBack5);
	    toolbarHB.getStyleClass().add("toolbar");	//Set the toolbar style
	    return toolbarHB;
	}
	
	ActionListener DialogActions = new ActionListener() {  //Save/Load Dialog Actions
		@Override
		public void actionPerformed(java.awt.event.ActionEvent event) {
			if (event.getActionCommand() == JFileChooser.APPROVE_SELECTION){
				File f = dialog.getSelectedFile();
				if (saveORload == FileOperation.SAVE) {
					if (!f.getAbsolutePath().endsWith(extension)) f = new File(f.getAbsolutePath() + extension); //Add the file extension if it is not there
					try {
					 	 FileOutputStream fOut = new FileOutputStream(f);
					 	 ObjectOutputStream oOut = new ObjectOutputStream(fOut);
					 	 oOut.writeObject(arena);
					 	 oOut.close();
					 	 fOut.close();
					} catch (IOException e) {
					 	 System.out.println("Error while saving to file!");
					 	 return;
					}
					System.out.println("Successfully saved to " + f.getAbsolutePath());
				}
				else if (saveORload == FileOperation.LOAD) {
					if (!f.getAbsolutePath().endsWith(extension)) {
						 System.out.println("Load cancelled! You must select a " + extension + " file.");
						 return;
					 }
					 try {
				 	 	 FileInputStream fIn = new FileInputStream(f);
			 	 	 	 ObjectInputStream oIn = new ObjectInputStream(fIn);
			 	 	 	 arena = (DroneArena) oIn.readObject();
			 	 	 	 oIn.close();
			 	 	 	 fIn.close();
				 	 } catch (IOException | ClassNotFoundException e) {
			 	 	 	 System.out.println("Error while loading from file!");
			 	 	 	 return;
				 	 }
					 System.out.println("Successfully loaded!");
				}
				Platform.runLater(new Runnable() { //To be able to close the window in JavaFX thread from this Swing thread

					@Override
					public void run() {
						saveLoadFileWindow.close();
						if (!playing && saveORload == FileOperation.SAVE) {//play if paused on save
							play();
							playing = true;
						}
						if (saveORload == FileOperation.LOAD) {
							zoom.setValue(scale());
							updateScale= true;
							arena.recalculateCts();
							resetTimer();
							onCanvasUpdate(0, cycles); //Update canvas
							draw();
						}
						
					}
				});
	        }
	        else if (event.getActionCommand() == JFileChooser.CANCEL_SELECTION){
	        	System.out.println((saveORload == FileOperation.SAVE ? "Save" : "Load") + " cancelled!");
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						saveLoadFileWindow.close();
						if (!playing) {//play if paused
							play();
							playing = true;
						}
					}
				});
	        }
		}
	};
	
	/** Set the dialog according to the given operation and show it
	 * @param operation: Type of the file operation 
	 */
	private void saveLoadFile(FileOperation operation) {
		if (playing) { //pause if playing
			pause();
			playing = false;
		}
		saveORload = operation;
		if (operation == FileOperation.SAVE) dialog.setDialogType(JFileChooser.SAVE_DIALOG);
		else dialog.setDialogType(JFileChooser.OPEN_DIALOG);
		saveLoadFileWindow.setTitle((saveORload == FileOperation.SAVE ? "Save to" : "Load from") + " File");
		saveLoadFileWindow.show();
	}
	
	/**Clears the timeline entries that are after the position
	 * 
	 */
	private void clearTimelineForward() {
		for (int i = timeline.size()-1; i > timelinePos; i--) timeline.remove(i);
	}
	
	/** Updates the canvas and info panel accordingly.
	 * @param t: Time in seconds
	 * @param cycles: cycle count for the animation
	 */
	private void onCanvasUpdate(double t, int cycles) {	//Runs each frame of canvas animation
		Entity.showIds = showIDs.isSelected();		//Set the showIDs flag according to the check box
		mc.fill(0, 0, canvas.getWidth(), canvas.getHeight());	//Reset he canvas
		drawBackground(cycles);	//Draw the background frame
		double scale = zoom.getValue();	//Get the zoom value from the slider
		if (arena.getW() * scale > 3900 || arena.getH() * scale > 3900 || arena.getW() * scale < 200 || arena.getH() * scale < 200) {	//Set min and set cap to the canvas size to the given values as javafx will not draw a canvas that big and throws and error
			scale = tempZoom;		//Set the zoom to the previous value
			zoom.setValue(scale);	//Put the slider position on this value
		}
		if (tempZoom != scale || updateScale) {	//if the zoom/scale value has changed
			canvas.setWidth(arena.getW() * scale);
			canvas.setHeight(arena.getH() * scale);
			mc.resize(arena.getW() * scale, arena.getH() * scale);
		}
		tempZoom = scale;
		updateScale = false;
		
		if ((arena.getW()*scale < canvas.getWidth()) || (arena.getH()*scale < canvas.getHeight())) mc.showBorders(); //if the arena is smaller than the canvas, show arena borders separately
		
		//Resize the scroll pane containing the canvas to the window size - padding
		if (tempBpSize != bp.getWidth() * bp.getHeight()) {
			scrollCanvas.setPrefWidth(bp.getWidth() - 307);
			scrollCanvas.setPrefHeight(bp.getHeight() - 130);
			zoom.setValue(scale());
			updateScale= true;
		}
		tempBpSize = bp.getWidth() * bp.getHeight();
		
		rPanel.getChildren().clear();
		rPanelText.setText(String.format("Time: %02d:%02d:%02d, FPS: %d", (int)t/60, (int)t%60, (int)(t*100)%100, FPS)+ "\n" + arena.toString());	//format and display the time and the arena info
		rPanel.getChildren().add(rPanelText);
		speedVal.setText(String.format("%.2f", speed.getValue()));	//Update the speed display
		zoomVal.setText(String.format("%.2fx", zoom.getValue()));	//Update the zoom display
	}
	
	/**The logic update that runs every frame.
	 * 
	 */
	private void logicUpdate() {
		frame++;
		if(frame % (JavaFX.FPS / timelineInterval) == 0) {	//every fixed interval
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out;
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(arena);				//Save the arena state in the time line
			} catch (IOException e) {
				System.out.println("Could not save to the timeline.");
			}   
			timeline.add(timelinePos++, new TimelineEntry(bos.toByteArray(), globalT));
			if (timeline.size() > timelineCapacity) {
				timeline.remove(0);			//If the saves are more than limit, start removing from the oldest save
				timelinePos--;
			}
		}
		arena.updateEntities(mc);		//Update all the entities
	}
	
	/** Draws the content of the arena on the canvas
	 * 
	 */
	private void draw() {
		arena.showEntites(mc);
	}

	/** Draws the background image frame and iterates and loops through the frames.
	 * @param cycles
	 */
	private void drawBackground(int cycles) {
		Image frame = bgImages.get(cycles % 60 / 4);	//Display the same frame for 4 cycles and iterate to the next. This is implemented this way to control the animation speed of the background
		mc.drawImage(frame, mc.xCanvasSize/2, mc.yCanvasSize/2, mc.xCanvasSize, mc.yCanvasSize);	//Draw the image
	}
	
	/** Load the background image frames to be displayed
	 * 
	 */
	private void loadBgImages() {
		for (int n = 1; n < 17; n++) {	//For each frame
			bgImages.add(new Image(getClass().getResourceAsStream("/assets/bg/bg ("+n+").jpeg")));
		}
	}
	
	public static void main(String[] args) {
		launch(); 
	}

}
