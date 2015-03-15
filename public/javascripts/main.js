function loadApplicationStatusDiv(applicationName) {
    var divId = '#' + applicationName + '-status-div'
    $(divId).html("<div>Loading...</div>");
    $(divId).load('/applications/' + applicationName + '/ajax');
}

function filterApplications(applicationNamePattern) {
    if (applicationNamePattern)
    $.find('.col-sm-3')[1].attributes['application-name']
}