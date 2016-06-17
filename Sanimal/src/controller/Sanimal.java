package controller;

import java.awt.EventQueue;

import model.SanimalData;
import view.SanimalView;

public class Sanimal
{
	public static void main(String args[])
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				new SanimalController(new SanimalView(), new SanimalData());
			}
		});
	}
}
