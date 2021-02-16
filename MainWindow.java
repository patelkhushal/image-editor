import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.image.*;

import java.awt.BorderLayout;

import java.awt.Graphics2D;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//selection code from: https://www.daniweb.com/programming/software-development/threads/342036/how-to-select-images-by-using-a-rectangular-area

public class MainWindow extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
	// the following avoids a "warning" with Java 1.5.0 complier (?)
	private static final long serialVersionUID = 1L;

	final JPanel MAIN_PANEL = new JPanel();
	final Color BACKGOUND_COLOR = new Color(212, 212, 212);// Color.BLACK;
	// final Color BACKGOUND_COLOR = Color.BLACK;//Color.BLACK;

	JPanel toolboxPanel;
	final JScrollPane PAINTING = new JScrollPane(MAIN_PANEL);
	boolean sliders_created = false;

	final int TEXT = 0;
	final int LIGHTING = 1;
	final int CROP = 2;
	final int NOISE_REDUCTION = 3;

	final int BRUSH = 4;
	final int LINE = 5;
	final int PENCIL = 6;
	final int ERASER = 7;

	final int ROTATE_RIGHT = 8;
	final int ROTATE_LEFT = 9;

	int last_changed = 0;
	int resized_cropped = 0;
	float brightnessPercentage = 1.0f;
	int lastBrightnessValue = -1;
	int lastContrastValue = -1;

	JPanel extendedToolBar = new JPanel();
	JPanel inkColorPanel = new JPanel();
	final int MAX_POINTS = 1048576;
	/// set max canvas and revision
	final int MAX_CANVAS = 1024;
	final int MAX_REVISION = 1024;
	private int pointsCount;
	private Point[] points = new Point[MAX_POINTS];
	///
	private Vector<?>[][] canvasRev = new Vector<?>[MAX_CANVAS][MAX_REVISION];
	private int[] revcnt = new int[MAX_CANVAS];
	private int[] maxrevision = new int[MAX_CANVAS];
	int canvasIndex;
	int maxTab;
	double zoomval = 1.0;

	Brush brush = new Brush();
	Brush pencil = new Brush();
	Brush eraser = new Brush();
	int state;
	int contrast_value = 0;

	JPanel controlboxPanel;

	final int UNDO = 10;
	final int REDO = 11;
	final int ZOOMIN = 12;
	final int ZOOMOUT = 13;
	final int ADD = 14;
	final int RESET = 15;

	DrawingAreaPanel canvas[] = new DrawingAreaPanel[1024];
	int selCanvas = 0;

	JPanel colorPanel;

	// ImageIcon ic = new ImageIcon(getClass().getResource("tmp.jpg"));
	// DrawingAreaPanel canvas;
	JScrollPane scrollPane;
	JTabbedPane canvasPanel;
	BufferedImage orig;
	BufferedImage initial_copy;
	JToolBar toolBar;
	JPanel colorPlate, textTool;

	JButton inkColor, canvasColor, bPencil, bBrush, bEraser;
	NewFileDialog dNewFile;
	CustomUI customUI;

	JColorChooser cc;
	JFileChooser fc;

	// The Menu Items:
	JMenuItem newFile;
	JMenuItem openFile;
	JMenuItem save;
	JMenuItem close;
	JMenuItem saveFile;
	JMenuItem saveFileAs;
	JMenuItem exit;
	JMenuItem undo;
	JMenuItem redo;
	JMenuItem zoomIn;
	JMenuItem zoomOut;
	JMenuItem instruction;

	// The Colors:
	JButton black, white, dGray, gray, lGray, green, blue, cyan, magenta, orange, purple, red, yellow, brown, lblue,
			lorange;;

	MainWindow() {
		/// initilization
		for (int i = 0; i < MAX_CANVAS; i++) {
			canvasRev[i][0] = new Vector<InkSegment>();
			revcnt[i] = 0;
		}
		initGUI();
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	protected void updateInkColor(Color c) {
		brush.setColor(c);
		pencil.setColor(c);
		inkColor.setBackground(c);
		inkColorPanel.setBackground(c);
		customUI.setBrushColor(c);
	}

	protected void updateCanvasColor(Color c) {
		eraser.setColor(c);
		canvasColor.setBackground(c);
	}

	// Set a constant size for the component
	private JButton colorButton(final Color c) {
		JButton b = new JButton();
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateInkColor(c);
			}
		});
		Model.setComponentSize(b, 32, 32);
		b.setOpaque(true);
		b.setBorderPainted(false);
		b.setBackground(c);
		colorPlate.add(b);
		return b;
	}

	// deep copy method:
	// https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
	static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	private JPanel getRowPanel(JLabel label, JTextField textField) {
		JPanel rowPanel = new JPanel();
		rowPanel.add(label);
		rowPanel.add(textField);
		return rowPanel;
	}

	private JButton controlButton(final int state, String s) {
		JButton b = new JButton();
		ImageIcon ic = new ImageIcon(getClass().getResource(s));
		b.setIcon(ic);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (state == UNDO) {
					if (revcnt[selCanvas] > 0) {
						revcnt[selCanvas] -= 1;
						canvas[selCanvas].v = (Vector<InkSegment>) canvasRev[selCanvas][revcnt[selCanvas]];
						// System.out.println(revcnt[selCanvas]);
						canvas[selCanvas].setImage(null);
						canvas[selCanvas].repaint();
					}
				} else if (state == REDO) {
					if (revcnt[selCanvas] < maxrevision[selCanvas]) {
						revcnt[selCanvas] += 1;
						canvas[selCanvas].v = (Vector<InkSegment>) canvasRev[selCanvas][revcnt[selCanvas]];
						// System.out.println(revcnt[selCanvas]);
						canvas[selCanvas].setImage(null);
						canvas[selCanvas].repaint();
					}

				} else if (state == ZOOMIN) {
					zoomval += 0.1;
					canvas[selCanvas].zoomrate = zoomval;
					canvas[selCanvas].repaint();
				} else if (state == ZOOMOUT) {
					if (zoomval > 0) {
						zoomval -= 0.1;
						canvas[selCanvas].zoomrate = zoomval;
						canvas[selCanvas].repaint();
					}

				}
				else if(state == RESET){
					canvas[selCanvas].clear();
					canvas[selCanvas].setImage(MainWindow.deepCopy(initial_copy));
					orig = MainWindow.deepCopy(initial_copy);
					canvas[selCanvas].revalidate();
					canvas[selCanvas].repaint();
					validate();
				}
			}
		});
		controlboxPanel.add(b);
		return b;
	}

	private void setExtendedToolBarPanel(int state_button) {
		
		extendedToolBar.removeAll();
		extendedToolBar.revalidate();
		validate();
		this.repaint();
		extendedToolBar.setLayout(new BoxLayout(extendedToolBar, BoxLayout.Y_AXIS));
		if (state == TEXT) {
			extendedToolBar.setLayout(new GridLayout(2, 2));

			textTool = new JPanel();
			textTool.setLayout(new GridLayout(2, 2));
			textTool.add(stateButton(PENCIL, "pencil.png"));

			textTool.add(stateButton(BRUSH, "brush.png"));
			textTool.add(stateButton(LINE, "line.png"));
			textTool.add(stateButton(ERASER, "eraser.png"));

			extendedToolBar.add(textTool);

			extendedToolBar.add(colorPanel);
		} else if (state == LIGHTING) {

			sliders_created = true;
			JSlider jslider_brightness = new JSlider(JSlider.HORIZONTAL);
			JSlider jslider_contrast = new JSlider(JSlider.HORIZONTAL);
			JSlider jslider_saturation = new JSlider(JSlider.HORIZONTAL);
			JSlider jslider_warmth = new JSlider(JSlider.HORIZONTAL);

			JPanel brightness_panel = getJSliderPanel("Brightness", jslider_brightness);
			jslider_brightness.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent event) {
					int value = jslider_brightness.getValue();
					lastBrightnessValue = value;
					contrast_value = jslider_contrast.getValue() - 50;

					BufferedImage image = MainWindow.deepCopy(orig);
					if(last_changed == 4){
						last_changed = 0;
						return;
					}
					if (last_changed != 1 || last_changed == 0) {
						image = MainWindow.deepCopy(canvas[selCanvas].getImage());
					}
					last_changed = 1;
					brightnessPercentage = ((float) value / 100) + 0.5f;
					// int brightness = (int)(256 - 256 * percentage);

					BufferedImage shadedImage = new BufferedImage(image.getWidth(), image.getHeight(),
							BufferedImage.TYPE_INT_ARGB);
					shadedImage.getGraphics().drawImage(image, 0, 0, canvas[selCanvas]);

					RescaleOp rescaleOp = new RescaleOp(brightnessPercentage, contrast_value, null);
					rescaleOp.filter(shadedImage, shadedImage); // Source and destination are the same.

					canvas[selCanvas].setImage(shadedImage);
					canvas[selCanvas].revalidate();
					canvas[selCanvas].repaint();
					validate();
				}
			});
			if(lastBrightnessValue != -1){
				last_changed = 4;
				jslider_brightness.setValue(lastBrightnessValue);
			}
			extendedToolBar.add(brightness_panel);

			JPanel contrast_panel = getJSliderPanel("Contrast", jslider_contrast);
			jslider_contrast.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent event) {
					contrast_value = jslider_contrast.getValue() - 50;
					lastContrastValue = jslider_contrast.getValue();

					BufferedImage image = MainWindow.deepCopy(orig);
					if(last_changed == 4){
						last_changed = 0;
						return;
					}
					if (last_changed != 2 || last_changed == 0) {
						image = MainWindow.deepCopy(canvas[selCanvas].getImage());
					}
					last_changed = 2;

					BufferedImage shadedImage = new BufferedImage(image.getWidth(), image.getHeight(),
							BufferedImage.TYPE_INT_ARGB);
					shadedImage.getGraphics().drawImage(image, 0, 0, canvas[selCanvas]);

					RescaleOp rescaleOp = new RescaleOp(brightnessPercentage, contrast_value, null);
					rescaleOp.filter(shadedImage, shadedImage); // Source and destination are the same.

					canvas[selCanvas].setImage(shadedImage);

					canvas[selCanvas].revalidate();
					canvas[selCanvas].repaint();
					validate();

				}
			});
			if(lastContrastValue != -1){
				last_changed = 4;
				jslider_contrast.setValue(lastContrastValue);
			}
			extendedToolBar.add(contrast_panel);

			JPanel saturation_panel = getJSliderPanel("Saturation", jslider_saturation);
			extendedToolBar.add(saturation_panel);

			JPanel warmth_panel = getJSliderPanel("Warmth", jslider_warmth);
			extendedToolBar.add(warmth_panel);

			// JButton reset_button = new JButton("Reset");
			// reset_button.addActionListener(new ActionListener() {
			// 	public void actionPerformed(ActionEvent e) {
			// 		jslider_brightness.setValue(50);
			// 		jslider_contrast.setValue(50);
			// 		contrast_value = 15;
			// 		jslider_saturation.setValue(50);
			// 		jslider_warmth.setValue(50);
			// 		canvas[selCanvas].clear();
			// 		canvas[selCanvas].setImage(MainWindow.deepCopy(initial_copy));
			// 		orig = MainWindow.deepCopy(initial_copy);
			// 		canvas[selCanvas].revalidate();
			// 		canvas[selCanvas].repaint();
			// 		validate();
			// 	}
			// });
			// extendedToolBar.add(reset_button);

		} else if (state == CROP) {
			JPanel rotate_buttons = new JPanel();

			JButton rotate_right_button = new JButton();
			// rotate_right_button.setBackground(Color.BLACK);
			rotate_right_button.setOpaque(true);
			rotate_right_button.setBackground(new Color(212,212,212));
			rotate_right_button.setBorderPainted(false);
			ImageIcon ic = new ImageIcon(getClass().getResource("rotate_right.png"));
			rotate_right_button.setIcon(ic);
			extendedToolBar.add(rotate_right_button);

			JButton rotate_left_button = new JButton();
			rotate_left_button.setOpaque(true);
			rotate_left_button.setBackground(new Color(212,212,212));
			rotate_left_button.setBorderPainted(false);
			// rotate_left_button.setBackground(Color.BLACK);
			rotate_left_button.setOpaque(true);
			// rotate_right_button.setBorderPainted(false);
			ImageIcon ic2 = new ImageIcon(getClass().getResource("rotate_left.png"));
			rotate_left_button.setIcon(ic2);

			rotate_buttons.add(rotate_left_button);
			rotate_buttons.add(rotate_right_button);
			extendedToolBar.add(rotate_buttons);

			JLabel crop_height = new JLabel("Crop Height:");
			JLabel crop_width = new JLabel("Crop Width:");

			JTextField heightTextField = new JTextField(4);
			JTextField widthTextField = new JTextField(4);

			JTextField x_crop = new JTextField(4);
			JTextField y_crop = new JTextField(4);

			JLabel x_crop_label = new JLabel("x:");
			JLabel y_crop_label = new JLabel("y:");

			// create a new button
			JButton crop_button = new JButton("Crop!");
			crop_button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BufferedImage image = canvas[selCanvas].getImage();
					try {
						canvas[selCanvas].setImage(image.getSubimage(Integer.parseInt(x_crop.getText()),
								Integer.parseInt(y_crop.getText()), Integer.parseInt(widthTextField.getText()),
								Integer.parseInt(heightTextField.getText())));
					} catch (Exception exp) {
						if (orig == null) {
							JOptionPane.showMessageDialog(null, "No image set!");
						} else {
							JOptionPane.showMessageDialog(null,
									"Error: Invalid Parameters\n\nImage size is:\nHeight:"
											+ canvas[selCanvas].getImage().getHeight() + "\nWidth:"
											+ canvas[selCanvas].getImage().getWidth());
						}

					}

					canvas[selCanvas].revalidate();
					canvas[selCanvas].repaint();
					validate();
				}
			});

			extendedToolBar.add(new JSeparator());

			JPanel xy_panel = new JPanel();
			xy_panel.setLayout(new FlowLayout());
			xy_panel.add(getRowPanel(x_crop_label, x_crop));
			xy_panel.add(getRowPanel(y_crop_label, y_crop));
			extendedToolBar.add(xy_panel);
			extendedToolBar.add(getRowPanel(crop_height, heightTextField));
			extendedToolBar.add(getRowPanel(crop_width, widthTextField));

			JPanel crop_button_panel = new JPanel();
			crop_button_panel.add(crop_button, BorderLayout.CENTER);
			extendedToolBar.add(crop_button_panel);
			extendedToolBar.add(new JSeparator());

			JLabel resize_height = new JLabel("Resize Height:");
			JLabel resize_width = new JLabel("Resize Width:");

			JTextField resizeHeightTextField = new JTextField(4);
			JTextField resizeWidthTextField = new JTextField(4);
			JButton resize_button = new JButton("Resize!");
			resize_button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BufferedImage image = canvas[selCanvas].getImage();
					try {
						Image resized_image = image.getScaledInstance(Integer.parseInt(resizeWidthTextField.getText()),
								Integer.parseInt(resizeHeightTextField.getText()), Image.SCALE_DEFAULT);
						// Draw the image on to the buffered image
						BufferedImage bimage = new BufferedImage(resized_image.getWidth(null),
								resized_image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
						Graphics2D bGr = bimage.createGraphics();
						bGr.drawImage(resized_image, 0, 0, null);
						bGr.dispose();
						canvas[selCanvas].setImage(bimage);
						// Model.setNewCanvasSize(canvasPanel, canvas[0], scrollPane, bimage.getWidth(), bimage.getHeight());
						orig = MainWindow.deepCopy(bimage);
					} catch (Exception exp) {
						if (orig == null) {
							JOptionPane.showMessageDialog(null, "No image set!");
						} else {
							JOptionPane.showMessageDialog(null,
									"Error: Invalid Parameters\n\nImage size is:\nHeight:"
											+ canvas[selCanvas].getImage().getHeight() + "\nWidth:"
											+ canvas[selCanvas].getImage().getWidth());
						}
					}

					resized_cropped = 1;
					canvas[selCanvas].revalidate();
					canvas[selCanvas].repaint();
					validate();
				}
			});

			extendedToolBar.add(getRowPanel(resize_height, resizeHeightTextField));
			extendedToolBar.add(getRowPanel(resize_width, resizeWidthTextField));
			JPanel resize_button_panel = new JPanel();
			resize_button_panel.add(resize_button, BorderLayout.CENTER);
			extendedToolBar.add(resize_button_panel);
			// extendedToolBar.add(new JSeparator());

			// JButton reset_button = new JButton("Reset");
			// reset_button.addActionListener(new ActionListener() {
			// 	public void actionPerformed(ActionEvent e) {
			// 		canvas[selCanvas].setImage(MainWindow.deepCopy(initial_copy));
			// 		orig = MainWindow.deepCopy(initial_copy);
			// 		canvas[selCanvas].revalidate();
			// 		canvas[selCanvas].repaint();
			// 		validate();
			// 	}
			// });
			// extendedToolBar.add(reset_button);
		} else if (state == NOISE_REDUCTION) {

		}
		extendedToolBar.revalidate();
		validate();
		this.repaint();
		// return extendedJPanel;
	}

	private JPanel getJSliderPanel(String title, JSlider jslider) {
		JPanel slider_panel = new JPanel();
		// jslider = new JSlider(JSlider.HORIZONTAL);
		jslider.setMinorTickSpacing(5);
		jslider.setMajorTickSpacing(25);
		jslider.setPaintTicks(true);
		jslider.setPaintLabels(true);
		jslider.setBorder(BorderFactory.createTitledBorder(title));
		slider_panel.add(jslider);

		return slider_panel;
	}

	private JButton stateButton(final int state, String s) {
		JButton b = new JButton();
		b.setBackground(new Color(212,212,212));
		b.setOpaque(true);
		// b.setBounds(new Rectangle(40,40));
		b.setBorderPainted(false);
		ImageIcon ic = new ImageIcon(getClass().getResource(s));
		b.setIcon(ic);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setState(state);
				/// if state if add then add tab
				if (state == ADD) {
					ImageIcon addicon = new ImageIcon("paintpan.png");
					canvasPanel.addTab("Tab " + maxTab, addicon, newCanvas(maxTab), "Tab");
					maxTab++;
				}
				if (state == 0 || state == 1 || state == 2 || state == 3) {
					if(state == LIGHTING){

					}
					setExtendedToolBarPanel(state);
				}
				// toolBar.add(extendedToolBar, BorderLayout.WEST);
			}
		});

		toolboxPanel.add(b);
		return b;
	}

	private /* static */ void initGUI() {
		this.setFocusable(true);

		Dimension d = new Dimension(444, 664);
		this.setMinimumSize(d);
		this.setPreferredSize(d);

		this.setSize(1000, 500);
		this.setTitle("Image Editor");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Point location = MouseInfo.getPointerInfo().getLocation();
		this.setLocation(location);

		Container mainPanel = this.getContentPane();

		// canvas = new DrawingAreaPanel();
		cc = new JColorChooser();
		dNewFile = new NewFileDialog();

		// canvas.addMouseMotionListener(this);
		// canvas.addMouseListener(this);

		// setState(BRUSH);
		inkColor = new JButton();
		canvasColor = new JButton();

		customUI = new CustomUI(BACKGOUND_COLOR, brush.getColor());
		this.updateInkColor(Color.BLACK);
		brush.setSize(8.0f);

		this.updateCanvasColor(Color.WHITE);
		eraser.setSize(16.0f);

		colorPlate = new JPanel(new GridLayout(4, 4, 1, 1));
		colorPlate.setBackground(BACKGOUND_COLOR);
		colorPlate.setBounds(10, 86, 100, 150);

		inkColorPanel = new JPanel();
		inkColorPanel.setBackground(brush.getColor());
		inkColor.setBackground(brush.getColor());
		inkColor.setBounds(0, 0, 100, 100);
		inkColor.addActionListener(this);
		Model.setComponentSize(inkColor, 500, 125);
		inkColorPanel.add(inkColor);
		Model.setComponentSize(inkColorPanel, 150, 120);

		// inkColor.setUI(customUI);

		colorPanel = new JPanel();
		colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.PAGE_AXIS));
		colorPanel.setBorder(new TitledBorder(new EtchedBorder(), "Colors"));
		colorPanel.setBackground(BACKGOUND_COLOR);
		inkColor.setOpaque(true);
		inkColor.setBorderPainted(false);
		// inkColor.border
		colorPanel.add(inkColorPanel);
		colorPanel.add(new JSeparator());
		colorPanel.add(colorPlate);
		Model.setComponentSize(colorPanel, 80, 304);

		ImageIcon addicon = new ImageIcon("paintpan.png");
		canvasPanel = new JTabbedPane();
		// canvas.setLayout(new BorderLayout());
		// canvas.setBounds(0, 0, 1024, 768);

		// canvasPanel = new JPanel();
		// canvasPanel.setLayout(new BorderLayout());
		// canvasPanel.setBackground(BACKGOUND_COLOR);
		// canvasPanel.add(canvas, BorderLayout.CENTER);
		// Model.setComponentSize(canvasPanel, 100, 100);
		// canvasPanel.setBounds(0, 0, 100, 100);

		canvasPanel.addTab("Tab 0", addicon, newCanvas(maxTab), "Tab");
		maxTab++;
		canvasPanel.setMnemonicAt(0, KeyEvent.VK_1);

		canvasPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				selCanvas = canvasPanel.getSelectedIndex();
			}
		});

		scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		scrollPane.setBackground(BACKGOUND_COLOR);
		// System.out.println(canvasPanel.getSize());
		scrollPane.setViewportView(canvasPanel);

		scrollPane.setBounds(0, 0, 1, 1);
		Model.setComponentSize(scrollPane, 1, 1);

		// AS_NEEDED is a default
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JButton()); //some
		// "interesting" button can be put there

		scrollPane.revalidate();
		scrollPane.repaint(); // repaint as soon as possible
		scrollPane.setBorder(new TitledBorder(new EtchedBorder(), "Drawing Canvas"));

		// Create a JMenuBar and menu items:
		JMenuBar menuBar = new JMenuBar();
		// menuBar.setBackground(BACKGOUND_COLOR);

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);

		// JMenu fileMenu0 = new JMenu("View");
		// fileMenu0.setMnemonic(KeyEvent.VK_V);

		JMenu fileMenu1 = new JMenu("Edit");
		fileMenu1.setMnemonic(KeyEvent.VK_E);

		JMenu fileMenu2 = new JMenu("Help");
		fileMenu2.setMnemonic(KeyEvent.VK_H);

		newFile = new JMenuItem("New", new ImageIcon("new.gif"));
		newFile.setMnemonic(KeyEvent.VK_N);
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newFile.addActionListener(this);

		openFile = new JMenuItem("Open...", new ImageIcon("open.gif"));
		openFile.setMnemonic(KeyEvent.VK_O);
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openFile.addActionListener(this);

		exit = new JMenuItem("Exit", new ImageIcon("blank.gif"));
		exit.setMnemonic(KeyEvent.VK_X);
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		exit.addActionListener(this);

		save = new JMenuItem("Save As", new ImageIcon("blank.gif"));
		save.setMnemonic(KeyEvent.VK_S);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		save.addActionListener(this);

		/// Edit Menu
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		
		undo = new JMenuItem("Undo", new ImageIcon("undo.gif"));
		undo.setMnemonic(KeyEvent.VK_Z);
		undo.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		undo.addActionListener(this);
		
		redo = new JMenuItem("Redo", new ImageIcon("redo.gif"));
		redo.setMnemonic(KeyEvent.VK_Y);
		redo.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		redo.addActionListener(this);

		/// View Menu
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_V);

		zoomIn = new JMenuItem("Zoom In", new ImageIcon("zoomin.gif"));
		zoomIn.setMnemonic(KeyEvent.VK_EQUALS);
		zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,ActionEvent.CTRL_MASK));
		zoomIn.addActionListener(this);

		zoomOut = new JMenuItem("Zoom Out", new ImageIcon("zoomout.gif"));
		zoomOut.setMnemonic(KeyEvent.VK_MINUS);
		zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
		zoomOut.addActionListener(this);

		instruction = new JMenuItem("Instruction", new ImageIcon("blank.gif"));
		instruction.setMnemonic(KeyEvent.VK_F);
		instruction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		instruction.addActionListener(this);
		fileMenu2.add(instruction);

		menuBar.add(fileMenu);

		fileMenu.add(newFile);
		fileMenu.add(openFile);
		fileMenu.add(save);
		fileMenu.addSeparator();
		fileMenu.add(exit);
		// menuBar.add(fileMenu0);
		// fileMenu0.add(undo);
		// fileMenu0.add(redo);
		// menuBar.add(fileMenu1);
		/// add to zoomin zoomout to viewMnue menubar 
		menuBar.add(editMenu);
		editMenu.add(undo);
		editMenu.add(redo);
		menuBar.add(viewMenu);
		viewMenu.add(zoomIn);
		viewMenu.add(zoomOut);
		menuBar.add(fileMenu2);

		// Create a toolbar and set it to be vertical:
		toolBar = new JToolBar(JToolBar.VERTICAL);
		// toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.PAGE_AXIS));
		// toolBar.setLayout(new GridLayout(1, 2, 0, 0));
		toolBar.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		toolBar.setFloatable(false);
		// Model.setComponentSize(toolBar, 250, 500);

		// The Colors:
		black = colorButton(Color.BLACK);
		brown = colorButton(new Color(139, 69, 19));

		dGray = colorButton(Color.DARK_GRAY);
		orange = colorButton(Color.ORANGE);

		gray = colorButton(Color.GRAY);
		yellow = colorButton(Color.YELLOW);

		lGray = colorButton(Color.LIGHT_GRAY);
		green = colorButton(Color.GREEN);

		white = colorButton(Color.WHITE);
		cyan = colorButton(Color.CYAN);

		magenta = colorButton(Color.MAGENTA);
		blue = colorButton(Color.BLUE);

		red = colorButton(Color.RED);
		purple = colorButton(new Color(138, 43, 226));// Color.PINK);

		lblue = colorButton(new Color(109, 176, 232));
		lorange = colorButton(new Color(232, 168, 109));

		toolboxPanel = new JPanel(new GridLayout(0, 1, 0, 0));
		toolboxPanel.setBackground(BACKGOUND_COLOR);
		toolboxPanel.setBorder(new TitledBorder(new EtchedBorder(), "Toolbox"));
		/// initilize controlboxPanel and add ADD tab button to toolbox
		controlboxPanel = new JPanel();
		controlboxPanel.setSize(30, 20);
		Model.setComponentSize(toolboxPanel, 100, 280);

		extendedToolBar.setBounds(61, 11, 81, 140);
		extendedToolBar.setLayout(new BoxLayout(extendedToolBar, BoxLayout.Y_AXIS));
		extendedToolBar.setBorder(new TitledBorder(new EtchedBorder(), "Options"));

		stateButton(ADD, "add.png");
		stateButton(TEXT, "text.png");
		stateButton(LIGHTING, "lighting.png");
		stateButton(CROP, "crop.png");
		stateButton(NOISE_REDUCTION, "noise_reduction.png");
		// stateButton(0, "eraser.png");

		toolBar.setBackground(BACKGOUND_COLOR);
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 0;
		c.gridy = 0;
		toolBar.add(toolboxPanel, c);

		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 1;
		c.gridy = 0;
		c.weighty = 1.0;
		toolBar.add(extendedToolBar, c);
		Model.setComponentSize(extendedToolBar, 210, 500);

		/// add buttons to controlbutton
		controlButton(UNDO, "undo.png");
		controlButton(REDO, "redo.png");
		controlButton(ZOOMIN, "zoomin.png");
		controlButton(ZOOMOUT, "zoomout.png");
		controlButton(RESET, "reset_button.jpg");

		/// Dimension display and modify by editing numbers
		JTextField wdtf = new JTextField();
		wdtf.setSize(50, 70);

		wdtf.setLayout(null);
		wdtf.setText("1000");
		JLabel xLbl = new JLabel("X");
		xLbl.setSize(30, 70);
		JTextField httf = new JTextField();
		httf.setSize(30, 70);
		httf.setLayout(null);
		httf.setText("500");
		controlboxPanel.add(wdtf);
		controlboxPanel.add(xLbl);
		controlboxPanel.add(httf);
		wdtf.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					BufferedImage temp = canvas[selCanvas].getImage();
					int iWidth = Integer.parseInt(wdtf.getText());
					int iHeight = Integer.parseInt(httf.getText());
					// scrollPane.setSize(iWidth, iHeight);
					Model.setNewCanvasSize(canvasPanel, canvas[selCanvas], scrollPane, iWidth, iHeight);
					canvas[selCanvas].setImage(temp);
				}
			}
		});
		httf.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					int iWidth = Integer.parseInt(wdtf.getText());
					int iHeight = Integer.parseInt(httf.getText());
					// scrollPane.setSize(iWidth, iHeight);
					Model.setNewCanvasSize(canvasPanel, canvas[0], scrollPane, iWidth, iHeight);
				}
			}
		});
		/// dimension end

		/// newCanvas method to add new canvas

		this.setJMenuBar(menuBar);
		mainPanel.setBackground(BACKGOUND_COLOR);
		mainPanel.add(toolBar, BorderLayout.WEST);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(controlboxPanel, BorderLayout.AFTER_LAST_LINE);
		this.setVisible(true);
	}

	/// newCanvas method to add new canvas
	public JPanel newCanvas(int i) {
		canvas[i] = new DrawingAreaPanel();
		canvas[i].setLayout(new BorderLayout());
		canvas[i].setBounds(0, 0, 1024, 768);
		canvas[i].addMouseMotionListener(this);
		canvas[i].addMouseListener(this);

		return canvas[i];
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == newFile) {
			dNewFile.setVisible(scrollPane, canvasPanel, canvas[0]);
		}

		else if (source == openFile) {
			// show the file chooser 'open' dialog box and get user response
			fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File f = fc.getSelectedFile();
				try {
					BufferedImage image = ImageIO.read(f);
					Model.setNewCanvasSize(canvasPanel, canvas[selCanvas], scrollPane, image.getWidth(),
							image.getHeight());
					canvas[selCanvas].setImage(image);
					// canvasPanel.setBounds(0, 0, image.getWidth(), image.getHeight());
					Model.setNewCanvasSize(canvasPanel, canvas[0], scrollPane, image.getWidth(), image.getHeight());

					orig = image;
					initial_copy = image;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				scrollPane.revalidate();
				scrollPane.repaint(); // repaint as soon as possible
			}
		}

		else if (source == exit)
			System.exit(0);

		else if (source == save) {
			fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				// File f = fc.getSelectedFile();
				BufferedImage image = canvas[selCanvas].getImage();
				// Graphics2D g2d = image.createGraphics();
				// canvasPanel.printAll(g2d);

				try {
					File file = fc.getSelectedFile();
					ImageIO.write(image, "png", file);

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		} else if (source == inkColor) {
			cc.setColor(brush.getColor());
			ActionListener lOK = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					updateInkColor(cc.getColor());
				}
			};

			JColorChooser.createDialog(this, "Select the Ink Color", true, cc, lOK, null).setVisible(true);
		} else if (source == canvasColor) {
			Color c;
			c = JColorChooser.showDialog(inkColor, "Select the Background Color", brush.getColor());
			updateInkColor(c);
		}
		else if(source == zoomIn)
		{
			zoomval += 0.1;
			canvas[selCanvas].zoomrate = zoomval;
			canvas[selCanvas].repaint();
		}
		else if(source == zoomOut)
		{
			if (zoomval > 0) {
				zoomval -= 0.1;
				canvas[selCanvas].zoomrate = zoomval;
				canvas[selCanvas].repaint();
			}
		}
		/// add undo
		else if (source == undo) {
			if (revcnt[selCanvas] > 0) {
				revcnt[selCanvas] -= 1;
				canvas[selCanvas].v = (Vector<InkSegment>) canvasRev[selCanvas][revcnt[selCanvas]];
				// System.out.println(revcnt[selCanvas]);
				// canvas[selCanvas].setImage(null);
				canvas[selCanvas].repaint();
			}
			/// add redo
		} else if (source == redo) {
			if (revcnt[selCanvas] < maxrevision[selCanvas]) {
				revcnt[selCanvas] += 1;
				canvas[selCanvas].v = (Vector<InkSegment>) canvasRev[selCanvas][revcnt[selCanvas]];
				// System.out.println(revcnt[selCanvas]);
				// canvas[selCanvas].setImage(null);
				canvas[selCanvas].repaint();
			}
			/// add zoomIn
		}

		else if (source == instruction){
			JOptionPane.showMessageDialog(null, "Help\n\nMenu bar\n\nFile menu\n\nFile menu has two options: New and Open\n\nNew option would refresh the canvas and give a clear canvas for user to work on\n\nOpen option would prompt for user to select an image file.\n\n\n\nToolbar guide\n\nText button\n\nClick on the \u201CAa\u201D button from the toolbar. In the extended option panel, you will see brush, pencil, line and eraser buttons. It will also have color plate to choose different colors.\n\nLighting button\n\nClick on the lighting button, the second vertical button on toolbar to open up sliders for brightness, contrast, saturation and warmth. You can increase and decrease the value for brightness and contrast as per your needs.\n\nResize button\n\nClick on the resize icon on the toolbar and you would see the extended toolbar option on the right of the toolbar\n\nTo use crop feature:\n\nType in x,y co-ordinate of the top left corner of the image you want to crop from. Then type in the height and width and click on crop button\n\nTo use Resize feature:\n\nType in the width and height (new dimension of the image) to resize the image and click on Resize button");
		}
		// else if(source == zoomIn) {
		// zoomval += 0.1;
		// canvas[selCanvas].zoomrate = zoomval;
		// canvas[selCanvas].repaint();
		// /// add zoomOut
		// } else if(source == zoomOut) {
		// if (zoomval > 0) {
		// zoomval -= 0.1;
		// canvas[selCanvas].zoomrate = zoomval;
		// canvas[selCanvas].repaint();
		// }

		// }
		// else if(source == TEXT)
	}

	// -------------------------------------------------

	@Override
	public void mouseClicked(MouseEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();

		// indicated which mouse button was pressed
		if (SwingUtilities.isLeftMouseButton(event)) {
			// Do something
		} else if (SwingUtilities.isRightMouseButton(event)) {
			// Do something
		}

		points[pointsCount] = new Point(x, y);
		if (pointsCount < MAX_POINTS - 1)
			pointsCount++;
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		///
		revcnt[selCanvas] += 1;
		maxrevision[selCanvas]++;
		canvasRev[selCanvas][revcnt[selCanvas]] = new Vector<InkSegment>(canvas[selCanvas].v);

		pointsCount = 0;
		/// if (state == LINE) canvas.removeLine();
		if (state == LINE)
			canvas[selCanvas].removeLine();
	}

	public void mouseDragged(MouseEvent event) {

		int x = event.getX();
		int y = event.getY();

		if (SwingUtilities.isLeftMouseButton(event)) {
			if (state == BRUSH) {
				points[pointsCount] = new Point(x, y);
				if (pointsCount < MAX_POINTS - 1) {
					/// canvas.drawPoint(points, pointsCount, brush);
					canvas[selCanvas].drawPoint(points, pointsCount, brush);
					pointsCount++;
				}
			} else if (state == LINE) {
				points[pointsCount] = new Point(x, y);
				if (pointsCount < MAX_POINTS - 1) {
					/// canvas.drawPoint(points, pointsCount, brush);
					canvas[selCanvas].drawLine(points, pointsCount, brush);
					pointsCount++;
				}
			} else if (state == PENCIL) {
				points[pointsCount] = new Point(x, y);
				if (pointsCount < MAX_POINTS - 1) {
					/// canvas.drawPoint(points, pointsCount, pencil);
					canvas[selCanvas].drawPoint(points, pointsCount, pencil);

					pointsCount++;
				}
			} else if (state == ERASER) {
				points[pointsCount] = new Point(x, y);
				if (pointsCount < MAX_POINTS - 1) {
					/// canvas.drawPoint(points, pointsCount, eraser);
					canvas[selCanvas].drawPoint(points, pointsCount, eraser);
					pointsCount++;
				}
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		// TODO Auto-generated method stub

	}

}
