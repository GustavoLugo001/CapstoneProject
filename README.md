***Introduction**

Landowners play a crucial role in agriculture, forestry, and land management, but maintaining large parcels of land is time-consuming and complex. Traditional record-keeping methods, such as paper maps and manual logs, are prone to loss, errors, and inefficiency. Many landowners struggle with tracking tree locations, irrigation lines, and other infrastructure, leading to disorganization and mismanagement.
To address this challenge, we propose the development of an interactive digital mapping system that allows landowners to digitally record, visualize, and manage their land assets. This platform incorporates Artificial Intelligence (AI) for predictive water and fertilization recommendations, helping users optimize resource use and increase agricultural efficiency.

**Please keep in mind that this will only run locally but is set up to be putting official servers to run permanently.  

***To run:***

1.In order to run locally make sure that you enable HTTPS on browser
2.You will also require HTTPS token to actually work which is keystore.p12 to see the actual token it is combined perms I had to use this in order to make my React app work with https as well as a default.

3.So, make sure everything is in HTTPS and that front-end and back-end have the token which is the certification I self-certified so makes the site a threat which is why you enable HTTPS on browser. 

4.Application properties in src/main/resources contain most of the information for the connections as well as debug and ssl key. 

5.To actually run this program have back-end in eclipse and right click JwtDemoApplication.java got to Run As and select java application.

