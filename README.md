Necesario tener instalado:	
-	JDK 7.0
-	Maveb
-	Tomcat
-	Apache
-	MySQL

Crear usuario mysql �books� con contrase�a �books�. Se a�ade base de datos �booksdb�. Para ello, tras crear usuario hacer �source booksdb-data.sql;� dentro del directorio donde se halle. 



Las clases java se encuentran en books-api/src/main/java/edu/upc/.../api

Y las clases java son las siguientes:
	- BookResource.java (para una determinada petici�n, qu� debe hacer
	- BooksApplication.java
	- DataSourceSPA.java
	- MediaType.java
	- MyResource.java (archivo de prueba inicial)
	- BooksError
	- BooksRootAPIResource
	- WebApplicationExceptionMapper
	- ThrowableMapper

En model se encuentran las clases o objetos:
	- Book.java (descripci�n de los par�metros que define un libro con 			getters y setters)
	- BookCollection.java (clase que hace array de books)
	- Review.java (descripci�n de la rese�as)
	- User.java (descripci�n del usuario)
	- BooksRootAPI
