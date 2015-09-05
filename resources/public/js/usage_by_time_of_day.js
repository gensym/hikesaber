// Depends on jquery (at least 2.1.1), and underscore.js

function isChecked(selector) {
    return $(selector).is(':checked');
}

// Requires checkboxes.length >= 2
function ensureAtLeastOneSelected(checkbox_selectors) {


    var check = function(selector) {
	$(selector).prop('checked', true);
    }
    _.each(checkbox_selectors, function(checkbox) {
	$(checkbox).change(function() {
	    if (isChecked(this)) {
		return;
	    } else {
		var others = _.without(checkbox_selectors, checkbox);
		if (_.find(others, isChecked) === undefined) {
		    check(_.first(others));
		}
	    }
	});
    });

    if (_.find(checkbox_selectors, isChecked) === undefined) {
	check(_.first(checkbox_selectors));
    }
}
