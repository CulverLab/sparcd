/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.ScatterChartData;
import org.apache.poi.ss.usermodel.charts.ScatterChartSeries;
import org.apache.poi.ss.usermodel.charts.ValueAxis;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import model.ImageEntry;

public class ExcelFormatter
{
	public boolean format(File location, List<ImageEntry> images)
	{
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet mainSheet = workbook.createSheet("Main Analysis");

		List<Integer[]> data = new ArrayList<Integer[]>();
		data.add(new Integer[]
		{ 1, 2, 3, 4 });
		data.add(new Integer[]
		{ 5, 6, 7, 8 });
		data.add(new Integer[]
		{ 9, 10, 11, 12 });

		int rowid = 0;
		for (Integer[] integers : data)
		{
			XSSFRow row = mainSheet.createRow(rowid++);
			int cellid = 0;
			for (Integer integer : integers)
			{
				XSSFCell cell = row.createCell(cellid++);
				cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellValue(integer);
			}
		}

		XSSFDrawing drawing = mainSheet.createDrawingPatriarch();
		ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 5, 10, 15);
		Chart chart = drawing.createChart(anchor);
		ChartLegend legend = chart.getOrCreateLegend();
		legend.setPosition(LegendPosition.TOP_RIGHT);
		ScatterChartData chartData = chart.getChartDataFactory().createScatterChartData();
		ValueAxis bottomAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
		ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
		leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
		ChartDataSource<Number> xs = DataSources.fromNumericCellRange(mainSheet, new CellRangeAddress(0, 0, 0, 3));
		ChartDataSource<Number> ys1 = DataSources.fromNumericCellRange(mainSheet, new CellRangeAddress(1, 1, 0, 3));
		ChartDataSource<Number> ys2 = DataSources.fromNumericCellRange(mainSheet, new CellRangeAddress(2, 2, 0, 3));
		ScatterChartSeries series1 = chartData.addSerie(xs, ys1);
		ScatterChartSeries series2 = chartData.addSerie(xs, ys2);
		series1.setTitle("Hello World!");
		series2.setTitle("Hello World 2!");

		chart.plot(chartData, bottomAxis, leftAxis);

		///
		/// Saving the file, do not change
		///

		try
		{
			OutputStream stream = null;
			if (location.isDirectory())
				stream = new FileOutputStream(new File(location, "Sanimal.xlsx"));
			else if (location.getAbsolutePath().endsWith(".xlsx"))
				stream = new FileOutputStream(location);
			else
				stream = new FileOutputStream(new File(location.getAbsolutePath() + ".xlsx"));
			workbook.write(stream);
			stream.close();
			workbook.close();
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
