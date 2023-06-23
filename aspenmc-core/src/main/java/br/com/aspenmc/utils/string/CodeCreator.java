package br.com.aspenmc.utils.string;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import br.com.aspenmc.CommonConst;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class CodeCreator {

	public static final CodeCreator DEFAULT_CREATOR = new CodeCreator(12).setSpecialCharacters(false);
	public static final CodeCreator DEFAULT_CREATOR_LETTERS_ONLY = new CodeCreator(12).setNumbers(false)
			.setSpecialCharacters(false).setUpperCase(false);
	public static final CodeCreator DEFAULT_CREATOR_SPECIAL = new CodeCreator(12);

	private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
	private static final String NUMBERS = "123456789";
	private static final String SPECIAL_CHARACTERS = "@_-()*&%$#!";

	private final int characters;

	private boolean upperCase = true;
	private boolean numbers = true;
	private boolean specialCharacters = true;

	public CodeCreator setUpperCase(boolean upperCase) {
		this.upperCase = upperCase;
		return this;
	}

	public CodeCreator setNumbers(boolean numbers) {
		this.numbers = numbers;
		return this;
	}

	public CodeCreator setSpecialCharacters(boolean specialCharacters) {
		this.specialCharacters = specialCharacters;
		return this;
	}

	public String random() {
		return random(characters);
	}

	public String random(int characters) {
		String avaiableCharacters = LETTERS;

		if (upperCase)
			avaiableCharacters += LETTERS.toUpperCase();

		if (numbers)
			avaiableCharacters += NUMBERS;

		if (specialCharacters)
			avaiableCharacters += SPECIAL_CHARACTERS;

		char[] chars = avaiableCharacters.toCharArray();
		StringBuilder code = new StringBuilder();

		for (int x = 1; x <= characters; x++) {
			code.append(chars[CommonConst.RANDOM.nextInt(chars.length)]);
		}

		return code.toString();
	}

}
