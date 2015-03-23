function loadApplicationStatusDiv(applicationName) {
    var divId = '#' + applicationName + '-status-div'
    $(divId).html("<div>Loading...</div>");
    $(divId).load('/applications/' + applicationName + '/ajax');
}

function filterApplications(applicationNamePattern) {
    if (!$.trim(applicationNamePattern).length) {
        $('.col-sm-3').each(function( index ) {
            $( this ).show();
        });
        updateUrl("")
    } else {
        updateUrl("?filter=" + applicationNamePattern)
        $('.col-sm-3').each(function( index ) {
            $( this ).show();
            var matches = $( this ).attr('application-name').toLowerCase().match(applicationNamePattern.toLowerCase())
            if (!matches) {
                $( this ).hide();
            }
        });
    }
}

function updateUrl(filterParameter) {
    if (window.history) {
        window.history.pushState(null, "Applications", "/applications" + filterParameter)
    }
}

function GetURLParameter(sParam) {
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++)
    {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam)
        {
            return sParameterName[1];
        }
    }
}


$( document ).ready(function() {
    if ( $( "#applicationNameFilterDiv" ).length ) {
        $("#applicationNameFilterDiv").keyup(function(e) {
            filterApplications($(this).val());
        });
        filterApplications(GetURLParameter("filter"));
    }

});