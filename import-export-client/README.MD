<h1>Import/Export client</h1>

<h2>Usage</h2>
Set spring.profiles.active when running the program to configure datasource url. Some environments are predefined, such as systemtest-a, with the properties-file "application-systemtest-a.properties". <br>
To configure your own environment, use application.properties and run the program without setting spring.profiles.active, or create your own .properties file 
with the format 'application-{profile-name}.properties'. Then run with <br>
<b> java -Dspring.profiles.active={profile-name} </b>
<br>

Example command: <br>
<b>java -Dspring.profiles.active=systemtest-a -jar import-export-client-SNAPSHOT-1.0.13.jar {options>} </b> 