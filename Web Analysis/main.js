// Class: CS 544
// Author: David Slovikosky
// Final Project

// testData for smaller dataset
let images = fullData;

let firstDate;
let lastDate;

let uniqueLocations;
let uniqueSpecies;

let marginPrimary = { top: 30, right: 30, bottom: 30, left: 50 };
let widthPrimary = 500 - marginPrimary.left - marginPrimary.right;
let heightPrimary = 500 - marginPrimary.top - marginPrimary.bottom;

let marginSpeciesAccum = { top: 30, right: 30, bottom: 30, left: 100 };
let widthSpeciesAccum = 500 - marginSpeciesAccum.left - marginSpeciesAccum.right;
let heightSpeciesAccum = 500 - marginSpeciesAccum.top - marginSpeciesAccum.bottom;

let oneDay = 1000 * 60 * 60 * 24;

let startDatePicker;
let endDatePicker;

let svgPrimaryBase;
let svgSpeciesAccumBase;

let XAxisEnum =
{
    Time: 0,
    Month: 1,
    Year: 2,
    Day: 3
};

let YAxisEnum =
{
    Count: 0,
    Activity: 1,
    Abundance: 2,
    Period: 3
};

// Prepares the data for drawing
function prepareData()
{
    // Convert Java's date format to JavaScript's date format
    images.forEach(image => image.dateTakenProperty = new Date(image.dateTakenProperty));

    // Find the first and last dates
    let datesTaken = images.map(image => image.dateTakenProperty);
    let sortedDates = datesTaken.slice(0).sort((a, b) => a - b);
    firstDate = sortedDates[0];
    lastDate = sortedDates[sortedDates.length - 1];

    // Find the unique locations and species on the images
    uniqueLocations = images
        .map(image => image.locationTakenProperty)
        .filter((loc1, index, self) => self.findIndex(loc2 => loc2.idProperty === loc1.idProperty) === index);

    uniqueSpecies = [].concat
        .apply([], images.map(point => point.speciesPresent))
        .map(point => point.speciesProperty)
        .filter((value, index, self) => self.findIndex(t => t.scientificName === value.scientificName) === index);

    // Create action listeners for the buttons
    d3.select("#refreshButton").on("click", () => refreshScatterplot());
    d3.select("#saveButton").on("click", () => saveSvgAsPng(d3.select("#svg").node(), "chart.png"));
    d3.select("#saveButton2").on("click", () => saveSvgAsPng(d3.select("#svg2").node(), "speciesAccumulationCurve.png"));
    d3.select("#selectAllSpecies").on("click", () => d3.select("#species").selectAll("input").property("checked", true));
    d3.select("#selectNoSpecies").on("click", () => d3.select("#species").selectAll("input").property("checked", false));
    d3.select("#selectAllLocations").on("click", () => d3.select("#locations").selectAll("input").property("checked", true));
    d3.select("#selectNoLocations").on("click", () => d3.select("#locations").selectAll("input").property("checked", false));
}

