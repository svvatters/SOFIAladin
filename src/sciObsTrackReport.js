// Pseudo global namespace
var tr = {};  // track report

// Names of the SOFIA imagers
tr.imagers = ["fpi", "ffi", "wfi"];

// Track star classifications
tr.classes = ["fpi", "ffi", "ffi-corner", "wfi", "wfi-corner"];

// The _skySep '-corner' minimum distances in arcminutes
// NOTE:  THESE MAY CHANGE AND SINCE THEY'RE HARDCODED HERE AND IN A JAVA
// ENUM THEY BOTH NEED TO BE UPDATED
tr.cornerDistances = [4.5, 30.0];

//
tr.obsPlanTable = document.getElementById("obsPlan-table");

// Catalog tables to be processed
tr.trackStars = document.getElementById("potential-track-stars");

// Tables of catalog data that will be classified before being displayed
tr.tables = tr.trackStars.getElementsByTagName("table");

tr.main = function() {

  // Make the ObsPlan data table a simple dataTable
  $(tr.obsPlanTable).dataTable({
    "paging":   false,
    "ordering": false,
    "info":     false,
    "searching": false,
  });

  // Classify the original catalog tables as hidden ("no-display)
  for (var i=0; i<tr.tables.length; i++) {
    var t = tr.tables.item(i);

    // Add the class "no-display" to them
    t.setAttribute("class", t.className + " no-display");

    // TODO: Display the track stars counts
    for (var j=0; j<tr.classes.length; j++) {
      var rows = t.getElementsByClassName(tr.classes[j]);
      
//      // TESTING
//      console.log(tr.classes[j] + ": " + rows.length);
    }
  }

  // Process each catalog table
  for (var i=0; i<tr.tables.length; i++) {
    var t = tr.tables.item(i);

    //
//    var table = t.cloneNode(true);

    var headers = tr.createTableHeaders(t);

    // TODO: Add units

    // Iterage through each imager and create a table of tracks stars for them
    for (var j=0; j<tr.imagers.length; j++) {
      var imager = tr.imagers[j];

      //
      var array = tr.createTableArray(imager, t);

      // If the new table isn't empty add it to trackStars and make it a dataTable
      if (array.length > 0) {

        //
        var newTable = document.createElement('table');
        var id = imager+"_"+t.id.toLowerCase();
        newTable.setAttribute("id", id);
        newTable.setAttribute("class", "catalog display");

        tr.trackStars.appendChild(newTable);

//        arrayToDataTable('#'+id, array, headers);

        //
        $('#'+id).dataTable({
          "data" : array,
          "columns" : headers,
          "destroy" : true,
        });

        // TODO: Mark new dataTables "corner" objects with 'class=imager + "-corner"'

      }
    }
  }
}

tr.createTableHeaders = function(table) {
  var out = [];
  var header = table.getElementsByClassName("names");
  for (var i=0; i<header.length; i++) {
    var names = header.item(i).getElementsByTagName('th');
    for (var j=0; j<names.length; j++) {
      var name = names.item(j).cloneNode('true');
      out[j] = {'title' : name.innerHTML};
    }
  }
  return out;
}

tr.createTableArray = function(imager, table) {
  var out = [];
  var sections = [imager, imager + "-corner"];
  for (var i=0; i<sections.length; i++) {
    var rows = table.getElementsByClassName(sections[i]);
    for (var j=0; j<rows.length; j++) {
      var tds = rows.item(j).getElementsByTagName("td");
      var row = [];
      for (var k=0; k<tds.length; k++) {
        row[k] = tds.item(k).innerHTML;
      }
      out[out.length] = row;
    }
  }
  return out;
}

tr.arrayToDataTable = function(tableId, array, headers) {
  $(tableId).dataTable({
    "data" : array,
    "columns" : headers,
    "destroy" : true,
  });
}

tr.flagCornerStars = function() {}

tr.applyBootstrap = function() {}

$(document).ready(tr.main());

//
//  for (var i=0; i<ucds.length; i++) {
//    var ths = ucds[i].getElementsByTagName("th");
//    for (var j=0; j<ths.length; j++) {
//      console.log(j);
//      if (ths[j].innerHTML.match(/angDistance/gi)){
//        console.log("angDistance: " + j);
//        skySep_col = j;
//      };
//    }
//  }
