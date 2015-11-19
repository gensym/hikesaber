function make_station_usage_plotter(element_id) {

    var required_keys = ['mapbox_api_key', 'background_area'];
    var state = {
    }


    var load_image = function(state) {
	var width = $(element_id).width();
	var height = $(element_id).height();
	// https://api.mapbox.com/v4/{mapid}/{lon},{lat},{z}/{width}x{height}.{format}?access_token=<your access token>
	var mapid = "mapbox.streets";
	var lat = (state['background_area']['max_lat'] + state['background_area']['min_lat']) / 2;
	var lon = (state['background_area']['max_lon'] + state['background_area']['min_lon']) / 2;

	var z = 15; // todo - get this from the max of the lat or lon range
	var format = 'png256';
	var access_token = state['mapbox_api_key'];

	var image_url = "https://api.mapbox.com/v4/" + mapid + "/" + lon + "," + lat +
	    "," + z + "/" + width + "x" + height + "." + format + "?access_token=" + access_token;
	$('<img src="'+ image_url +'">').load(function() {
	    $(element_id).append(this);
	});
    };

    var set_state = function(state, key, val) {
	var oldVal = state[key];
	state[key] = val;
	if (oldVal === undefined &&
	    _.every(required_keys, function(k) {return state[k] !== undefined;})) {
	    load_image(state);
	} 
    }

    return {
	set_mapbox_api_key: function(api_key) {
	    set_state(state, 'mapbox_api_key', api_key);
	},
	set_background_area: function(min_lat, min_lon, max_lat, max_lon) {
	    set_state(state, 'background_area', {min_lat: min_lat,
						 min_lon: min_lon,
						 max_lat: max_lat,
						 max_lon: max_lon});
	}
    };

};

