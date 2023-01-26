package drone;

import java.util.ArrayList;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

/**
 * @author Mustafa
 *  Class to handle a canvas, used by different GUIs
 */
public class MyCanvas {
	double xCanvasSize = 512;				// constants for relevant sizes, default values set
	double yCanvasSize = 512;
    GraphicsContext gc;
    public static Image droneOrange;
    public static Image droneAttacker;
    public static Image obstacle1;
    public static Image beams;
    public static Image mine;
    public static Image blast;
    public static ArrayList<Image> planet1Images;

    /**
     * Constructor sets up relevant Graphics context and size of canvas
     * @param g
     * @param cs
     */
    public MyCanvas(GraphicsContext g, double xcs, double ycs) {
    	gc = g;
    	xCanvasSize = xcs;
    	yCanvasSize = ycs;
    	loadImages();
    }
    
    /** Load images to be used while displaying
     * 
     */
    private void loadImages() {
    	//Load drone images
    	if (droneOrange == null) droneOrange = new Image(getClass().getResourceAsStream("/assets/ships/06/Spaceship_06_ORANGE.png"));
		if (droneAttacker == null) droneAttacker = new Image(getClass().getResourceAsStream("/assets/ships/02/Spaceship_02_RED.png"));
    	
		//Load planet images
    	if (planet1Images == null) {
			planet1Images = new ArrayList<Image>();
			for (int n = 1; n < 33; n++) {
				planet1Images.add(new Image(getClass().getResourceAsStream("/assets/planet/p1/planet ("+n+").png")));
			}
		}
    	
    	//Load obstacle images
    	if (obstacle1 == null) obstacle1 = new Image(getClass().getResourceAsStream("/assets/obstacles/asteroid.png"));
    	
    	//Load beams images
    	if (beams == null) beams = new Image(getClass().getResourceAsStream("/assets/beams.png"));
    	
    	//Load mine image
    	if (mine == null) mine = new Image(getClass().getResourceAsStream("/assets/mine.png"));
    	
    	//Load blast image
    	if (blast == null) blast = new Image(getClass().getResourceAsStream("/assets/blast.png"));
    }
    
    /**
     * @return canvas width
     */
    public double getXCanvasSize() {
    	return xCanvasSize;
    }
    /**
     * @return canvas height
     */
    public double getYCanvasSize() {
    	return yCanvasSize;
    }
    
    /** Setter for x and y size of the canvas
     * @param x size to be resized
     * @param y size to be resized
     */
    public void resize(double x, double y) {
    	xCanvasSize = x;
    	yCanvasSize = y;
    }
    
    /** Display blast at x,y
     * @param x
     * @param y
     */
    public void displayBlast(double x, double y, double rad) {
    	drawImage(blast, x, y, rad, rad);
    }
    
    /** Show border lines inside the canvas
     * 
     */
    public void showBorders() {
    	gc.setStroke(Color.RED);
        gc.strokeLine(xCanvasSize, 0, xCanvasSize, yCanvasSize);
        gc.strokeLine(0, yCanvasSize, xCanvasSize, yCanvasSize);
    }
    
    /** Fill the canvas
     * @param xs Start x
     * @param ys Start y
     * @param xe End x
     * @param ye End y
     */
    public void fill(double xs, double ys, double xe, double ye) {
    	gc.clearRect(xs, ys, xe, ye);
    }
    
    /** Draws a semi-transparent cone
     * @param x	centre x
     * @param y	centre y
     * @param rad	radius
     * @param dir	angle from the centre of the cone
     * @param angle	the angle range of the cone
     * @param c 	colour
     */
    public void drawCone(double x, double y, double rad, double dir, double angle, char c) {
    	gc.setGlobalAlpha(0.2);	//Opacity
    	gc.setFill(colFromChar(c));
    	gc.fillArc(x-rad/2, y-rad/2, rad, rad, -dir-(angle/2), angle, ArcType.ROUND);
    	gc.setGlobalAlpha(1);
    }
    
    /**
     * clears the canvas
     */
    public void clearCanvas() {
		gc.clearRect(0,  0,  xCanvasSize,  yCanvasSize);		// clear canvas
    }
    
	/**
     * drawImage ... draws object defined by given image at position and size
     * @param i		image
     * @param x		xposition	in range 0..1
     * @param y
     * @param sz	size
     */
	public void drawImage (Image i, double x, double y, double w, double h) {
			// to draw centred at x,y, give top left position and x,y size
			// sizes/position in range 0.. canvassize 
		gc.drawImage(i, x - w/2, y - h/2, w, h);
	}

    /**
     * Draw an image with a rotation transform applied in given angles.
     *
     * Since cannot rotate Image directly, set rotation on canvas, draw the image, and reset the canvas.
     *
     * @param image to be drawn.
     * @param rotation angle.
     * @param min x of the image.
     * @param min y of the image.
     * @param width of the image.
     * @param height of the image.
     */
    public void drawImageWithRotation(Image image, double ang, double minX, double minY, double w, double h) {
        gc.save();	//Save current transform to load later
        Rotate rot = new Rotate(ang, minX, minY);	//Define rotation according to the angle and the position
        gc.setTransform(rot.getMxx(), rot.getMyx(), rot.getMxy(), rot.getMyy(), rot.getTx(), rot.getTy());	//Apply the transform to the canvas
        drawImage(image, minX, minY, w, h);	//Draw the image as normal
        gc.restore(); //Reset the canvas by restoring the previously saved state.
    }
	
	/** 
	 * function to convert char c to actual colour used
	 * @param c
	 * @return Color
	 */
	Color colFromChar (char c){
		Color ans = Color.BLACK;
		switch (c) {
		case 'y' :	ans = Color.YELLOW;
					break;
		case 'w' :	ans = Color.WHITE;
					break;
		case 'r' :	ans = Color.RED;
					break;
		case 'g' :	ans = Color.GREEN;
					break;
		case 'b' :	ans = Color.BLUE;
					break;
		case 'o' :	ans = Color.ORANGE;
					break;
		}
		return ans;
	}
	
	/**
	 * set the fill colour to color c
	 * @param c
	 */
	public void setFillColour (Color c) {
		gc.setFill(c);
	}
	/**
	 * show the ball at position x,y , radius r in colour defined by col
	 * @param x
	 * @param y
	 * @param rad
	 * @param col
	 */
	public void showCircle(double x, double y, double rad, char col) {
	 	gc.setStroke(colFromChar(col));			// set the stroke colour
	 	showCircle(x, y, rad);						// show the circle
	}

	/**
	 * show the ball in the current colour at x,y size rad
	 * @param x
	 * @param y
	 * @param rad
	 */
	public void showCircle(double x, double y, double rad) {
		gc.strokeOval(x-rad, y-rad, rad*2, rad*2);	// draw circle
	}

	/**
	 * Show Text .. by writing string s at position x,y
	 * @param x
	 * @param y
	 * @param s
	 */
	public void showText (double x, double y, String s) {
		gc.setTextAlign(TextAlignment.CENTER);							// set horizontal alignment
		gc.setTextBaseline(VPos.CENTER);								// vertical
		gc.setFill(Color.WHITE);										// colour in white
		gc.fillText(s, x, y);											// print score as text
	}

	/**
	 * Show Int .. by writing int i at position x,y
	 * @param x
	 * @param y
	 * @param i
	 */
	public void showInt (double x, double y, int i) {
		showText (x, y, Integer.toString(i));
	}	
}

