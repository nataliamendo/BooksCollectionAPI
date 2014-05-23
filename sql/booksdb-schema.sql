drop database if exists booksdb;
create database booksdb;

use booksdb;

create table users (
	username	varchar(20) not null primary key,
	userpass	char(32) not null,
	name		varchar(70) not null,
	email		varchar(255) not null
);

create table user_roles (
	username			varchar(20) not null,
	rolename 			varchar(20) not null,
	foreign key(username) references users(username) on delete cascade,
	primary key (username, rolename)
);

create table books (
	bookid			int not null auto_increment primary key,
	titulo			varchar(40) not null,
	autor			varchar(20) not null,
	lengua			varchar(20) not null,
	edicion			int not null,
	editorial		varchar(20) not null,
	fechae			date not null,
	fechai			date not null
);

create table review (
	reviewid		integer not null auto_increment unique,
	bookid			integer not null,
	username		varchar(20) not null,
	fecha			timestamp not null,
	review_text		text(200) not null,
	foreign key (username) references users (username),
	foreign key (bookid) references books(bookid) on delete cascade,
	primary key (username, bookid)
);

