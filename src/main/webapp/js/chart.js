google.charts.load('current', {'packages': ['corechart', 'controls']});

var rangeSlider;
var chart;
var chartView = new Set();

var CHART_CONTAINER = 'chart_div';
var RANGE_FILTER_DIV = 'filter_div';
var SOURCE_FILTER_DIV = 'source_filter_div';

function drawDashboard(entries) {
    var dashboard = new google.visualization.Dashboard(document.getElementById('dashboard_div'));
    var data = new google.visualization.DataTable();

    prepareDate(entries, data);

    rangeSlider = new google.visualization.ControlWrapper({
        'controlType': 'ChartRangeFilter',
        'containerId': RANGE_FILTER_DIV,
        options: {
            filterColumnIndex: 0,
            ui: {
                chartType: 'Line',
                chartOptions: {
                    'enableInteractivity': false,
                    'height': 50,
                    'legend': {'position': 'none'},
                    'hAxis': {
                        'textPosition': 'out',
                        'gridlines': {'color': 'none'}
                    },
                    'vAxis': {
                        'textPosition': 'none',
                        'gridlines': {'color': 'none'}
                    }
                },
            }
        }
    });

    chart = new google.visualization.ChartWrapper({
        'chartType': 'LineChart',
        'containerId': CHART_CONTAINER,
        'options': {
            curveType: 'function',
            height: 400,
            legend: {position: 'top'}
        }
    });

    dashboard.bind(rangeSlider, chart);
    dashboard.draw(data);

    drawFilter(data);
}

function prepareDate(entries, data) {
    var sources = {};
    var times = {};

    //--Defining all the Sorces and Time Points --//
    entries.forEach(function (element, index, array) {
        sources[element.name] = {
            name: element.name,
            colNumber: 0
        };
        var dt = element.date + ' ' + element.time;
        times[dt] = [new Date(Date.parse(dt))];
    });

    //--Defining Source position in table --//
    var i = 0;
    for (var source in sources)
        sources[source].colNumber = ++i;


    //--Fill the Time Points with values--//
    entries.forEach(function (element, index, array) {
        var time = times[element.date + ' ' + element.time];
        var col = sources[element.name].colNumber;
        // -- It may be boolean value --//
        if (element.value == 'true') {
            time[col] = 50;
            return
        }
        if (element.value == 'false') {
            time[col] = -50;
            return
        }
        // -- Or it may be Numeric --//
        time[col] = parseInt(element.value);
    });


    var numberOfSources = Object.keys(sources).length;
    var timesArray = Object.values(times);
    var numberOfTimes = timesArray.length;

    //--First row fixing-------------------------------//
    //--Setting value of cell in case of it not exist--//
    for (var i = 1; i <= numberOfSources; i++)
        if (typeof timesArray[0][i] == 'undefined')
            timesArray[0][i] = undefined;

    //--Other cells fixing with previous value--//
    for (var rowIndex = 1; rowIndex < numberOfTimes; rowIndex++)
        for (var colIndex = 1; colIndex <= numberOfSources; colIndex++)
            if (typeof timesArray[rowIndex][colIndex] == 'undefined')
                timesArray[rowIndex][colIndex] = timesArray[rowIndex - 1][colIndex];

    //--Creating table--//
    //--Generate header--//
    data.addColumn('datetime', 'Date and time');
    for (var source in sources)
        data.addColumn('number', sources[source].name);
    //--Fill the rows--//
    data.addRows(timesArray);
}

function drawFilter(data) {

    var containerContent = "";

    chartView.add(0);
    for (var i = 1; i < data.getNumberOfColumns(); i++) {
        chartView.add(i);

        containerContent +=
            "<label class='checkbox-inline'>" +
            "<input type='checkbox' checked='true' value=" + i + " onchange='updateViews(this);'>"
            + data.getColumnLabel(i) +
            "</label>"
    }

    document.getElementById(SOURCE_FILTER_DIV).innerHTML = containerContent;
}

function updateViews(checkbox) {
    // View updating //
    var lineNumber = parseInt(checkbox.value);
    if (checkbox.checked)
        chartView.add(lineNumber);
    else
        chartView.delete(lineNumber);

    //-- Disable filter in case of one active line on chart --//
    var labels = document.getElementById(SOURCE_FILTER_DIV).childNodes;
    if (chartView.size == 2) {
        labels.forEach(function (node) {
            var checkbox = node.childNodes[0];
            if (checkbox.checked)
                checkbox.disabled = true;

        });
    } else {
        labels.forEach(function (node) {
            node.childNodes[0].disabled = false;
        });
    }

    // Chart Redrawing//
    var chartViewColumns = Array.from(chartView).sort();
    chart.setView({columns: filterViewColumns});
    chart.draw();

    // Control Redrawing //
    // Control view relates on chart view and therefore control columns have another index
    var filterViewColumns = [];
    for (var i = 0; i < chartViewColumns.length; i++)
        filterViewColumns.push(i);

    rangeSlider.setView({columns: chartViewColumns});
    rangeSlider.draw();
}