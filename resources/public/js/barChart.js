function makeChart(xFunction, yFunction, selector, dataset) {

    var svgElem = d3.select(selector);

    var margin = {top: 20, right: 30, bottom: 30, left: 80};
    var h = parseInt(svgElem.style("height")) - margin.bottom - margin.top;
    var w = parseInt(svgElem.style("width")) - margin.left - margin.right;
    var barPadding = 4;

    var xScale = d3.scale.ordinal()
	.rangeRoundBands([0, w], 0.001)
	.domain(dataset.map(xFunction));

    var yScale = d3.scale.linear()
	.domain([0, d3.max(dataset, yFunction)])
	.range([h, 0]);

    var chart = svgElem
	.attr("width", w + margin.left + margin.right)
	.attr("height", h + margin.top + margin.bottom);

    var charter = {};

    charter.drawChart = function() {
	chart.append("g")
	    .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
	    .selectAll("rect")
	    .data(dataset)
	    .enter()
	    .append("rect")
	    .attr("x", function(d, i) {
		return xScale(xFunction(d));
	    })
	    .attr("y", function(d) { return yScale(yFunction(d));  } )
	    .attr("width", w / dataset.length - barPadding)
	    .attr("height", function(d) { return h - yScale(yFunction(d)); })
	    .attr("fill", "rgb(47, 202, 252)");

	return charter;
    }

    charter.addXAxis = function() {
	var xAxis = d3.svg.axis()
	    .scale(xScale)
	    .tickFormat(xTickFormatter)
	    .ticks(20)
	    .orient("bottom");

	chart.append("g")
	    .attr("class", "axis")
	    .attr("transform", "translate(" + margin.left + "," + (h + margin.top) +  ")")
	    .call(xAxis);

	return charter;
    }

    charter.addYAxis = function () {
	var yAxis = d3.svg.axis()
	    .scale(yScale)
	    .orient("left");

	chart.append("g")
	    .attr("class", "axis")
	    .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
	    .call(yAxis);
	return charter;
    }

    charter.addYLabels = function() {
	return charter;
    }

    return charter;
}
