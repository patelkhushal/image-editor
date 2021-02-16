import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;


public class Brush
{
  // The brush:
	float	size;
	int		shape;
	int		join;
	Color	color;
	Stroke	stroke;
    
	public Brush()
	{
		super();
		this.size	= 1.0f;                                              
		this.shape	= BasicStroke.CAP_ROUND;                             
		this.color	= Color.DARK_GRAY;
		this.join	= BasicStroke.JOIN_ROUND;
		this.stroke	= new BasicStroke(size, shape, join);
	}
	

	public Brush(float size, int shape, Color color, Stroke stroke)
	{
		super();
		this.size	= size;
		this.shape	= shape;
		this.color	= color;
		this.stroke	= stroke;
	}
	
	public Stroke getStroke()
	{
		return this.stroke;
	}
	public void setStroke(Stroke stroke)
	{
		this.stroke = stroke;
	}
	
	public Color getColor()
	{
		return this.color;
	}
	public void setColor(Color color)
	{
		this.color = color;
	}
	
	public float getSize()
	{
		return size;
	}

	public void setSize(float size)
	{
		this.size = size;
		this.stroke	= new BasicStroke(this.size, this.shape, this.join);
	}
	
	public int getShape()
	{
		return shape;
	}

	public void setShape(int shape)
	{
		this.shape = shape;
		this.stroke	= new BasicStroke(this.size, this.shape, this.join);
	}

	public int getJoin()
	{
		return join;
	}



	public void setJoin(int join)
	{
		this.join = join;
		this.stroke	= new BasicStroke(this.size, this.shape, this.join);
	}
}
