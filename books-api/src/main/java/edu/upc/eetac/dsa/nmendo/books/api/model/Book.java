package edu.upc.eetac.dsa.nmendo.books.api.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Link;

import org.eclipse.persistence.internal.queries.ArrayListContainerPolicy;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.InjectLinks;

import edu.upc.eetac.dsa.nmendo.books.api.BookResource;
import edu.upc.eetac.dsa.nmendo.books.api.BooksRootAPIResource;
import edu.upc.eetac.dsa.nmendo.books.api.MediaType;

public class Book {
	@InjectLinks({
		@InjectLink(resource = BookResource.class, style = Style.ABSOLUTE, rel = "BookResource", title = "Books collection", type = MediaType.BOOKS_API_BOOK_COLLECTION, method="getBooks"),
		@InjectLink(resource = BookResource.class, style = Style.ABSOLUTE, rel = "self edit", title = "Book", type = MediaType.BOOKS_API_BOOK, method = "getBookid", bindings = @Binding(name = "bookid", value = "${instance.bookid}")),
		@InjectLink(resource = BookResource.class, style = Style.ABSOLUTE, rel = "createBook", condition = "${resource.administrator}", title = "Create book", type = MediaType.BOOKS_API_BOOK, method = "createBook", bindings = @Binding(name = "bookid", value = "${instance.bookid}")),
		@InjectLink(resource = BookResource.class, style = Style.ABSOLUTE, rel = "updateBook", condition = "${resource.administrator}", title = "Update book", type = MediaType.BOOKS_API_BOOK, method = "updateBook", bindings = @Binding(name = "bookid", value = "${instance.bookid}")),
		@InjectLink(resource = BookResource.class, style = Style.ABSOLUTE, rel = "deleteBook", condition = "${resource.administrator}", title = "Delete book", type = MediaType.BOOKS_API_BOOK, method = "deleteBook", bindings = @Binding(name = "bookid", value = "${instance.bookid}"))})
	private List<Link> links;
	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}

	private int bookid;
	private String titulo;
	private String autor;
	private String lengua;
	private String edicion;
	private String editorial;
	// DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private String fechae;// fecha de edición
	private String fechai;// fecha de impresión
	private List<Review> reviews = new ArrayList<Review>();

	public int getBookid() {
		return bookid;
	}

	public void setBookid(int bookid) {
		this.bookid = bookid;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getLengua() {
		return lengua;
	}

	public void setLengua(String lengua) {
		this.lengua = lengua;
	}

	public String getEdicion() {
		return edicion;
	}

	public void setEdicion(String edicion) {
		this.edicion = edicion;
	}

	public String getEditorial() {
		return editorial;
	}

	public void setEditorial(String editorial) {
		this.editorial = editorial;
	}

	public String getFechae() {
		return fechae;
	}

	public void setFechae(String fechae) {
		this.fechae = fechae;
	}

	public String getFechai() {
		return fechai;
	}

	public void setFechai(String fechai) {
		this.fechai = fechai;
	}

	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	public void addReview(Review review) {
		reviews.add(review);
	}

}
