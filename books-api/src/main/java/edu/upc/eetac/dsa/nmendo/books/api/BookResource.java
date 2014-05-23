package edu.upc.eetac.dsa.nmendo.books.api;

import java.security.SecurityPermission;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import edu.upc.eetac.dsa.nmendo.books.api.DataSourceSPA;
import edu.upc.eetac.dsa.nmendo.books.api.MediaType;
import edu.upc.eetac.dsa.nmendo.books.api.model.Book;
import edu.upc.eetac.dsa.nmendo.books.api.model.BookCollection;
import edu.upc.eetac.dsa.nmendo.books.api.model.Review;
import edu.upc.eetac.dsa.nmendo.books.api.model.User;

@Path("/books")
public class BookResource {

	// variables globales:
	@Context
	SecurityContext security;
	private DataSource ds = DataSourceSPA.getInstance().getDataSource();
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

	private boolean administrator;

	/*
	 * @POST public String createBook() { if (!security.isUserInRole("admin")) {
	 * throw new ForbiddenException("You are not an admin."); } return
	 * security.getUserPrincipal().getName() + " you can createBook()"; }
	 * 
	 * @Path("/{booksid}/reviews")
	 * 
	 * @POST public String createReview() { return "createReview()"; }
	 */

	// para obtener la coleción de libros GET (1)

