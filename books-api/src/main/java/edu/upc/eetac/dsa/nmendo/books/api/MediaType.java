package edu.upc.eetac.dsa.nmendo.books.api;


//Define los tipos de 'media type' que utilizaremos
//vnd-> quien es dueño, a quien le pertenece
public interface MediaType {
	public final static String BOOKS_API_BOOK_COLLECTION = "application/vnd.beeter.api.book.collection+json";
	public final static String BOOKS_API_BOOK = "application/vnd.beeter.api.book+json";
	public final static String BOOKS_API_REVIEW = "application/vnd.beeter.api.review+json";
	
	//nueva media type para error:
	public final static String BOOKS_API_ERROR = "application/vnd.dsa.beeter.error+json";
}