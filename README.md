# Jar ServletContext
This simple project demonstrates a servlet context problem when spring boot is executed in a jar.

When a static resource is needed to be loaded dynamically with the servlet context:
```
getServletContext().getResourceAsStream("/module/afile.rpc")
```
Tomcat embedded in a spring boot __jar__ returns *null*.

When packaged in a __war__, it returns the file without any issue.

_This type of file loading is mandatory in GWT applications._

With this simple project, an index.html has a link TEST which invokes a servlet trying to load the .rpc file. It generates an exception on the server side.

If you activate the spring profile ___customizetomcatctx___, it loads with success. The quick fix adds to the ```TomcatContextCustomizer``` a ```JarResourceSet``` pointing at the __static__ directory.

