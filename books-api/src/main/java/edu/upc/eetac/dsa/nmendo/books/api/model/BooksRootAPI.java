package edu.upc.eetac.dsa.nmendo.books.api.model;

import java.util.List;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.InjectLinks;

import edu.upc.eetac.dsa.nmendo.books.api.BooksRootAPIResource;
import edu.upc.eetac.dsa.nmendo.books.api.MediaType;
import edu.upc.eetac.dsa.nmendo.books.api.BookResource;

public class BooksRootAPI {
	@InjectLinks({
			@InjectLink(resource = BooksRootAPIResource.class, style = Style.ABSOLUTE, rel = "self bookmark home", title = "Beeter Root API", method = "getRootAPI"),
			@InjectLink(resource = BookResource.class, style = Style.ABSOLUTE, rel = "books", title = "Books collection", type = MediaType.BOOKS_API_BOOK_COLLECTION),
			@InjectLink(resource = BookResource.class, style = Style.ABSOLUTE, rel = "create-book", condition = "${resource.administrator}", title = "Create book", type = MediaType.BOOKS_API_BOOK) })
	private List<Link> links;

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}
}
