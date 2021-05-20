# How to use


This zip contains required files to interact with your deployed business object in your application.


To do that:

* Create a new maven project using `example-pom.xml`
* Extract the two jars `bdm-dao.jar` and `bdm-model.jar` from the zip file, next to your `pom.xml`
* Make sure the engine is accessible using [the HTTP API](https://documentation.bonitasoft.com/bonita/latest/configure-client-of-bonita-bpm-engine#_server_configuration_to_accept_http_connection).

Here  is an example on how to retrieve the DAO to find your objects
```java

// configure APIClient to connect via HTTP
HashMap<String, String> params = new HashMap<String, String>();
params.put("server.url", "http://localhost:8080/");
params.put("application.name", "bonita");
APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP,params);
// connect to the engine
APIClient apiClient = new APIClient();
apiClient.login("walter.bates","bpm");

// retrieve an instance of the DAO
MyBusinessObjectDAO dao = apiClient.getDAO(MyBusinessObjectDAO.class);

// use the DAO
List<MyBusinessObject> myBusinessObjects = dao.find(0, 100);

apiClient.logout();
```