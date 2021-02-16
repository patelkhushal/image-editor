
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;




public class NewFileDialog extends JDialog implements ActionListener, ItemListener
{

	// the following avoids a "warning" with Java 1.5.0 complier (?)
	static final long serialVersionUID = 1L;

	NewFileDialog mainWindow;
	JTextField tWidth, tHeight;
	DrawingAreaPanel c;
	///JPanel change to JTabbedPane
	JTabbedPane p;
	JScrollPane s;
	
	private void addComponent(Component c, JPanel p)
	{
		p.add(c);
	}
	
	///JPanel change to JTabbedPane
	void setVisible(JScrollPane s, JTabbedPane p, DrawingAreaPanel c)
	{
		super.setVisible(true);
		this.s = s;
		this.p = p;
		this.c = c;
	}
	NewFileDialog(){
		super();

		mainWindow = this;
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel northPanel	= new JPanel();
		JPanel centerPanel	= new JPanel();
		JPanel southPanel	= new JPanel();
		
		JLabel northMsg = new JLabel("This will remove the current drawing and will                \n");
		JLabel southMsg = new JLabel("create a blank canvas with the following dimensions:\n");
		
		northMsg.setForeground(Color.RED);
		southMsg.setForeground(Color.RED);
		
		Model.setComponentSize(northPanel, 364, 64);
		
		this.addComponent(northMsg, northPanel);
		this.addComponent(southMsg, northPanel);
		this.addComponent(new JLabel(), northPanel);
		
		centerPanel.setLayout(new GridLayout(2, 2, 0, 2));
		Model.setComponentSize(centerPanel, 160, 48);
		this.addComponent(new JLabel("Width"), centerPanel);
		tWidth = new JTextField("1024");
		this.addComponent(tWidth, centerPanel);
		this.addComponent(new JLabel("Height"), centerPanel);
		tHeight = new JTextField("768");
		this.addComponent(tHeight, centerPanel);
		
		this.addComponent(new JLabel("                       "
				+ "                                          "
				+ "                                          "
				+ "                      "), southPanel);
		JButton OK = new JButton("OK");
		OK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				int iWidth = Integer.parseInt(tWidth.getText());
				int iHeight = Integer.parseInt(tHeight.getText());
/// 			Model.setNewCanvasSize(c, p, s, iWidth, iHeight);
				Model.setNewCanvasSize(p, c, s, iWidth, iHeight);
				mainWindow.setVisible(false);
			}
		});
		this.addComponent(OK, southPanel);
//		
		
		KeyListener	kl = new KeyListener()
		{
			@Override
			public void keyPressed(KeyEvent event)
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void keyReleased(KeyEvent event)
			{
				switch (event.getKeyCode())
				{
				case KeyEvent.VK_ENTER:
				{
					int iWidth = Integer.parseInt(tWidth.getText());
					int iHeight = Integer.parseInt(tHeight.getText());
					/// 			Model.setNewCanvasSize(c, p, s, iWidth, iHeight);
					Model.setNewCanvasSize(p, c, s, iWidth, iHeight);
					mainWindow.setVisible(false);
				}
				default: {}
				}
			}
			@Override
			public void keyTyped(KeyEvent event)
			{
				// TODO Auto-generated method stub

			}
		};
		
		tWidth.addKeyListener(kl);
		tHeight.addKeyListener(kl);
		this.addKeyListener(kl);
		this.setFocusable(true);
		
		this.add(northPanel);
		this.add(centerPanel);
		this.add(southPanel);
		
		this.pack();
		Point location = MouseInfo.getPointerInfo().getLocation();
		this.setLocation(location);
		this.setSize(320, 200);
		this.setResizable(false);
	}
	@Override
	public void itemStateChanged(ItemEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
		
	}


}