	@GET
	@Produces(MediaType.BOOKS_API_BOOK_COLLECTION)
	public BookCollection getBooks() {
		BookCollection books = new BookCollection();

		// hacemos la conexión a la base de datos
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			String sql = buildQueryGetBooksCollection();
			stmt = conn.prepareStatement(sql);
			// obtenemos la respuesta
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				/*
				Book book = new Book();
				book.setBookid(rs.getInt("bookid"));
				book.setAutor(rs.getString("autor"));
				book.setTitulo(rs.getString("titulo"));
				book.setEdicion(rs.getString("edicion"));
				book.setLengua(rs.getString("lengua"));
				book.setEditorial(rs.getString("editorial"));
				// book.setFecha(rs.getDate("fecha"));//¿cómo indico variable
				// fecha (DATE) en
				// mysql en java
				book.setFechae(sdf.format(rs.getDate("fechae")));
				book.setFechai(sdf.format(rs.getDate("fechai")));*/
				int bookid = rs.getInt("bookid");
				Book book = new Book();
				book = getBookAndReviewsFromBookid(bookid);
				books.addBook(book);

			}
			
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}

		return books;
	}

	// para el GET de un libro con bookid (2)
	@GET
	@Path("/{bookid}")
	@Produces(MediaType.BOOKS_API_BOOK)
	public Book getBookid(@PathParam("bookid") int bookid) {
		return getBookFromDatabase(bookid);
	
	}

	// (3) para hacer GET a partir de un pattern: TITULO
	@GET
	@Path("/search")
	@Produces(MediaType.BOOKS_API_BOOK_COLLECTION)
	public BookCollection search(@QueryParam("title") String titulo,
			@QueryParam("author") String autor) {
		BookCollection books = new BookCollection();
		// hacemos la conexión a la base de datos
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {

			// Dependiendo de si los parámetros son nulos o no:
			// Se considera que se puede pedir o por el título o por el autor,
			// no ambas
			String sql = buildGetStingsQueryByTitleAuthor(titulo, autor);
			if (sql == null)
				throw new BadRequestException(
						"Search parameters should be title and/or author.");
			stmt = conn.prepareStatement(sql);
			if ((autor != null) && (titulo == null)) {
				stmt.setString(1, "%" + autor + "%");
			} else if ((autor == null) && (titulo != null)) {
				stmt.setString(1, "%" + titulo + "%");
			}

			// obtenemos la respuesta
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Book book = new Book();
				int bookid = rs.getInt("bookid");
				book = getBookAndReviewsFromBookid(bookid);
				books.addBook(book);
			}
		}

		catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
		return books;

	}

	// (5) POST-> crear una ficha de un libro nuevo (sólo ADMIN)
	@POST
	@Consumes(MediaType.BOOKS_API_BOOK)
	@Produces(MediaType.BOOKS_API_BOOK)
	public Book createBook(Book book) {
		setAdministrator(security.isUserInRole("admin"));
		// Comprobamos que el usuario que vaya a crear la ficha de libro sea
		// ADMIN
		if (!security.isUserInRole("admin")) {
			throw new ForbiddenException("You are not an admin.");
		}
		// compruebo que no hayan parámetros nulos
		validateBook(book);

		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			String sql = buildQueryInsertBook();
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, book.getTitulo());
			stmt.setString(2, book.getAutor());
			stmt.setString(3, book.getLengua());
			stmt.setString(4, book.getEdicion());
			stmt.setString(5, book.getEditorial());

			try {
				Date fechae = new Date(sdf.parse(book.getFechae()).getTime());
				Date fechai = new Date(sdf.parse(book.getFechai()).getTime());

				stmt.setDate(6, fechae);
				stmt.setDate(7, fechai);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			stmt.executeUpdate(); // para añadir la ficha del libro con los
									// datos a la BBDD

			// si ha ido bien la inserción
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				int bookid = rs.getInt(1);
				// devuelve el nuevo book
				book = getBookFromDatabase(bookid);
			} else {
				throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
				throw new ServerErrorException(e.getMessage(),
						Response.Status.INTERNAL_SERVER_ERROR);
			}
		}

		return book;
	}

	// (6)PUT que el administrador Actualice la ficha de un libro
	@PUT
	@Path("/{bookid}")
	@Consumes(MediaType.BOOKS_API_BOOK)
	@Produces(MediaType.BOOKS_API_BOOK)
	public Book updateBook(@PathParam("bookid") int bookid, Book book) {
		// Comprobamos que el usuario que vaya a crear la ficha de libro sea
		// ADMIN: llamamos al método validateUser del usuario user
		if (!security.isUserInRole("admin")) {
			throw new ForbiddenException(
					"You are not an admin. You can not do this action.");
		}

		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			// llamamos a la función para la query y la hacemos la database
			String sql = buildUpdateBook();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, book.getTitulo());
			stmt.setString(2, book.getAutor());
			stmt.setString(3, book.getLengua());
			stmt.setString(4, book.getEdicion());
			stmt.setString(5, book.getEditorial());
			try {
				if (book.getFechae() != null) {
					Date fechae = new Date(sdf.parse(book.getFechae())
							.getTime());
					stmt.setDate(6, fechae);
				} else
					stmt.setDate(6, null);
				if (book.getFechai() != null) {
					Date fechai = new Date(sdf.parse(book.getFechai())
							.getTime());

					stmt.setDate(7, fechai);
				} else
					stmt.setDate(7, null);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stmt.setInt(8, bookid);
			stmt.executeUpdate(); // para añadir la ficha del libro con los
									// datos a la BBDD
			// si ha ido bien la inserción
			// ResultSet rs = stmt.getGeneratedKeys();
			// if (rs.next()) {
			// devuelve el nuevo book
			book = getBookFromDatabase(bookid);
			// } else {
			// throw new NotFoundException("Could not update book");
			// }

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
				throw new ServerErrorException(e.getMessage(),
						Response.Status.INTERNAL_SERVER_ERROR);
			}
		}

		return book;

	}

	// (7)DELETE, eliminar una ficha de un libro con bookid

	@DELETE
	@Path("/{bookid}")
	public void deleteBook(@PathParam("bookid") int bookid) {
		// Comprobamos que el usuario que vaya a crear la ficha de libro sea
		// ADMIN: llamamos al método validateUser del usuario user
		if (!security.isUserInRole("admin")) {
			throw new ForbiddenException("You are not an admin.");
		}

		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			// llamamos a la función para la query y la hacemos la database
			String sql = buildDeleteBook();
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, bookid);

			int rows = stmt.executeUpdate();

			if (rows == 0) {
				throw new NotFoundException("There's no book with bookid="
						+ bookid);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
				throw new ServerErrorException(e.getMessage(),
						Response.Status.INTERNAL_SERVER_ERROR);
			}
		}
	}

	// (8) Hacer publicación de una reseña en la ficha de un libro con bookid
	@POST
	@Path("/{bookid}/reviews")
	@Consumes(MediaType.BOOKS_API_REVIEW)
	@Produces(MediaType.BOOKS_API_REVIEW)
	public Book creatReview(@PathParam("bookid") int bookid, Review review) {
		// Comprobamos que el usuario que vaya a crear la ficha de libro sea
		// ADMIN
		Book book = null;

		if (!security.isUserInRole("registered")) {
			throw new ForbiddenException(
					"You have not registered or you are not admin");
		}

		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		// asegurar que el usuario no haya hecho ya un review del libro:
		// en caso contrario hace un throw
		validateUserReview(bookid);

		PreparedStatement stmt = null;
		try {
			String sql = buildCreateReview();
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, bookid);
			stmt.setString(2, security.getUserPrincipal().getName());
			stmt.setString(3, review.getReview_text());
			stmt.executeUpdate();

			// si ha ido bien la inserción
			// ResultSet rs = stmt.getGeneratedKeys();
			// if (rs.next()) {
			book = getBookFromDatabase(bookid);
			// review2 = getReviewFromDatbase(review.getReviewid());
			// } else {
			// // Something has failed...
			// }
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
				throw new ServerErrorException(e.getMessage(),
						Response.Status.INTERNAL_SERVER_ERROR);
			}
		}

		return book;
	}

	// (9) PUT actualizar/editar una reseña con reviewid de un libro con bookid
	@PUT
	@Path("/{bookid}/reviews/{reviewid}")
	@Consumes(MediaType.BOOKS_API_REVIEW)
	@Produces(MediaType.BOOKS_API_REVIEW)
	public Review updateReview(@PathParam("bookid") int bookid,
			@PathParam("reviewid") int reviewid, Review review) {
		Book book = null;
		Review review2 = null; // para que devuelva la nueva review

		// En prime lugar comprobar que el usuario sea registrado
		// y que la reseña a editar sea la suya (Alicia solo puede editar la
		// reseña de Alicia)
		if (!security.isUserInRole("registered")) {
			throw new ForbiddenException("You have not registered");
		}

		// ahora el usuario
		//validateUser(reviewid);

		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			String sql = buildUpdateReview();
			stmt = conn.prepareStatement(sql);
			//stmt.setInt(1, review.getBookid());
			//stmt.setDate(2, (Date) review.getFecha());
			stmt.setString(1, review.getReview_text());
			stmt.setInt(2, review.getReviewid());
			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
				throw new ServerErrorException(e.getMessage(),
						Response.Status.INTERNAL_SERVER_ERROR);
			}
		}

		return review2;
	}

	// (10) DELETE - eliminar una reseña con reviewid de un libro con bookid
	@DELETE
	@Path("/{bookid}/reviews/{reviewid}")
	public void deleteReview(@PathParam("bookid") int bookid,
			@PathParam("reviewid") int reviewid) {

		// reseña de Alicia)
		if (!security.isUserInRole("registered")) {
			throw new ForbiddenException("You have not registered");
		}

		// ahora el usuario
		//validateUser(reviewid);

		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			// llamamos a la función para la query y la hacemos la database
			String sql = buildDeleteReview();
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, reviewid);

			int rows = stmt.executeUpdate();

			if (rows == 0) {
				throw new NotFoundException("There's no review with review="
						+ reviewid + "with the book with bookid=" + bookid);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
				throw new ServerErrorException(e.getMessage(),
						Response.Status.INTERNAL_SERVER_ERROR);
			}
		}
	}

	// *************************** Métodos adicionales / QUERY *********************
	// (1)GET colección de libros
	private String buildQueryGetBooksCollection() {
		return "select * from books";
		// return "select * from books b, review r where r.bookid = b.bookid";
	}

	// 1.2. Obetener las reviews de un libro con bookid
	private Book getBookAndReviewsFromBookid(int bookid)
	{
		Connection conn = null;
		Book book = new Book();
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			String sql = buildQueryGetBookByBookid();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, Integer.valueOf(bookid));

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				book.setBookid(rs.getInt("bookid"));
				book.setAutor(rs.getString("autor"));
				book.setTitulo(rs.getString("titulo"));
				book.setEdicion(rs.getString("edicion"));
				book.setLengua(rs.getString("lengua"));
				book.setEditorial(rs.getString("editorial"));
				book.setFechae(sdf.format(rs.getDate("fechae")));
				book.setFechai(sdf.format(rs.getDate("fechai")));
			} else
				throw new NotFoundException();

			sql = "select * from review where bookid=?";
			stmt.close();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, Integer.valueOf(bookid));
			rs = stmt.executeQuery();
			while (rs.next()) {
				Review review = new Review();
				review.setBookid(rs.getInt("bookid"));
				review.setReview_text(rs.getString("review_text"));
				review.setFecha(rs.getDate("fecha"));
				review.setReviewid(rs.getInt("reviewid"));
				review.setUsername(rs.getString("username"));

				book.addReview(review);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// Haya ido bien o haya ido mal cierra las conexiones
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
				throw new ServerErrorException(e.getMessage(),
						Response.Status.INTERNAL_SERVER_ERROR);
			}
		}
		return book;
	}
	
	// (2)GET de un libro con identificador bookid
	private String buildQueryGetBookByBookid() {
		return "select * from books where bookid=?";
	}

	// (3)GET con pattern de title y author
	private String buildGetStingsQueryByTitleAuthor(String title, String autor) {
		String query = null;
		if ((title != null) && (autor == null)) {
			query = "select * from books where titulo like ?";
		} else if ((title == null) && (autor != null)) {
			query = "select * from books where autor like ?";
		} else if ((title == null) && (autor == null)) {
			query = null;
		}
		return query;

	}

	// (5)POST, crear una ficha de un libro: varios métodos:
	// 5.1. comprobamos que no hayan párametros vacíos
	private void validateBook(Book book) {
		if (book.getAutor() == null)
			throw new BadRequestException("Subject can't be null.");
		if (book.getTitulo() == null)
			throw new BadRequestException("Content can't be null.");
		/*
		 * if (book.getSubject().length() > 100) throw new BadRequestException(
		 * "Subject can't be greater than 100 characters."); if
		 * (book.getContent().length() > 500) throw new BadRequestException(
		 * "Content can't be greater than 500 characters.");
		 */
	}

	// 5.2. Método para query de Insert la ficha del libro en la BBDD
	private String buildQueryInsertBook() {
		return "insert into books (titulo, autor, lengua, edicion, editorial, fechae, fechai) value (?, ?, ?, ?, ?, ?, ?)";
	}

	// 5.3. Método para obtener libro con bookid
	private Book getBookFromDatabase(int bookid) {
		Connection conn = null;
		Book book = new Book();
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			String sql = buildQueryGetBookByBookid();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, Integer.valueOf(bookid));

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				book.setBookid(rs.getInt("bookid"));
				book.setAutor(rs.getString("autor"));
				book.setTitulo(rs.getString("titulo"));
				book.setEdicion(rs.getString("edicion"));
				book.setLengua(rs.getString("lengua"));
				book.setEditorial(rs.getString("editorial"));
				book.setFechae(sdf.format(rs.getDate("fechae")));
				book.setFechai(sdf.format(rs.getDate("fechai")));
			} else
				throw new NotFoundException();

			sql = "select * from review where bookid=?";
			stmt.close();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, Integer.valueOf(bookid));
			rs = stmt.executeQuery();
			while (rs.next()) {
				Review review = new Review();
				review.setBookid(rs.getInt("bookid"));
				review.setReview_text(rs.getString("review_text"));
				review.setFecha(rs.getDate("fecha"));
				review.setReviewid(rs.getInt("reviewid"));
				review.setUsername(rs.getString("username"));

				book.addReview(review);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// Haya ido bien o haya ido mal cierra las conexiones
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
				throw new ServerErrorException(e.getMessage(),
						Response.Status.INTERNAL_SERVER_ERROR);
			}
		}
		return book;
	}

	// (6)PUT hacer una actualización de la ficha del libro:
	private String buildUpdateBook() {
		return "update books set titulo=ifnull(?, titulo), autor=ifnull(?, autor), lengua=ifnull(?, lengua), edicion=ifnull(?, edicion), editorial=ifnull(?, editorial), fechae=ifnull(?, fechae), fechai=ifnull(?, "
				+ "fechai) where bookid=?";
	}

	// (7) DELETE - eliminar una ficha de un libro a partir del bookid
	private String buildDeleteBook() {
		return "delete from books where bookid=?";
	}

	// (8) POST Crear una reseña de un libro con bookid

	// 8.0. Añadir la reseña a la base de datos
	private String buildCreateReview() {
		return "insert into review (bookid, username, review_text) value (?, ?, ?)";
	}

	// 8.1. Obtener review a partir del reviewid
	private Review getReviewFromDatbase(int reviewid) {
		Connection conn = null;
		Book book = new Book();
		Review review = new Review();
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			String sql = buildQueryGetReviewByReviewid();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, reviewid);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				review.setBookid(rs.getInt("bookid"));
				review.setUsername(rs.getString("username"));
				review.setReviewid(rs.getInt("reviewid"));
				review.setFecha(rs.getDate("fecha"));
				// FALTA AÑADIR PARA EL TEXTO DE LA RESEÑA
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// Haya ido bien o haya ido mal cierra las conexiones
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
				throw new ServerErrorException(e.getMessage(),
						Response.Status.INTERNAL_SERVER_ERROR);
			}
		}
		return review;

	}

	// 8.2. Hacer query
	private String buildQueryGetReviewByReviewid() {
		return "select*from reviews where reviewid=?";
	}

	// 8.3. Asegurar que el usuario no haga otra reseña sobre el libro:
	private void validateUserReview(int bookid) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}

		PreparedStatement stmt = null;
		try {
			// buscamos los usuarios que han hecho una reseña en el libro con
			// bookid
			String sql = getReviewsFromDatabaseByBookid(bookid);
			// String sql = "select * from review where username = '" +
			// security.getUserPrincipal().getName() +" and bookid = "+bookid;
			stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				// si el usuario que está realizando la consulta (en este caso
				// crear una review
				// de un libro es igual uno de los usuario con username que YA
				// HA HECHO una
				// una reseña del libro, hace un throw new ForbiddenException

				if (security.getUserPrincipal().getName()
						.equals(rs.getString("username"))) {
					throw new ClientErrorException(
							"You already have written a review for this book",
							409);

				}

				// query directa, si recibe respuesta al select * from ... con
				// el usuario
				// significa que el usuario la reseña del libro, si es nulo
				// puede hacer la reseña
				/*
				 * throw new ClientErrorException(
				 * "You already have written a review for this book", 409);
				 */
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// Haya ido bien o haya ido mal cierra las conexiones
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
				throw new ServerErrorException(e.getMessage(),
						Response.Status.INTERNAL_SERVER_ERROR);
			}
		}

	}

	private String getReviewsFromDatabaseByBookid(int bookid) {
		return "select username from review where review.bookid = " + bookid;
	}

	// (9) Actulizar reseña
	// 9.1. Validación del usuario (alicia sólo editar/eliminar reseña de
	// Alicia)
	private void validateUser(int reviewid) {
		// si el usuario que consulta la reseña no es el que la ha creado,
		// ForbiddenException
		Review currentReview = getReviewFromDatbase(reviewid);
		if (!security.getUserPrincipal().getName()
				.equals(currentReview.getUsername()))
			throw new ForbiddenException(
					"You are not allowed to modify/create/delete this review.");
	}

	// 9.2. Query para hacer edición/actualizar reseña
	private String buildUpdateReview() {
		//return "update review set bookid=ifnull(?, bookid), review_text=ifnull(?, review_text) where reviewid=?";
		return "update review set review_text=? where reviewid=? ";
	}

	// (10) Eliminar una reseña de un libro
	// 10.1. Query a la base de datos:
	private String buildDeleteReview() {
		return "delete from review where reviewid=?";
	}

	public boolean isAdministrator() {
		return administrator;
	}

	public void setAdministrator(boolean administrator) {
		this.administrator = administrator;
	}
}
