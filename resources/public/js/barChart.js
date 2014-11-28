function barChart(selector, dataset) {

    var margin = {top: 20, right: 30, bottom: 30, left: 80};
    var h = 500 - margin.bottom - margin.top;
    var w = 950 - margin.left - margin.right;
    var barPadding = 4;

    function month(intLabel) {
	return ["Jan",
		"Feb",
		"Mar",
		"Apr",
		"May",
		"Jun",
		"Jul",
		"Aug",
		"Sep",
		"Oct",
		"Nov",
		"Dec"][parseInt(intLabel - 1)]
    }

    function xTickFormatter(s) {
	return month(s.substring(0, 2)) + " " + s.substring(3); 
    }

    var maxCount = d3.max(dataset, function(d) { return d.count; });

    var xPadding = function(v) {
	var padding = [0, 0, 0, 13, 11, 7, 5, 0, 0, 0];
	var numDigits = Math.floor(Math.log(v) / Math.log(10));
	return padding[numDigits];
//	return 0;
    }

    var xFunction = function(d) {
	return d.month + " " + d.year.substring(2);
    }

    var xScale = d3.scale.ordinal()
	.rangeRoundBands([0, w], 0.001)
	.domain(dataset.map(xFunction));


    var yScale = d3.scale.linear()
	.domain([0, d3.max(dataset, function(d) { return d.count; })])
	.range([h, 0]);

    var xAxis = d3.svg.axis()
	.scale(xScale)
	.tickFormat(xTickFormatter)
	.orient("bottom");

    var yAxis = d3.svg.axis()
	.scale(yScale)
	.orient("left");

    var chart = d3.select(selector)
	.attr("width", w + margin.left + margin.right)
	.attr("height", h + margin.top + margin.bottom);

    chart.append("g")
	.attr("transform", "translate(" + margin.left + "," + margin.top + ")")
	.selectAll("rect")
	.data(dataset)
	.enter()
	.append("rect")
	.attr("x", function(d, i) {
	    return xScale(xFunction(d));
	})
	.attr("y", function(d) { return yScale(d.count);  } )
	.attr("width", w / dataset.length - barPadding)
	.attr("height", function(d) { return h - yScale(d.count); })
	.attr("fill", "rgb(47, 202, 252)");

    chart.append("g")
    	.attr("transform", "translate(" + margin.left + "," + margin.top + ")")
	.selectAll(".label")
	.data(dataset)
	.enter()
	.append("svg:text")
	.attr("class", "label")
	.attr("x", function(d, i) {
	    return xScale(xFunction(d)) + xPadding(d.count);
	})
	.attr("y", function(d) {
	    return yScale(d.count) - 5;
	})
	.text(function(d) {
	    return d3.format("0,000")(d.count);
	});

    chart.append("g")
	.attr("class", "axis")
	.attr("transform", "translate(" + margin.left + "," + (h + margin.top) +  ")")
	.call(xAxis);

    chart.append("g")
	.attr("class", "axis")
	.attr("transform", "translate(" + margin.left + "," + margin.top + ")")
	.call(yAxis);
}
