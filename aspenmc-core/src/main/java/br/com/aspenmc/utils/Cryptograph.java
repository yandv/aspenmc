package br.com.aspenmc.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public enum Cryptograph {

    BASE64 {
        public String encode(String string) {
            return new String(Base64.getEncoder().encode(string.getBytes()), StandardCharsets.UTF_8);
        }

        public String decode(String string) {
            return new String(Base64.getDecoder().decode(string.getBytes()), StandardCharsets.UTF_8);
        }
    };

    public String encode(String string) {
        return "";
    }

    public String decode(String string) {
        return "";
    }
}
