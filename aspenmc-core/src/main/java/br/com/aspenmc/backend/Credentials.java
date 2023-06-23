package br.com.aspenmc.backend;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Credentials {

	private String hostName;

	private String userName;
	private String passWord;

	private String database;
	private int port;

}
