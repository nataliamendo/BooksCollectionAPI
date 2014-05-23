package edu.upc.eetac.dsa.nmendo.books.api;

import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class BooksApplication extends ResourceConfig{

	public BooksApplication() {
		super();
		// TODO Auto-generated constructor stub
		register(DeclarativeLinkingFeature.class);
		
	}

}
