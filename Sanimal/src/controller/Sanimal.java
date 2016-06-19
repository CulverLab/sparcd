package controller;

import model.SanimalData;
import view.SanimalView;

public class Sanimal
{
	public static void main(String args[])
	{
		new SanimalController(new SanimalView(), new SanimalData());
	}
}
