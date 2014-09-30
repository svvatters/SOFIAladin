package cds.aladin;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import cds.aladin.Coord;
import cds.aladin.ViewSimple;
import cds.aladin.SOFIA_Aladin.Shapes;

/**
 * @author shannon.watters@gmail.com
 * @SOFIA-extension class
 */
public final class AltAzFov {

	Shapes shape;
	ViewSimple view;
	Graphics	 graphics;
	Coord coordCenter;	// Coord on celestial sphere view centered on
	double size;			// in degrees
	double scale;		// degrees/pixel

	/**
	 * @param vs
	 * @param g
	 * @param degrees
	 */
	AltAzFov(ViewSimple vs, Graphics g, double degrees) {
    		this(vs, g, degrees, Shapes.CIRCLE);
    }  
    
    /**
     * @param vs
     * @param g
     * @param size
     * @param shape
     */
    AltAzFov(ViewSimple vs, Graphics g, double size, Shapes shape) {
	    	// TODO handle bad input
	    	view = vs;
	    	graphics = g;
	    	coordCenter = view.getCooCentre();
	    	this.size = size;
	    	this.shape = shape;
	    	scale = view.getPixelSize();
    }
    
    /**
     * Draw the FOV outline centered on the view's center
     */
    void draw() {
	    Point viewCenter = view.getViewCoord(coordCenter.x, coordCenter.y);
	    draw(viewCenter);
	}

	/**
	 * Draw the FOV outline centered on a spot offset from the view's
	 * center by 'dist' and 'angle'
	 * @param dist
	 * @param angle
	 */
	void draw(double dist, double angle) {
			// TODO
		
			// TODO handle bad input
			Point center = view.getViewCoord(coordCenter.x, coordCenter.y);
			center.translate(
					(int)(-1.0*(dist/scale)*Math.sin(Math.toRadians(angle))), 
		  		  	(int)(-1.0*(dist/scale)*Math.cos(Math.toRadians(angle))) 
		);    	
	    float[] dashPattern = { 15, 8, 8, 8 };
	  	Graphics2D g2d = (Graphics2D)graphics;
	    g2d.setStroke(new BasicStroke(1, 	BasicStroke.CAP_BUTT,
	  		  							BasicStroke.JOIN_MITER, 10,
	  		  							dashPattern, 0));
	    draw(center);    
	    g2d.setStroke(new BasicStroke());      
	}

	/** 
	 * Draw the FOV outline centered on the input Point
	 * @param fovCenter
	 */
	void draw(Point fovCenter) {
        int diameter = (int) (size / scale);
    		int x = (int)(fovCenter.x - (diameter/2.0));
    		int y = (int)(fovCenter.y - (diameter/2.0));
        switch (shape) {
        	case CIRCLE:
        		graphics.drawOval(x, y, diameter, diameter);
        		break;
        	case SQUARE:
        		graphics.drawRect(x, y, diameter, diameter);
        		break;
        	default:
        		// TODO
        }
    }
}