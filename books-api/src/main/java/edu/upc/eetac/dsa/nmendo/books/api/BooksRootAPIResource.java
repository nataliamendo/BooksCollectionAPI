package edu.upc.eetac.dsa.nmendo.books.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import edu.upc.eetac.dsa.nmendo.books.api.model.BooksRootAPI;

@Path("/")
// Esta indicada como /-> es la raíz.
public class BooksRootAPIResource {
	private boolean administrator;
	@Context
	SecurityContext security;

	@GET
	public BooksRootAPI getRootAPI() {
		System.out.println(security.getUserPrincipal().getName());
		setAdministrator(security.isUserInRole("admin"));
		BooksRootAPI api = new BooksRootAPI();
		return api;
	}

	public boolean isAdministrator() {
		return administrator;
	}

	public void setAdministrator(boolean administrator) {
		this.administrator = administrator;
	}
}