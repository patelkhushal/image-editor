import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.Vector;
import javax.swing.*;

public class InkSegment extends Line2D.Double
	{	
	  // the following avoids a "warning" with Java 1.5.0 complier (?)
		private static final long serialVersionUID = 1L;
		private Color	color;
		private Stroke	stroke;
		
		@SuppressWarnings("unused")
		public InkSegment()
		{
			super();
			this.stroke	= new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		}
		public InkSegment(double x1, double y1, double x2, double y2, Color c, Stroke s)
		{
			super(x1, y1, x2, y2);
			this.color = c;
			this.stroke = s;
		}
		@SuppressWarnings("unused")
		public InkSegment(Line2D.Double l, Color c, Stroke s)
		{
			super(l.getX1(), l.getY1(), l.getX2(), l.getY2());
			this.color = c;
			this.stroke = s;
		}
		
		public Stroke getStroke()
		{
			return this.stroke;
		}
		@SuppressWarnings("unused")
		public void setStroke(Stroke s)
		{
			this.stroke = s;
		}
		public Color getColor()
		{
			return this.color;
		}
		@SuppressWarnings("unused")
		public void setColor(Color c)
		{
			this.color = c;
		}
	}