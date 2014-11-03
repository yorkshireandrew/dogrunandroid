package Applet;

import java.awt.Dimension;

import javax.swing.JLabel;

public class FixedWidthLabel extends JLabel{
	private static final long serialVersionUID = 1L;
	
	int width;
	public FixedWidthLabel(String text, int width)
	{
		super(text);
		this.width = width;
	}
	
	public Dimension getMinimumSize()
	{
	    return new Dimension(width,super.getMinimumSize().height);
	}

	@Override
	public Dimension getPreferredSize()
	{
	    return new Dimension(width,super.getPreferredSize().height);
	}

	@Override
	public Dimension getMaximumSize()
	{
	    return new Dimension(width,super.getMaximumSize().height);
	}
}
