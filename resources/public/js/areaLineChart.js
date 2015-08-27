// selector - any d3 selector (https://github.com/mbostock/d3/wiki/Selections)
// dataPath
function makeAreaLineChart(selector, dataPath) {
    var timeFormat = d3.time.format('%H:%M');

    function addAxesAndLegend (svg, xAxis, yAxis, margin, chartWidth, chartHeight) {

	var legendWidth  = 200,
	legendHeight = 100;

	// clipping to make sure nothing appears behind legend
	svg.append('clipPath')
	    .attr('id', 'axes-clip')
	    .append('polygon')
	    .attr('points', (-margin.left)                 + ',' + (-margin.top)                 + ' ' +
                  (chartWidth - legendWidth - 1) + ',' + (-margin.top)                 + ' ' +
                  (chartWidth - legendWidth - 1) + ',' + legendHeight                  + ' ' +
                  (chartWidth + margin.right)    + ',' + legendHeight                  + ' ' +
                  (chartWidth + margin.right)    + ',' + (chartHeight + margin.bottom) + ' ' +
                  (-margin.left)                 + ',' + (chartHeight + margin.bottom));

	var axes = svg.append('g')
	    .attr('clip-path', 'url(#axes-clip)');

	axes.append('g')
	    .attr('class', 'x axis')
	    .attr('transform', 'translate(0,' + chartHeight + ')')
	    .call(xAxis);

	axes.append('g')
	    .attr('class', 'y axis')
	    .call(yAxis)
	    .append('text')
	    .attr('transform', 'rotate(-90)')
	    .attr('y', 6)
	    .attr('dy', '.71em')
	    .style('text-anchor', 'end')
	    .text('Time (s)');

	var legend = svg.append('g')
	    .attr('class', 'legend')
	    .attr('transform', 'translate(' + (chartWidth - legendWidth) + ', 0)');

	legend.append('rect')
	    .attr('class', 'legend-bg')
	    .attr('width',  legendWidth)
	    .attr('height', legendHeight);

	legend.append('rect')
	    .attr('class', 'outer')
	    .attr('width',  75)
	    .attr('height', 20)
	    .attr('x', 10)
	    .attr('y', 10);

	legend.append('text')
	    .attr('x', 115)
	    .attr('y', 25)
	    .text('5% - 95%');

	legend.append('rect')
	    .attr('class', 'inner')
	    .attr('width',  75)
	    .attr('height', 20)
	    .attr('x', 10)
	    .attr('y', 40);

	legend.append('text')
	    .attr('x', 115)
	    .attr('y', 55)
	    .text('25% - 75%');

	legend.append('path')
	    .attr('class', 'median-line')
	    .attr('d', 'M10,80L85,80');

	legend.append('text')
	    .attr('x', 115)
	    .attr('y', 85)
	    .text('Median');
    }

    function drawPaths (svg, data, x, y) {
	var upperOuterArea = d3.svg.area()
	    .interpolate('linear')
	    .x (function (d) { return x(d.time) || 1; })
	    .y0(function (d) { return y(d.pct95); })
	    .y1(function (d) { return y(d.pct75); });

	var upperInnerArea = d3.svg.area()
	    .interpolate('linear')
	    .x (function (d) { return x(d.time) || 1; })
	    .y0(function (d) { return y(d.pct75); })
	    .y1(function (d) { return y(d.pct50); });

	var medianLine = d3.svg.line()
	    .interpolate('linear')
	    .x(function (d) { return x(d.time); })
	    .y(function (d) { return y(d.pct50); });

	var lowerInnerArea = d3.svg.area()
	    .interpolate('linear')
	    .x (function (d) { return x(d.time) || 1; })
	    .y0(function (d) { return y(d.pct50); })
	    .y1(function (d) { return y(d.pct25); });

	var lowerOuterArea = d3.svg.area()
	    .interpolate('linear')
	    .x (function (d) { return x(d.time) || 1; })
	    .y0(function (d) { return y(d.pct25); })
	    .y1(function (d) { return y(d.pct05); });

	svg.datum(data);

	svg.append('path')
	    .attr('class', 'area upper outer')
	    .attr('d', upperOuterArea)
	    .attr('clip-path', 'url(#rect-clip)');

	svg.append('path')
	    .attr('class', 'area lower outer')
	    .attr('d', lowerOuterArea)
	    .attr('clip-path', 'url(#rect-clip)');

	svg.append('path')
	    .attr('class', 'area upper inner')
	    .attr('d', upperInnerArea)
	    .attr('clip-path', 'url(#rect-clip)');

	svg.append('path')
	    .attr('class', 'area lower inner')
	    .attr('d', lowerInnerArea)
	    .attr('clip-path', 'url(#rect-clip)');

	svg.append('path')
	    .attr('class', 'median-line')
	    .attr('d', medianLine)
	    .attr('clip-path', 'url(#rect-clip)');
    }


    function makeChart (data) {
	var svgWidth  = 960,
	svgHeight = 500,
	margin = { top: 20, right: 20, bottom: 40, left: 40 },
	chartWidth  = svgWidth  - margin.left - margin.right,
	chartHeight = svgHeight - margin.top  - margin.bottom;

	var x = d3.scale.linear().range([0, chartWidth])
            .domain(d3.extent(data, function (d) { return d.time; })),
	y = d3.scale.linear().range([chartHeight, 0])
            .domain([0, d3.max(data, function (d) { return d.pct95; })]);

	var xAxis = d3.svg.axis()
	    .scale(x)
	    .tickFormat(function(x) {
		return timeFormat(new Date(x));
	    })
	    .orient('bottom')
            .innerTickSize(-chartHeight)
	    .outerTickSize(0)
	    .tickPadding(10);

	var yAxis = d3.svg.axis()
	    .scale(y)
	    .orient('left')
            .innerTickSize(-chartWidth)
	    .outerTickSize(0)
	    .tickPadding(10);

	var svg = d3.select(selector).append('svg')
	    .attr('width',  svgWidth)
	    .attr('height', svgHeight)
	    .append('g')
	    .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');


	addAxesAndLegend(svg, xAxis, yAxis, margin, chartWidth, chartHeight);
	drawPaths(svg, data, x, y);
    }

    return function() {
	d3.json(dataPath, function (error, rawData) {
	    if (error) {
		console.error(error);
		return;
	    }

	    var data = rawData.map(function (d) {
		return {
		    time:  timeFormat.parse(d.time).valueOf(),
		    pct05: d.percentiles["0.05"],
		    pct25: d.percentiles["0.25"],
		    pct50: d.percentiles["0.50"],
		    pct75: d.percentiles["0.75"],
		    pct95: d.percentiles["0.95"]
		};
	    });

	    makeChart(data);

	});
    };
}

