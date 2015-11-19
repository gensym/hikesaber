function makeDateRangeSelector(startDateElementId,
			       endDateElementId,
			       submitButtonElementId,
			       onStartDateSet,
			       onEndDateSet) {
    $.ajax("date_range.json").done(function(data) {
	var startDate = data.start;
	onStartDateSet(startDate);
	var endDate = data.end;
	onEndDateSet(endDate);

	$(startDateElementId).datepicker({minDate: startDate,
					   maxDate: endDate,
					   defaultDate: startDate});
	$(endDateElementId).datepicker({minDate: startDate,
					maxDate: endDate,
					defaultDate: endDate});

	$(submitButtonElementId).click(function() {
            if ($(startDateElementId).val()) {
		onStartDateSet($(startDateElementId).val());
            }

            if ($(endDateElementId).val()) {
		onEndDateSet($(endDateElementId).val());
            }
	});
    });

}