// Initializes the visualization with axis, labels, and inputs
function initializeVis()
{
    // Create a list of species checkboxes
    let labels = d3.select("#species")
        .selectAll("label")
        .data(uniqueSpecies)
        .enter()
        .append("label");

    labels.append("input")
        .attr("type", "checkbox")
        .attr("name", "speciesBox")
        .attr("checked", "true")
        .attr("value", (data) => data.name);

    labels.append("text").text(data => data.name);
    labels.append("br");

    // Create a list of location checkboxes
    labels = d3.select("#locations")
        .selectAll("label")
        .data(uniqueLocations)
        .enter()
        .append("label");

    labels.append("input")
        .attr("type", "checkbox")
        .attr("name", "speciesBox")
        .attr("checked", "true")
        .attr("value", (data) => data.nameProperty);

    labels.append("text").text(data => data.nameProperty);
    labels.append("br");

    // Initialize the datepicker start and end
    startDatePicker = datepicker("#startDate", {
        minDate: firstDate,
        maxDate: lastDate,
        dateSelected: firstDate
    });

    endDatePicker = datepicker("#endDate", {
        minDate: firstDate,
        maxDate: lastDate,
        dateSelected: lastDate
    });

    // Create the primary SVG width and height
    let svgPrimary = d3
        .select("#svg")
        .attr("width", widthPrimary + marginPrimary.left + marginPrimary.right)
        .attr("height", heightPrimary + marginPrimary.top + marginPrimary.bottom);

    svgPrimary
        .append("rect")
        .attr("x", "0")
        .attr("y", "0")
        .attr("width", widthPrimary + marginPrimary.left + marginPrimary.right)
        .attr("height", heightPrimary + marginPrimary.top + marginPrimary.bottom)
        .attr("stroke", "none")
        .attr("fill", "#f0f0f0");

    // Setup a base position to draw axis and points at
    svgPrimaryBase = svgPrimary
        .append("g")
        .attr("transform", createTranslate(marginPrimary.left, marginPrimary.top));

    // Create the path which we will manipulate to create the scatterplot
    svgPrimaryBase.append("g")
        .attr("class", "pathAndPoints")
        .selectAll("path")
        .data([1])
        .enter()
        .append("path")
        .attr("d", "")
        .attr("fill", "none")
        .attr("stroke", "black");

    // Create the X and Y axis
    svgPrimaryBase.append("g")
        .attr("transform", createTranslate(0, heightPrimary))
        .attr("class", "xAxisMarkings")
        .call(d3
            .axisBottom(d3.scaleLinear()));

    svgPrimaryBase.append("g")
        .attr("class", "yAxisMarkings")
        .call(d3
            .axisLeft(d3.scaleLinear()));

    // Create X and Y axis labels
    let axisLabels = svgPrimaryBase.append("g");

    axisLabels.append("text")
        .attr("class", "xLabel")
        .text("XAxis")
        .attr("font-size", "25")
        .attr("fill", "#007c61")
        .attr("transform", createTranslate(10, 0) + createRotate(90, 0, 0));

    axisLabels.append("text")
        .attr("class", "yLabel")
        .text("YAxis")
        .attr("font-size", "25")
        .attr("fill", "#007c61")
        .attr("text-anchor", "end")
        .attr("transform", createTranslate(widthPrimary, heightPrimary - 5));

    // Create the species accumulation curve SVG width and height
    let svgSpeciesAccum = d3
        .select("#svg2")
        .attr("width", widthSpeciesAccum + marginSpeciesAccum.left + marginSpeciesAccum.right)
        .attr("height", heightSpeciesAccum + marginSpeciesAccum.top + marginSpeciesAccum.bottom);

    // Create the path which we will manipulate to create the scatterplot
    svgSpeciesAccum
        .append("rect")
        .attr("x", "0")
        .attr("y", "0")
        .attr("width", widthSpeciesAccum + marginSpeciesAccum.left + marginSpeciesAccum.right)
        .attr("height", heightSpeciesAccum + marginSpeciesAccum.top + marginSpeciesAccum.bottom)
        .attr("stroke", "none")
        .attr("fill", "#f0f0f0");

    // Setup a base position to draw axis and points at
    svgSpeciesAccumBase = svgSpeciesAccum
        .append("g")
        .attr("transform", createTranslate(marginSpeciesAccum.left, marginSpeciesAccum.top));

    svgSpeciesAccumBase.append("g")
        .attr("class", "pathAndPoints")
        .selectAll("path")
        .data([1])
        .enter()
        .append("path")
        .attr("d", "")
        .attr("fill", "none")
        .attr("stroke", "black");

    // Create the X and Y axis
    svgSpeciesAccumBase.append("g")
        .attr("transform", createTranslate(0, heightSpeciesAccum))
        .attr("class", "xAxisMarkings")
        .call(d3
            .axisBottom(d3.scaleLinear()));

    svgSpeciesAccumBase.append("g")
        .attr("class", "yAxisMarkings")
        .call(d3
            .axisLeft(d3.scaleLinear()));

    // Create X and Y axis labels
    let axisLabelsSpeciesAccum = svgSpeciesAccumBase.append("g");

    axisLabelsSpeciesAccum.append("text")
        .attr("class", "xLabel")
        .text("Species")
        .attr("font-size", "25")
        .attr("fill", "#007c61")
        .attr("transform", createTranslate(10, 0) + createRotate(90, 0, 0));

    axisLabelsSpeciesAccum.append("text")
        .attr("class", "yLabel")
        .text("Day First Seen")
        .attr("font-size", "25")
        .attr("fill", "#007c61")
        .attr("text-anchor", "end")
        .attr("transform", createTranslate(widthSpeciesAccum, heightSpeciesAccum - 5));
}

