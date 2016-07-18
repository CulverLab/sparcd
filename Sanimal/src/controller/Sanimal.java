package controller;

import javax.swing.JOptionPane;

import model.SanimalData;
import view.SanimalView;

public class Sanimal
{
	public static void main(String args[])
	{
		if (System.getProperty("java.version").startsWith("1.8."))
			new SanimalController(new SanimalView(), new SanimalData());
		else
			JOptionPane.showMessageDialog(null, "Java 8 must be installed to use this software!");
	}
}
