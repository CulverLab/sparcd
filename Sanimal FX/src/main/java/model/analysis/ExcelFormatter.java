package model.analysis;

import model.image.ImageEntry;

import java.io.File;
import java.util.List;

/**
 * A formatter for excel files
 * 
 * @author David Slovikosky
 */
public class ExcelFormatter
{
	/**
	 * 
	 * @param location
	 *            The file to write to
	 * @param images
	 *            The image list (may be filtered) to analyze
	 * @return
	 */
	public boolean format(File location, List<ImageEntry> images, Integer eventInterval)
	{
		if (eventInterval == -1 || images == null)
			return false;

		/*
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet mainSheet = workbook.createSheet("Analysis");
		DataAnalyzer analysis = new DataAnalyzer(images, eventInterval);

		for (ImageEntry entry : images)
		{
			System.out.println(entry.toString());
		}
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
		//			String name = entry.getKey().getCommonName();
		//			Row row = mainSheet.createRow(rowid++);
		//			int cellid = 0;
		//			Cell cellCounter = row.createCell(cellid++, XSSFCell.CELL_TYPE_NUMERIC);
		//			cellCounter.setCellValue(currentNum);
		//			Cell cellDays = row.createCell(cellid++, XSSFCell.CELL_TYPE_NUMERIC);
		//			cellDays.setCellValue(days + 1);
		//			Cell cellName = row.createCell(cellid++, XSSFCell.CELL_TYPE_STRING);
		//			cellName.setCellValue(name);
		//		}
		
		Integer[][] data = new Integer[][]
		{
				{ 2, 3, 4 },
				{ 5, 6, 7 },
				{ 8, 9, 10 } };
		
		int rowid = 0;
		for (Integer[] integers : data)
		{
			Row row = mainSheet.createRow(rowid++);
			int cellid = 0;
			for (Integer integer : integers)
			{
				Cell cell = row.createCell(cellid++, XSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellValue(integer);
			}
		}
		
		//System.out.println(dataType == 0 ? "Number of Pics" : dataType == 1 ? "Abundance" : dataType == 2 ? "Activity" : "Period");
		Drawing drawing = mainSheet.createDrawingPatriarch();
		ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 5, 2, 20, 24);
		Chart chart = drawing.createChart(anchor);
		ChartLegend legend = chart.getOrCreateLegend();
		legend.setPosition(LegendPosition.TOP_RIGHT);
		ScatterChartData chartData = chart.getChartDataFactory().createScatterChartData();
		ValueAxis bottomAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
		ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
		leftAxis.setMinimum(0.0);
		leftAxis.setMinorTickMark(AxisTickMark.NONE);
		bottomAxis.setMinimum(0.0);
		bottomAxis.setMinorTickMark(AxisTickMark.NONE);
		leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
		ChartDataSource<Number> ys = DataSources.fromNumericCellRange(mainSheet, new CellRangeAddress(0, rowid - 1, 0, 0));
		ChartDataSource<Number> xs = DataSources.fromNumericCellRange(mainSheet, new CellRangeAddress(0, rowid - 1, 1, 1));
		//ChartDataSource<Number> ys2 = DataSources.fromNumericCellRange(mainSheet, new CellRangeAddress(2, 2, 0, 3));
		ScatterChartSeries series = chartData.addSerie(xs, ys);
		//ScatterChartSeries series2 = chartData.addSerie(xs, ys2);
		series.setTitle("Data Table");
		//series2.setTitle("Hello World 2!");
		
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
		*/
		return false;
	}
}