// Called whenever the user clicks "refresh"
function refreshScatterplot()
{
    ///
    /// Prepare the inputs to the graph
    ///

    let eventInterval = d3.select("#eventInterval").property("value");
    if (isNaN(eventInterval))
        eventInterval = 5;

    let startDate = startDatePicker.dateSelected;
    let endDate = endDatePicker.dateSelected;
    if (endDate - startDate < 0)
    {
        let temp = endDate;
        endDate = startDate;
        startDate = temp;
    }

    let speciesSelected = [];
    d3.select("#species").selectAll("input").each(function (data) {
        let checkbox = d3.select(this);
        if (checkbox.property("checked"))
            speciesSelected.push(data);
    });

    let locationsSelected = [];
    d3.select("#locations").selectAll("input").each(function (data) {
        let checkbox = d3.select(this);
        if (checkbox.property("checked"))
            locationsSelected.push(data);
    });

    let speciesCheck = function (speciesToTest, masterSpeciesList)
    {
        for (let i = 0; i < speciesToTest.length; i++)
            if (masterSpeciesList.filter(species => species.scientificName === speciesToTest[i].scientificName).length === 0)
                return false;
        return true;
    };

    let locationCheck = function (locationToTest, locationList)
    {
        for (let i = 0; i < locationList.length; i++)
            if (locationToTest.latProperty === locationList[i].latProperty && locationToTest.lngProperty === locationList[i].lngProperty)
                return true;
        return false;
    };

    let inputImages = images.filter(image => {
        let inDateRange = image.dateTakenProperty >= startDate && image.dateTakenProperty <= endDate;
        let correctSpecies = speciesCheck(image.speciesPresent.map(speciesEntry => speciesEntry.speciesProperty), speciesSelected);
        let correctLocation = locationCheck(image.locationTakenProperty, locationsSelected);
        return inDateRange && correctLocation && correctSpecies;
    });

    let xAxis;
    switch(d3.select("#xAxisChoices").select("input[name=xAxis]:checked").node().value)
    {
        case "time":
            xAxis = XAxisEnum.Time;
            break;
        case "month":
            xAxis = XAxisEnum.Month;
            break;
        case "year":
            xAxis = XAxisEnum.Year;
            break;
        case "day":
            xAxis = XAxisEnum.Day;
            break;
        default:
            xAxis = XAxisEnum.Time;
    }

    let yAxis;
    switch(d3.select("#yAxisChoices").select("input[name=yAxis]:checked").node().value)
    {
        case "count":
            yAxis = YAxisEnum.Count;
            break;
        case "activity":
            yAxis = YAxisEnum.Activity;
            break;
        case "abundance":
            yAxis = YAxisEnum.Abundance;
            break;
        case "period":
            yAxis = YAxisEnum.Period;
            break;
        default:
            yAxis = YAxisEnum.Count;
    }

    let isBarChart = d3.select("#showBars").node().value === "bar";

    ///
    /// Update the SVG elements with the new data we just calculated
    ///
    updatePrimarySVG(inputImages, xAxis, yAxis, isBarChart, eventInterval);

    updateSpeciesAccumSVG(inputImages);
}

