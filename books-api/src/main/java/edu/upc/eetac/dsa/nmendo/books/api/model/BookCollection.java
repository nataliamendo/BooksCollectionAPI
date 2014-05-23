package edu.upc.eetac.dsa.nmendo.books.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.InjectLinks;

import edu.upc.eetac.dsa.nmendo.books.api.BookResource;
import edu.upc.eetac.dsa.nmendo.books.api.MediaType;

public class BookCollection {
	@InjectLinks({
		//@InjectLink(resource = BookResource.class, style = Style.ABSOLUTE, rel = "BookResource", title = "Books collection", type = MediaType.BOOKS_API_BOOK_COLLECTION),
		//@InjectLink(resource = BookResource.class, style = Style.ABSOLUTE, rel = "createBook", condition = "${resource.administrator}", title = "Create book", type = MediaType.BOOKS_API_BOOK) ,
		@InjectLink(resource = BookResource.class, style = Style.ABSOLUTE, rel = "getBookid()",title = "bookid", type = MediaType.BOOKS_API_BOOK, bindings = @Binding(name = "bookid", value = "${instance.id}"))})
	private List<Link> links;
	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}

	private List<Book> books;

	public BookCollection() {
		super();
		books = new ArrayList<Book>();
	}

	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}

	public void addBook(Book book) {
		books.add(book);
	}

}
