Application Dashboard (v1.0)
===
Application Dashboard allows you to view status and version of all your applications in all the pre-prod and production environments.
This app should be deployed on the intranet where security policy usually allows for accessing application 'status' pages. 

Screenshots
===
![dashboard-applications-view2](https://cloud.githubusercontent.com/assets/1870012/6679293/a439b824-cc3d-11e4-9618-aa9e976f6438.PNG)

![dashbord-config-builder](https://cloud.githubusercontent.com/assets/1870012/6679337/18fbdcbe-cc3e-11e4-8302-3b07eca36616.PNG)

![dashboard-configuration](https://cloud.githubusercontent.com/assets/1870012/6340342/545905c2-bbb6-11e4-882d-10f92364d881.PNG)

![dashboard-config-history](https://cloud.githubusercontent.com/assets/1870012/6679382/c9cb3882-cc3e-11e4-9917-2c55726f0a60.PNG)


Requirements
===
Java 7

Building the app
===
$./activator dist

Running the app locally
===
$cd application-dashboard

$./activator run

Go to http://localhost:9000/ in your browser

Running the app on the server
==

$ dashboard-1.0/bin/dashboard -Ddashboard.config.file=/mnt/dashboard.json

Stopping app (https://coderwall.com/p/8k61bg/stop-kill-play-framework-application-with-running_pid)

$ cat RUNNING_PID | xargs kill -SIGTERM

or this:

$ kill -SIGTERM `cat RUNNING_PID`

Credits
==
UI (HTML/Css/JavaSctipt)
- Twitter bootstrap 
    - http://getbootstrap.com/getting-started/
- SBAdmin UI 
    - http://ironsummitmedia.github.io/startbootstrap-sb-admin-2
- Awesome fonts
    - http://fortawesome.github.io/Font-Awesome/icons/

Back-end (scala)
- Play framework
    - https://www.playframework.com/

Roadmap
==
- Record application version changes and present in a form of a timeline
- Load all the application/environment views asynchronously in the HTML UI
- Refresh individual environment view (refresh icon)
- Improve config wizard