// Updates the left SVG element
function updatePrimarySVG(inputImages, xAxis, yAxis, isBarChart, eventInterval)
{
    // Calculate how we want to aggregate our input images based on the chosen X axis and setup the according scale
    let values = computeXAndYValues(xAxis, yAxis, inputImages);
    let xData = values[0];
    let yData = values[1];
    let xValues = xData.map(data => dataToXValue(xAxis, data));
    let yValues = yData.map(data => dataToYValue(yAxis, data, eventInterval));
    let scaleX = d3.scaleLinear().domain([d3.min(xValues), d3.max(xValues)]).range([0, widthPrimary]);
    let scaleY = d3.scaleLinear().domain([d3.min(yValues), d3.max(yValues)]).range([heightPrimary, 0]);
    let lineFunction = d3.line().x(d => scaleX(dataToXValue(xAxis, d[0]))).y(d => scaleY(dataToYValue(yAxis, d[1], eventInterval)));
    let zippedValues = xData.map((d, i) => [d, yData[i]]);

    // Update the path element
    svgPrimaryBase.select(".pathAndPoints")
        .selectAll("path")
        .data([zippedValues])
        .transition()
        .attr("d", isBarChart ? "" : lineFunction);

    // Update the bar elements
    let bars = svgPrimaryBase.select(".pathAndPoints")
        .selectAll("rect")
        .data(zippedValues);

    // Update the bar elements if it's a bar chart, otherwise remove them
    if (isBarChart)
    {
        bars.enter()
            .append("rect")
            .merge(bars)
            .transition()
            .attr("x", d => scaleX(dataToXValue(xAxis, d[0])) - 5)
            .attr("y", d => scaleY(dataToYValue(yAxis, d[1], eventInterval)))
            .attr("fill", "#d7d7d7")
            .attr("width", "10")
            .attr("height", d => heightPrimary - scaleY(dataToYValue(yAxis, d[1], eventInterval)));

        bars.exit().remove();
    }
    else
    {
        bars.remove();
    }

    // Update the vertex circle elements
    let circles = svgPrimaryBase.select(".pathAndPoints")
        .selectAll("circle")
        .data(zippedValues);

    circles.enter()
        .append("circle")
        .merge(circles)
        .moveToFront()
        .on("click", d => circleClicked(d[1]))
        .on("mouseover", function (d) {
            d3.select(this)
                .attr("fill", "orange")
                .attr("r", "8")
                .attr("stroke", "black")
                .attr("stroke-width", "2");
            svgPrimaryBase.select(".pathAndPoints")
                .append("text")
                .attr("x", () => {
                    let baseX = scaleX(dataToXValue(xAxis, d[0]));
                    return baseX > widthPrimary / 2 ? baseX - 15 : baseX + 15;
                })
                .attr("y", scaleY(dataToYValue(yAxis, d[1], eventInterval)) + 4)
                .attr("text-anchor", scaleX(dataToXValue(xAxis, d[0])) > widthPrimary / 2 ? "end" : "left")
                .text("(" + Math.floor(dataToXValue(xAxis, d[0])) + ", " + Math.floor(dataToYValue(yAxis, d[1], eventInterval)) + ")")
        })
        .on("mouseout", function () {
            d3.select(this)
                .attr("fill", "black")
                .attr("r", "5")
                .attr("stroke", "none")
                .attr("stroke-width", "0");
            svgPrimaryBase.select(".pathAndPoints").select("text").remove();
        })
        .transition()
        .attr("cx", d => scaleX(dataToXValue(xAxis, d[0])))
        .attr("cy", d => scaleY(dataToYValue(yAxis, d[1], eventInterval)))
        .attr("r", "5");

    // Remove extra circles without data
    circles.exit().remove();

    // Update the axis
    let bottomAxis = d3
        .axisBottom(scaleX)
        .ticks(Math.clamp(xData.length + 1, 2, 25))
        .tickFormat(null);

    // Update the month axis labels to be the months instead of integers
    if (xAxis === XAxisEnum.Month)
    {
        const tickFormats = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
        bottomAxis.tickFormat(d => tickFormats[d]);
    }
    // Years or days are integers only
    else if (xAxis === XAxisEnum.Year || xAxis === XAxisEnum.Day)
    {
        bottomAxis.tickFormat(d3.format("d"));
    }

    svgPrimaryBase.select(".xAxisMarkings")
        .transition()
        .call(bottomAxis);

    svgPrimaryBase.select(".yAxisMarkings")
        .transition()
        .call(d3.axisLeft(scaleY));

    // Update the axis labels
    d3.select(".xLabel")
        .text(yAxis === YAxisEnum.Count     ? "Count" :
              yAxis === YAxisEnum.Activity  ? "Activity" :
              yAxis === YAxisEnum.Abundance ? "Abundance" :
                                              "Period");

    d3.select(".yLabel")
        .text(xAxis === XAxisEnum.Day  ? "Day of Year" :
              xAxis === XAxisEnum.Time ? "Time of Day" :
              xAxis === XAxisEnum.Year ? "Year" :
                                         "Month of Year");
}

