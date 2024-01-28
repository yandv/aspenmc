package br.com.aspenmc.entity.sender.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Skin {

	private String playerName;
	private UUID uniqueId;

	private String value;
	private String signature;

	private long createdAt;

	public Skin(String playerName, UUID uniqueId, String value, String signature) {
		this(playerName, uniqueId, value, signature, System.currentTimeMillis());
	}

	public Skin(String playerName, String value, String signature) {
		this(playerName, null, value, signature, System.currentTimeMillis());
	}

	public Skin(String playerName, UUID uniqueId, String value) {
		this(playerName, uniqueId, value, "", System.currentTimeMillis());
	}

}
