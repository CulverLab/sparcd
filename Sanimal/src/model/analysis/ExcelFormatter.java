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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import model.ImageEntry;

public class ExcelFormatter
{
	public boolean format(File location, List<ImageEntry> images)
	{
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet mainSheet = workbook.createSheet("Main Analysis");
		DataAnalysis analysis = new DataAnalysis(images, 60);

		List<Integer[]> data = new ArrayList<Integer[]>();
		data.add(new Integer[]
		{ 1, 2, 3, 4 });
		data.add(new Integer[]
		{ 5, 6, 7, 8 });
		data.add(new Integer[]
		{ 9, 10, 11, 12 });

		//

		//		Date firstImageDate = analysis.getImagesSortedByDate().get(0).getDateTaken();
		//		List<Map.Entry<Species, ImageEntry>> firstImageEntriesSorted = new ArrayList<Map.Entry<Species, ImageEntry>>(analysis.getSpeciesToFirstImage().entrySet());
		//		Collections.<Map.Entry<Species, ImageEntry>> sort(firstImageEntriesSorted, new Comparator<Map.Entry<Species, ImageEntry>>()
		//		{
		//			@Override
		//			public int compare(Map.Entry<Species, ImageEntry> entry1, Map.Entry<Species, ImageEntry> entry2)
		//			{
		//				return entry1.getValue().getDateTaken().compareTo(entry2.getValue().getDateTaken());
		//			}
		//		});
		//
		//		int number = 0;
		//		int rowid = 0;
		//		for (Map.Entry<Species, ImageEntry> entry : firstImageEntriesSorted)
		//		{
		//			long days = daysBetween(firstImageDate, entry.getValue().getDateTaken());
		//			int currentNum = ++number;
		//			String name = entry.getKey().getName();
		//			Row row = mainSheet.createRow(rowid++);
		//			int cellid = 0;
		//			Cell cellCounter = row.createCell(cellid++, XSSFCell.CELL_TYPE_NUMERIC);
		//			cellCounter.setCellValue(currentNum);
		//			Cell cellDays = row.createCell(cellid++, XSSFCell.CELL_TYPE_NUMERIC);
		//			cellDays.setCellValue(days + 1);
		//			Cell cellName = row.createCell(cellid++, XSSFCell.CELL_TYPE_STRING);
		//			cellName.setCellValue(name);
		//		}

		//

		//		int rowid = 0;
		//		for (Integer[] integers : data)
		//		{
		//			Row row = mainSheet.createRow(rowid++);
		//			int cellid = 0;
		//			for (Integer integer : integers)
		//			{
		//				Cell cell = row.createCell(cellid++, XSSFCell.CELL_TYPE_NUMERIC);
		//				cell.setCellValue(integer);
		//			}
		//		}

		//		XSSFDrawing drawing = mainSheet.createDrawingPatriarch();
		//		XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 5, 2, 20, 24);
		//		XSSFChart chart = drawing.createChart(anchor);
		//		XSSFChartLegend legend = chart.getOrCreateLegend();
		//		legend.setPosition(LegendPosition.TOP_RIGHT);
		//		XSSFScatterChartData chartData = chart.getChartDataFactory().createScatterChartData();
		//		XSSFValueAxis bottomAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
		//		XSSFValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
		//		leftAxis.setMinimum(0.0);
		//		leftAxis.setMinorTickMark(AxisTickMark.NONE);
		//		bottomAxis.setMinimum(0.0);
		//		bottomAxis.setMinorTickMark(AxisTickMark.NONE);
		//		leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
		//		ChartDataSource<Number> ys = DataSources.fromNumericCellRange(mainSheet, new CellRangeAddress(0, rowid - 1, 0, 0));
		//		ChartDataSource<Number> xs = DataSources.fromNumericCellRange(mainSheet, new CellRangeAddress(0, rowid - 1, 1, 1));
		//		//ChartDataSource<Number> ys2 = DataSources.fromNumericCellRange(mainSheet, new CellRangeAddress(2, 2, 0, 3));
		//		ScatterChartSeries series = chartData.addSerie(xs, ys);
		//		//ScatterChartSeries series2 = chartData.addSerie(xs, ys2);
		//		series.setTitle("Species Accumulation");
		//		//series2.setTitle("Hello World 2!");
		//
		//		chart.plot(chartData, bottomAxis, leftAxis);

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

	private long daysBetween(Date date1, Date date2)
	{
		if (date1 != null && date2 != null)
			return ChronoUnit.DAYS.between(date1.toInstant(), date2.toInstant());
		else
			return 0;
	}
}
