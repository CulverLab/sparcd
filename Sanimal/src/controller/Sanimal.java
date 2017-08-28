package controller;

import java.awt.EventQueue;

import model.SanimalData;
import newView.SanimalViewController;
import view.SanimalView;

import javax.swing.*;

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
		EventQueue.invokeLater(() ->
		{
			new SanimalController(new SanimalView(), new SanimalData());
		});
	}
}