![image](https://github.com/user-attachments/assets/b592b9b6-d721-4bf7-b1f5-0068194a9561)

6.MySQL you will have to make sure you have the same information as the application properties as well as the tables also the database name which should be interact database interactive_map which all tables will be in and assure that the server is running. In my case I open MySQL enter the secure password and start. To make sure it is actually running go to server status and see if it is running make sure it is the right port.

![image](https://github.com/user-attachments/assets/518179d2-791b-47df-ba38-7a852e8588f0)
![image](https://github.com/user-attachments/assets/c6b13cd5-90ef-4cf3-9e82-d636dc1fea25)


7.To run flask-ai you will need to go into the interactive-map-frontend/flask-ai assure you are cd to it now run .\venv\Scripts\activate  make sure that it shows (venv) PS and finally python app.py 

![image](https://github.com/user-attachments/assets/370d23eb-bc61-46d2-af67-9a624a197c82)


8.Now for React app open cmd aka the terminal  on window search bar cd to interactive-map-frontend then npm start now a web application will open up and the whole program will work 

![image](https://github.com/user-attachments/assets/d34b8b1e-6cf7-4a3a-a096-3c35d12fab43)
![image](https://github.com/user-attachments/assets/03d81f11-2614-4697-badd-74bb71360bcd)



you will have to create an account but once doing so it should look like so:

![image](https://github.com/user-attachments/assets/83e16ee9-2860-4368-88a9-d2473cc3da92)



***Each Files goal/ task***

**back-end:**


AuthController.java – The task for this file is to allow Requesting Mapping which allows the call for specific directories. In the files you can see that the call will be /api now anything else for example /test must be called /api/test this allows the program to know specific files making it easier. Now this file contains login and register. The others while there were codes in order to assure my program was running they are not used and are there if they want to be used in more security measures.

ElectricalLine.java – This file contains the getters and setters of ElectricalLine now they also grab the information from the table as well as set based on the table being called which in this case electrical_lines so we must identify which variables will represent the column to help.

ElectricalLineController.java – The controller is again another RequestMapping which will be as /api/electrical-lines. Now in this we have 5 main functions approveElectricalLine which is simple it will assure whether the ROLE_ADMIN is placing the object if so then it will be APPORVED. denyElectricalLine which is the same concept it will grab the item and it will be determined if you want to deny. getElectricalLines which will be a list what its task is that it will going to the current user that has logged in and it will the see whether ROLE_ADMIN to get all that have the id if a ROLE_USER it will locate the admin_id and garb the Electrical Line. addElectricalLine which is the act of being able to put the object by doing so ROLE_ADMIN will be putting permissions to Approve and ROLE_USER will set everything to a pending but the object will then be added with the needed information. deleteElectricalLine which is simple it will locate the name of the Electrical Line and delete from table.  

ElectricalLineRepository.java – The file while small is dedicated in the List of ElectricalLine based on finding it based on the admin Id

JwtAuthenticationFilter.java – Now this files allows the request of HTTP in order to actually request a token now this has some debugging to assure that the code being sent is actually the same one as the one given and assuring that the request is being made.

JwtDemoApplication.java – this is the Main head of the program this is what you call to start the serever and to be able to use all files in the project. 

JwtService.java – This file is what creates and also puts a time limit to the token as one can see its expiration if I recall was an hour. It also makes the key to gives the okay to the api,js to be able to make the calls to the back-end.

PasswordHasher.java – this file was to start the process of hash passwords but I ended up in a mess  it was then use to assure that JWT tokens were being generated correctly as it was failing.

SavedLocation.java - This file contains the getters and setters of SavedLocation now they also grab the information from the table as well as set based on the table being called which in this case svaed_location so we must identify which variables will represent the column to help.

SavedLocationController.java - The controller is again another RequestMapping which will be as /api/electrical-lines. Now in this we have 5 main functions approveSavedLocation which is simple it will assure whether the ROLE_ADMIN is placing the object if so then it will be APPORVED. denySavedLocation which is the same concept it will grab the item and it will be determined if you want to deny. getSavedLocations which will be a list what its task is that it will going to the current user that has logged in and it will the see whether ROLE_ADMIN to get all that have the id if a ROLE_USER it will locate the admin_id and garb the Saved  Location. addSavedLocation which is the act of being able to put the object by doing so ROLE_ADMIN will be putting permissions to Approve and ROLE_USER will set everything to a pending but the object will then be added with the needed information. deleteSavedLocation which is simple it will locate the name of the Electrical Line and delete from table.  

SavedLocationRepository.java – (does the same as ElectricalLineRepository.java just for SavedLocation)

SecurityConfig.java – While simple was the most challenging in my program the concept first assure that you have JWT token then we confirm that the user is who they say they are and providing a token. SecurityFilter which was tasked in making the calls from each controller were allowed to be called and what level of security they would have. UserDetail service  is assuring that the username and password are in database by calling userRepository , AuthenticationManager allows for the Spring security to allow access, NoOpPasswordEncoder  which is just a state off saying that plain text will be allowed this would have to be changed if you would like to make a more secured password and finally CorsConfigurationSource  which is to allow the front-end to have access to certain features and access to the back-end. The challenging part in this was due to the fact that csrf was active due to Spring boot which was a simple fix but bothersome.

Tree.java – which is the setter and getters which will call the table trees fairly simple the only complex setter is setLocation which is combining latitude and longitude.

TreeController.java - (does the same as ElectricalLineController.java just for tree) the only change is that updateTreeDates which updates fertilization, watering, and planted date. updateTree which is just updating information which is more regard to the flask-ai

TreeRepository.java – which is to grab the information or update based on the Query 

TreeService.java - which is more of a dive when the UpdateTree is called it will end up here and do the behind the scenes which is actually plugging it into the table such as deleteTree which removes from table , and updateTreeCareSchedules which is importing the date.

User.java – getters and setters which will store and get from table users.

UserRepository.java -  checking username and password as well as getting information regarding the query  based on the information.

WaterLine.java – getters and setters which call the get and set of table water_line

WaterLineController.java – (like electricalLineController.java just for waterline)

WaterLineRepository.java – (like electricalLineRepository.java just for waterline)

**Front-end:**


App.css – which is where the data 

App.js – which is the main page of the login which can be customized but left normally for easier time. This mainly has the login, register, and logout

Dashboard.js – which is the admin code as well as the role of the user that is logged in. 

Login.js – is the login and of the user as well as the call of the api it will grab the username and password and if the login fails will be told, but if login works will then retrieve a token from 

Logout.js – which will remove the token and remove from local storage in front-end  and moves user back to login page. 

MapSearch.js – this is a search bar for the map I used multiple source due to the plugins of leaflet not working  based on being to update or React being to updated and files missing. This allows the connection of the leaflet and also where the location is at it also has it based on the incorrect t coordinates. 

Registration.js – which is the ability of registering the new user it also contains the formatting. It will first submit the items entered which will be in text once doing so it will make sure that all are confirmed. If an error is found it will say it was already entered.

TreeMap.js – Now this file contains all the information needed it shows all the information to fetching to getting as well as rendering to pop-up as well getting fetching information from Flask-ai 

api.js – which are the call that are done to the back end  making sure that data is driven either json or text regarding the type that is being fetch, added from the backend 

index.css –  added by React app to start the local hosting format 

index.js – added by React app to start the local hosting

logo.svg – added by React app

reportWebVitals.js - added by React app

**Flask-ai:**


app.py – is the main structure and will provide information regarding the user and provide the information and base it off the found sites and sources this is the flask 

train_fertilization_model.py- is the structure of how the ai works and is utilized. 

train_watering_model.py - is the structure of how the ai works and is utilized.

update_tree_care.py – the ai that will use the base information and the user information and input what is more appropriate to the estimates. This will then show the fully tested data based on the estimated data and choose based on which is better.  
The other files are made for the ai to work and the csv are simple but meant to help in estimating data of trees to get an average unfortunately I had to get from several sources and try to combine them in order for it to work in the method I needed. 

**MySQL:**


 contains all the great data with 5 tables 

users - id, username, password, role, email, admin_code, admin_id

trees - id, species, health_status, height, next_fertilization_date, owner_id, user_has_permission, last_watering_date, next_watering_date, last_fertilization_date, soil_moisture_level, temperature, humidity, planting_date, latitude, longitude, health_note, approval_status

water_lines - id, admin_id, name, line_geometry, description, created_at, approval_status

saved_location - id, admin_id, name, location, description, created_at, approval_status

electrical_lines -  id, admin_id, name, line_geometry, description, created_at, approval_status

tree_permissions - tree_id, user_id (not really used but was considered in approval and deny status then initially thought to make it in a sense a history log on what trees get removed but was not implemented.)




