Necesario tener instalado:	
-	JDK 7.0
-	Maveb
-	Tomcat
-	Apache
-	MySQL

Crear usuario mysql “books” con contraseña “books”. Se añade base de datos “booksdb”. Para ello, tras crear usuario hacer ‘source booksdb-data.sql;’ dentro del directorio donde se halle. 



Las clases java se encuentran en books-api/src/main/java/edu/upc/.../api

Y las clases java son las siguientes:
	- BookResource.java (para una determinada petición, qué debe hacer
	- BooksApplication.java
	- DataSourceSPA.java
	- MediaType.java
	- MyResource.java (archivo de prueba inicial)
	- BooksError
	- BooksRootAPIResource
	- WebApplicationExceptionMapper
	- ThrowableMapper

En model se encuentran las clases o objetos:
	- Book.java (descripción de los parámetros que define un libro con 			getters y setters)
	- BookCollection.java (clase que hace array de books)
	- Review.java (descripción de la reseñas)
	- User.java (descripción del usuario)
	- BooksRootAPI
