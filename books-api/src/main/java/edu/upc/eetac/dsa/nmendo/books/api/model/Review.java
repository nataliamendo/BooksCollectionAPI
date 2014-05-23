package edu.upc.eetac.dsa.nmendo.books.api.model;

import java.util.Date;

public class Review {
	private int bookid;
	private int reviewid;
	private String username;
	private Date fecha = new Date();//fecha de la review
	private String review_text;

	public int getBookid() {
		return bookid;
	}
	public void setBookid(int libroid) {
		this.bookid = libroid;
	}
	public int getReviewid() {
		return reviewid;
	}
	public void setReviewid(int reviewid) {
		this.reviewid = reviewid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public String getReview_text() {
		return review_text;
	}
	public void setReview_text(String review_text) {
		this.review_text = review_text;
	}
	
	

}