// Updates the right SVG element
function updateSpeciesAccumSVG(inputImages)
{
    // We need images sorted
    let sortedImages = inputImages.slice(0).sort((a, b) => a.dateTakenProperty - b.dateTakenProperty);
    let data = [];
    uniqueSpecies.forEach(species => {
        let firstImage = sortedImages.filter(image => image.speciesPresent.map(speciesEntry => speciesEntry.speciesProperty.scientificName).includes(species.scientificName))[0];
        if (firstImage !== undefined)
        {
            let currentDate = firstImage.dateTakenProperty;
            let start = firstDate;
            let diff = (currentDate - start) + ((start.getTimezoneOffset() - currentDate.getTimezoneOffset()) * 60 * 1000);
            let day = Math.floor(diff / oneDay);
            data.push([day, {speciesID: species, imageID: firstImage }]);
        }
    });

    data.sort((a, b) => a[0] - b[0]);

    let xValues = data.map(x => x[0]);
    let yValues = data.map((x, i) => i);
    let scaleX = d3.scaleLinear().domain([d3.min(xValues), d3.max(xValues)]).range([0, widthSpeciesAccum]);
    let scaleY = d3.scaleLinear().domain([d3.min(yValues), d3.max(yValues)]).range([heightSpeciesAccum, 0]);
    let lineFunction = d3.line().x(d => scaleX(d[0])).y((d, i) => scaleY(i));

    // Update the path with newly calculated data
    svgSpeciesAccumBase.select(".pathAndPoints")
        .selectAll("path")
        .data([data])
        .transition()
        .attr("d", lineFunction);

    // Update the vertex circle elements
    let circles = svgSpeciesAccumBase.select(".pathAndPoints")
        .selectAll("circle")
        .data(data);

    circles.enter()
        .append("circle")
        .merge(circles)
        .on("click", d => circleClicked([d[1].imageID]))
        .on("mouseover", function (d, i) {
            d3.select(this)
                .attr("fill", "orange")
                .attr("r", "8")
                .attr("stroke", "black")
                .attr("stroke-width", "2");
            svgSpeciesAccumBase.select(".pathAndPoints")
                .append("text")
                .attr("x", () => {
                    let baseX = scaleX(d[0]);
                    return baseX > widthSpeciesAccum / 2 ? baseX - 15 : baseX + 15;
                })
                .attr("y", scaleY(i) + 4)
                .attr("text-anchor", scaleX(d[0]) > widthSpeciesAccum / 2 ? "end" : "left")
                .text(d[1].speciesID.name + ": " + d[0]);
            // Highlight the circle from the other chart that contains this image
            svgPrimaryBase.select(".pathAndPoints")
                .selectAll("circle")
                .filter(dataOther => dataOther[1].includes(d[1].imageID))
                .attr("fill", "orange")
                .attr("r", "10")
                .attr("stroke", "black")
                .attr("stroke-width", "2");
        })
        .on("mouseout", function (d) {
            d3.select(this)
                .attr("fill", "black")
                .attr("r", "5")
                .attr("stroke", "none")
                .attr("stroke-width", "0");
            svgSpeciesAccumBase.select(".pathAndPoints").select("text").remove();
            // Stop highlighting the circle from the other chart that contains this image
            svgPrimaryBase.select(".pathAndPoints")
                .selectAll("circle")
                .filter(dataOther => dataOther[1].includes(d[1].imageID))
                .attr("fill", "black")
                .attr("r", "5")
                .attr("stroke", "none")
                .attr("stroke-width", "0");
        })
        .transition()
        .attr("cx", d => scaleX(d[0]))
        .attr("cy", (d, i) => scaleY(i))
        .attr("r", "5");

    // Remoe extra elements
    circles.exit().remove();

    // Update the axis
    svgSpeciesAccumBase.select(".xAxisMarkings")
        .transition()
        .call(d3.axisBottom(scaleX));

    // The left axis contains the species names instead of indices
    let leftAxis = d3
        .axisLeft(scaleY)
        .ticks(data.length + 1)
        .tickFormat(d => d % 1 === 0 ? data[d][1].speciesID.name: "");

    svgSpeciesAccumBase.select(".yAxisMarkings")
        .transition()
        .call(leftAxis);
}

