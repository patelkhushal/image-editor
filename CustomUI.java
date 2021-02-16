import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonListener;


public class CustomUI extends ButtonUI
{
	final Insets I = new Insets(4, 16, 4, 16);
	private Color backgroundColor;
	private Color brushColor;
	private Color fillColor;

	public CustomUI(Color backgroundColor, Color fillColor)
	{
		super();
		if (backgroundColor == null) this.backgroundColor = Color.LIGHT_GRAY;
		else 						 this.backgroundColor = backgroundColor;
		this.brushColor = backgroundColor;
	}
	
	public Color getBrushColor()
	{
		return brushColor;
	}

	public void setBrushColor(Color c)
	{
		this.brushColor = c;
	}

	public void installUI(JComponent c)
	{
		AbstractButton b = (AbstractButton)c;
		BasicButtonListener listener = new BasicButtonListener(b);
		b.addMouseListener(listener);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	public void uninstallUI(JComponent c)
	{
		AbstractButton b = (AbstractButton)c;
		BasicButtonListener listener = new BasicButtonListener(b);
		b.removeMouseListener(listener);
	}

	public Insets getDefaultMargin(AbstractButton ab)
	{
		return I;
	}

	public Insets getInsets(JComponent c)
	{
		return I;
	}

	public Dimension getMaximumSize(JComponent c)
	{
		return this.getPreferredSize(c);
	}

	public Dimension getMinimumSize(JComponent c)
	{
		return this.getPreferredSize(c);
	}

	public Dimension getPreferredSize(JComponent c)
	{
		Graphics g = c.getGraphics();
		FontMetrics fm = g.getFontMetrics();

		Dimension d = new Dimension();
		d.width = fm.stringWidth(((JButton)c).getText()) + I.left + I.right;
		d.height = fm.getHeight() + I.top + I.bottom;
		return d;
	}

	public void paint(Graphics g, JComponent c)
	{
		Graphics2D g2 = (Graphics2D)g;

		AbstractButton b = (AbstractButton)c;
		ButtonModel bm = b.getModel();

		final Color BLUE_LIGHT = new Color(107, 132, 181);
		final Color BLUE_DARK = new Color(32, 54, 94);

		Color lineColor;
		if (brushColor == Color.WHITE) lineColor = Color.BLACK;
		else						  lineColor = Color.DARK_GRAY;

		if (bm.isArmed())
		{
			fillColor = BLUE_LIGHT;
		}
		else if (bm.isPressed())
			fillColor = BLUE_DARK;
		else
			fillColor = brushColor;

			// use a round rectangle for drawing

		Dimension d = c.getPreferredSize();
		int x = d.width - 1;    // x-coordinate of right edge
		int y = d.height - 1;   // y-coordinate of bottom edge
		final int LW = 2;       // line width
		int arc = y;

		g2.setColor(backgroundColor);
		g2.fillRect(0, 0, x + 1, y + 1);

		g2.setColor(lineColor);
		g2.fillRoundRect(0, 0, x, y, arc, arc);

		g2.setColor(fillColor);
		g2.fillRoundRect(LW, LW, x - 2 * LW, y - 2 * LW, arc, arc);

	}

	/** Returns true if x,y is within the rounded rectangle representing
	 * this button.
	 */
	public boolean contains(JComponent c, int x, int y)
	{
		int w = c.getWidth();
		int h = c.getHeight();
		int arc = h;

		RoundRectangle2D.Double r =
				new RoundRectangle2D.Double(0, 0, w, h, arc, arc);

		return r.contains(x, y);
	}
}


