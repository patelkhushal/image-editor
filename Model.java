import java.awt.Component;
import java.awt.Dimension;
/// import 
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.image.BufferedImage;


public final class Model
{
	private Model() {}
	
	///	public static void setNewCanvasSize(DrawingAreaPanel c, JPanel p, JScrollPane s, int width, int height)
    /// switch the order and change to JTabbedPane
	public static void setNewCanvasSize(JTabbedPane p, DrawingAreaPanel c, JScrollPane s, int width, int height)
	{
		// BufferedImage img = c.getImage();
		// c.clear();
		c.setBounds(0, 0, width, height);
		// c.setImage(img);
		setComponentSize(p, width, height);
		s.revalidate();
	}
	
	public static void setComponentSize(Component c, int width, int height)
	{
		Dimension d = new Dimension(width, height);
		c.setMaximumSize(d);
		c.setMinimumSize(d);
		c.setPreferredSize(d);
	}

}
