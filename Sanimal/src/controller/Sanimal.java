package controller;

import javax.swing.JOptionPane;

import model.SanimalData;
import view.SanimalView;

/**
 * Main class entry point
 * 
 * @author David Slovikosky
 * @version 1.0
 */
public class Sanimal
{
	public static void main(String args[])
	{
		// Ensure that the user is using Java 8 and not Java 7-
		if (System.getProperty("java.version").startsWith("1.8."))
			new SanimalController(new SanimalView(), new SanimalData());
		else
			JOptionPane.showMessageDialog(null, "Java 8 must be installed to use this software!");
	}
}