// When we click a vertex, update the selected images
function circleClicked(images)
{
    // Browser will not let me load images from disk because of security concerns
    /*
    let fileReader = new FileReader();
    fileReader.onload = (event) => {
        d3.select("#previewImg")
            .attr("src", event.target.result);
    };

    console.log("file:///" + d[1][0].imageFileProperty.path);
    let request = new XMLHttpRequest();
    request.responseType = "blob";
    request.open("GET", "file:///" + d[1][0].imageFileProperty.path);
    request.onload = () => {
        console.log(request.response);
        fileReader.readAsDataURL(request.response);
    }
    request.send();
    */
    // Grab the selected items list and append the first 10 to the list
    let listItems = d3.select("#selectedImages")
        .selectAll("li")
        .data(images.slice(0, 10));

    listItems.enter()
        .append("li")
        .merge(listItems)
        .text(d => d.imageFileProperty.path.replace(/\\/g,'/').replace(/.*\//, '') + " -- " + d.imageFileProperty.path);

    listItems.exit().remove();
}

// Compute the X and Y values of the left SVG graph based on the axis selected and input images
function computeXAndYValues(xAxis, yAxis, inputImages)
{
    // The aggregation function for the X axis values
    let groupBy = function(array, func)
    {
        return array.reduce((currentArr, currentValue) => {
            (currentArr[func(currentValue)] = currentArr[func(currentValue)] || []).push(currentValue);
            return currentArr;
        }, {});
    };

    // Keys are x values, values are y values
    let xAndYPoints;
    if (xAxis === XAxisEnum.Time)
        // Domain can be between 0 and 23
        xAndYPoints = groupBy(inputImages, image => image.dateTakenProperty.getHours());
    else if (xAxis === XAxisEnum.Day)
        // Domain can be between 0 and 365
        xAndYPoints = groupBy(inputImages, image => {
            let currentDate = image.dateTakenProperty;
            let start = new Date(currentDate.getFullYear(), 0, 0);
            let diff = (currentDate - start) + ((start.getTimezoneOffset() - currentDate.getTimezoneOffset()) * 60 * 1000);
            return Math.floor(diff / oneDay);
        });
    else if (xAxis === XAxisEnum.Month)
        // Domain can be between 0 and 11
        xAndYPoints = groupBy(inputImages, image => image.dateTakenProperty.getMonth());
    else if (xAxis === XAxisEnum.Year)
        // Domain can be between 1900-current year
        xAndYPoints = groupBy(inputImages, image => image.dateTakenProperty.getFullYear());

    let xValues = Object.keys(xAndYPoints);
    let yValues = Object.values(xAndYPoints);

    return [xValues, yValues];
}

// Used to convert a data point and x axis type to an integer
function dataToXValue(xAxis, data)
{
    return parseInt(data);
}

// Used to convert a data point and y axis type to an integer
function dataToYValue(yAxis, data, eventInterval)
{
    // Depending on the y axis type, we calculate different y values
    if (yAxis === YAxisEnum.Count)
        return data.length;
    else if (yAxis === YAxisEnum.Activity)
        return calculateActivity(data);
    else if (yAxis === YAxisEnum.Period)
        return calculatePeriod(data, eventInterval);
    else if (yAxis === YAxisEnum.Abundance)
        return calculateAbundance(data, eventInterval);
}

// Useful helper function to create SVG transform strings
function createTranslate(x, y)
{
    return " translate (" + x + ", " + y + ")"
}

// Useful helper function to create SVG transform strings
function createRotate(deg, x, y)
{
    return " rotate (" + deg + ", " + x + ", " + y + ")"
}

// Used to calculate the activity value of an image list, algorithm is described in the writeup
function calculateActivity(imageList)
{
    imageList.sort((a, b) => a.dateTakenProperty - b.dateTakenProperty);

    let activity = 0;

    let oldHour = -1;
    let oldDay = -1;
    let oldYear = -1;

    for (let i = 0; i < imageList.length; i++)
    {
        let image = imageList[i];
        let hour = image.dateTakenProperty.getHours();

        // https://stackoverflow.com/questions/8619879/javascript-calculate-the-day-of-the-year-1-366
        let currentDate = image.dateTakenProperty;
        let start = new Date(currentDate.getFullYear(), 0, 0);
        let diff = (currentDate - start) + ((start.getTimezoneOffset() - currentDate.getTimezoneOffset()) * 60 * 1000);
        let day = Math.floor(diff / oneDay);

        let year = currentDate.getFullYear();

        if (hour !== oldHour || oldDay !== day || oldYear !== year)
        {
            activity = activity + 1;
            oldHour = hour;
            oldDay = day;
            oldYear = year;
        }
    }

    return activity;
}

// Used to calculate the period value of an image list, algorithm is described in the writeup
function calculatePeriod(imageList, eventInterval)
{
    imageList.sort((a, b) => a.dateTakenProperty - b.dateTakenProperty);

    let period = 0;

    let lastImageTimeMillis = 0;
    for (let i = 0; i < imageList.length; i++)
    {
        let image = imageList[i];

        let imageTimeMillis = image.dateTakenProperty.getTime();
        let differenceMillis = imageTimeMillis - lastImageTimeMillis;
        let differenceMinutes = differenceMillis / 1000 / 60;
        if (differenceMinutes > eventInterval)
            period++;
        lastImageTimeMillis = imageTimeMillis;
    }

    return period;
}

// Used to calculate the abundance value of an image list, algorithm is described in the writeup
function calculateAbundance(imageList, eventInterval, speciesFilter = null)
{
    imageList.sort((a, b) => a.dateTakenProperty - b.dateTakenProperty);

    let abundance = 0;
    let lastImageTimeMillis = 0;
    let maxAnimalsInEvent = 0;
    for (let i = 0; i < imageList.length; i++)
    {
        let image = imageList[i];

        let imageTimeMillis = image.dateTakenProperty.getTime();
        let differenceMillis = imageTimeMillis - lastImageTimeMillis;
        let differenceMinutes = differenceMillis / 1000 / 60;

        if (differenceMinutes > eventInterval)
        {
            abundance = abundance + maxAnimalsInEvent;
            maxAnimalsInEvent = 0;
        }

        for (let j = 0; j < image.speciesPresent.length; j++)
        {
            let speciesEntry = image.speciesPresent[j];
            if (speciesFilter === null || speciesEntry.speciesProperty === speciesFilter)
                maxAnimalsInEvent = Math.max(maxAnimalsInEvent, speciesEntry.amountProperty);
        }

        lastImageTimeMillis = imageTimeMillis;
    }

    abundance = abundance + maxAnimalsInEvent;

    return abundance;
}

// Add a function I've used a lot from C# or Java which ensures a value is inbetween a min and max value, or returns the closest thing in the range [min, max]
Math.clamp = (number, min, max) => Math.max(min, Math.min(number, max));

// Add a move-to-front method which moves a DOM element to the front of the rendering process
d3.selection.prototype.moveToFront = function() {
    return this.each(function(){
        this.parentNode.appendChild(this);
    });
};

// the "main" of the program is really simple. We prepare our data and then initialize the visualization
prepareData();
initializeVis();