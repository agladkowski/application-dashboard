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
    } else {
        $('.col-sm-3').each(function( index ) {
            $( this ).show();
            var matches = $( this ).attr('application-name').toLowerCase().match(applicationNamePattern.toLowerCase())
            if (!matches) {
                $( this ).hide();
            }
        });
    }
}

$( document ).ready(function() {
    $("#applicationNameFilterDiv").keyup(function() {
        filterApplications($(this).val());
    });
});