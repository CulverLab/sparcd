let images = fullData;

let firstDate;
let lastDate;

let uniqueLocations;
let uniqueSpecies;

let margin = { top: 5, right: 30, bottom: 30, left: 30 };
let width = 700 - margin.left - margin.right;
let height = 700 - margin.top - margin.bottom;

let oneDay = 1000 * 60 * 60 * 24;

let startDatePicker;
let endDatePicker;

let svgBase;

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

function prepareData()
{
    images.forEach(image => image.dateTakenProperty = new Date(image.dateTakenProperty));

    let datesTaken = images.map(image => image.dateTakenProperty);
    let sortedDates = datesTaken.slice(0).sort((a, b) => a - b);
    firstDate = sortedDates[0];
    lastDate = sortedDates[sortedDates.length - 1];
    let firstImage = images.filter(image => image.dateTakenProperty === firstDate)[0];
    let lastImage = images.filter(image => image.dateTakenProperty === lastDate)[0];

    uniqueLocations = images
        .map(image => image.locationTakenProperty)
        .filter((loc1, index, self) => self.findIndex(loc2 => loc2.idProperty === loc1.idProperty) === index);

    uniqueSpecies = [].concat
        .apply([], images.map(point => point.speciesPresent))
        .map(point => point.speciesProperty)
        .filter((value, index, self) => self.findIndex(t => t.scientificName === value.scientificName) === index);

    d3.select("#refreshButton").on("click", () =>
    {
        refreshScatterplot();
    });
}

function initializeVis()
{
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

    let svg = d3
        .select("#svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom);

    svg
        .append("rect")
        .attr("x", "0")
        .attr("y", "0")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .attr("stroke", "none")
        .attr("fill", "#f0f0f0");

    svgBase = svg
        .append("g")
        .attr("transform", createTranslate(margin.left, margin.top));

    svgBase.append("g")
        .attr("class", "pathAndPoints")
        .selectAll("path")
        .data([1])
        .enter()
        .append("path")
        .attr("d", "")
        .attr("fill", "none")
        .attr("stroke", "black");

    svgBase.append("g")
        .attr("transform", createTranslate(0, height))
        .attr("class", "xAxisMarkings")
        .call(d3
            .axisBottom(d3.scaleLinear()));

    svgBase.append("g")
        .attr("class", "yAxisMarkings")
        .call(d3
            .axisLeft(d3.scaleLinear()));

    let axisLabels = svgBase.append("g");

    axisLabels.append("text")
        .attr("class", "xLabel")
        .text("XAxis")
        .attr("font-size", "25")
        .attr("stroke", "#black")
        .attr("fill", "#007c61")
        .attr("transform", createTranslate(10, 0) + createRotate(90, 0, 0));

    axisLabels.append("text")
        .attr("class", "yLabel")
        .text("YAxis")
        .attr("font-size", "25")
        .attr("stroke", "#black")
        .attr("fill", "#007c61")
        .attr("text-anchor", "end")
        .attr("transform", createTranslate(width, height - 5));
}

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
        startDate = endDate;
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

    ///
    /// Update the SVG elements with the new data
    ///

    // Calculate how we want to aggregate our input images based on the chosen X axis and setup the according scale
    let values = computeXAndYValues(xAxis, yAxis, inputImages, eventInterval);
    let xData = values[0];
    let yData = values[1];
    let xValues = xData.map(data => dataToXValue(xAxis, data));
    let yValues = yData.map(data => dataToYValue(yAxis, data, eventInterval));
    let scaleX = d3.scaleLinear().domain([d3.min(xValues), d3.max(xValues)]).range([0, width]);
    let scaleY = d3.scaleLinear().domain([d3.min(yValues), d3.max(yValues)]).range([height, 0]);
    let lineFunction = d3.line().x(d => scaleX(dataToXValue(xAxis, d[0]))).y(d => scaleY(dataToYValue(yAxis, d[1], eventInterval)));
    let zippedValues = xData.map((d, i) => [d, yData[i]]);

    svgBase.select(".pathAndPoints")
        .selectAll("path")
        .data([zippedValues])
        .transition()
        .attr("d", lineFunction);

    let circles = svgBase.select(".pathAndPoints")
        .selectAll("circle")
        .data(zippedValues);

    circles.enter()
        .append("circle")
        .merge(circles)
        .on("click", d => {
            alert(d[0] + ", " + d[1]);
        })
        .on("mouseover", function (d) {
            d3.select(this)
                .attr("fill", "orange")
                .attr("r", "8")
                .attr("stroke", "black")
                .attr("stroke-width", "2")
        })
        .on("mouseout", function (d) {
            d3.select(this)
                .attr("fill", "black")
                .attr("r", "5")
                .attr("stroke", "none")
                .attr("stroke-width", "0")
        })
        .transition()
        .attr("cx", d => scaleX(dataToXValue(xAxis, d[0])))
        .attr("cy", d => scaleY(dataToYValue(yAxis, d[1], eventInterval)))
        .attr("r", "5");

    circles.exit().remove();

    let bottomAxis = d3
        .axisBottom(scaleX)
        .ticks(Math.clamp(xData.length + 1, 2, 25))
        .tickFormat(null);

    if (xAxis === XAxisEnum.Month)
    {
        tickFormats = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
        bottomAxis.tickFormat(d => tickFormats[d]);
    }
    else if (xAxis === XAxisEnum.Year || xAxis === XAxisEnum.Day)
    {
        bottomAxis.tickFormat(d3.format("d"));
    }

    svgBase.select(".xAxisMarkings")
        .transition()
        .call(bottomAxis);

    svgBase.select(".yAxisMarkings")
        .transition()
        .call(d3.axisLeft(scaleY));

    d3.select(".xLabel")
        .text(yAxis === YAxisEnum.Count ? "Count" : yAxis === YAxisEnum.Activity ? "Activity" : yAxis === YAxisEnum.Abundance ? "Abundance" : "Period");

    d3.select(".yLabel")
        .text(xAxis === XAxisEnum.Day ? "Day of Year" : xAxis === XAxisEnum.Time ? "Time of Day" : xAxis === XAxisEnum.Year ? "Year" : "Month of Year");
}

function computeXAndYValues(xAxis, yAxis, inputImages, eventInterval)
{
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
        // Domain can be between 0 and 366
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

function dataToXValue(xAxis, data)
{
    return parseInt(data);
}

function dataToYValue(yAxis, data, eventInterval)
{
    if (yAxis === YAxisEnum.Count)
        return data.length;
    else if (yAxis === YAxisEnum.Activity)
        return calculateActivity(data);
    else if (yAxis === YAxisEnum.Period)
        return calculatePeriod(data, eventInterval);
    else if (yAxis === YAxisEnum.Abundance)
        return calculateAbundance(data, eventInterval);
}

function createTranslate(x, y)
{
    return " translate (" + x + ", " + y + ")"
}

function createRotate(deg, x, y)
{
    return " rotate (" + deg + ", " + x + ", " + y + ")"
}

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

Math.clamp = (number, min, max) => Math.max(min, Math.min(number, max));
prepareData();
initializeVis();