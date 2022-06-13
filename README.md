
# Your Friends In The World - Assignment 1

 This product has been developed for the Android development course and is not intended to be published or used commercially.
 
## Installation
* Clone repository
https://github.com/twgust/Your-Friends-In-The-World
* Verify that your SDK Manager is fully up to date and that GMS has been installed.
* Provide an API key for Google Maps in the Local.properties folder.
* Make sure that your API key is RESTRICTED, this can be one in the google developer cloud console. 
https://developers.google.com/maps/api-security-best-practices
* Get your local ip from ipconfig and change the value of 'ipString' (ConnectThread.java) to the value of your local ipv4 address. 
* Sync Project with gradle files
* Run the server 
Navigate to com.example.assignment1.server and execute 'java -jar p2server.exe'
* Run the app on your emulator or android device. 

## Resources
#### https://developers.google.com/maps - Setup your API KEY

## Server
### Communicating with the server
[![image](https://i.imgur.com/T860crj.png)](server-to-from-1)
#### The server periodically sends out the 'messages from server'.
The client can't ask for these messages in a request and have them returned. Instead the client must wait for the server to send these JSON messages to the clients input stream. 

[![image](https://i.imgur.com/sNcPL6J.png)](server-from)
[![image](https://i.imgur.com/FxrcGT5.png)](server-to-from-2)

## 🔗 Contributors
[![portfolio](https://i.imgur.com/0rd5oK3.png)](https://github.com/twgust)


