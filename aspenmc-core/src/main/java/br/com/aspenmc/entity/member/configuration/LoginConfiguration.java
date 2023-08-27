package br.com.aspenmc.entity.member.configuration;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.utils.Cryptograph;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


public class LoginConfiguration {

    @Getter
    private final AccountType accountType;
    private String passWord;
    @Getter
    private boolean captch;

    private final Map<String, Long> storageSessionMap;
    private final Map<String, Long> timeoutAddressMap;

    private final int encoder;

    @Getter
    private boolean logged;

    private transient Member member;

    public LoginConfiguration(AccountType accountType) {
        this.accountType = accountType;
        this.passWord = "";
        this.captch = accountType == AccountType.PREMIUM;
        this.encoder = CommonConst.RANDOM.nextInt(Cryptograph.values().length);
        this.storageSessionMap = new HashMap<>();
        this.timeoutAddressMap = new HashMap<>();
    }

    public boolean logIn(String passWord) {
        Cryptograph cryptograph = getCryptograph();

        if (isRegistered()) return false;

        if (cryptograph.decode(this.passWord).equals(passWord)) {
            this.logged = true;
            save("logged");
            startSession();
            return true;
        }

        return false;
    }

    public void logOut() {
        this.logged = false;
        save();
    }

    public void setCaptch(boolean captch) {
        this.captch = captch;
        save();
    }

    public boolean register(String passWord) {
        if (isRegistered()) return false;

        if (!isCaptch()) return false;

        this.passWord = getCryptograph().encode(passWord);
        this.logged = true;
        startSession();
        return true;
    }

    public void startSession() {
        String ipAddress = this.member.getIpAddress();

        this.storageSessionMap.put(ipAddress, System.currentTimeMillis() + (1000L * 60L * 60L * 24L * 7L));
        save("logged", "passWord", "storageSessionMap");
    }

    public LoginResult reloadSession() {
        if (accountType == AccountType.PREMIUM) {
            this.logged = true;
            save("logged");
            return LoginResult.PREMIUM;
        }

        if (this.member != null) {
            if (this.storageSessionMap.containsKey(this.member.getIpAddress())) {
                if (this.storageSessionMap.get(this.member.getIpAddress()) > System.currentTimeMillis()) {
                    this.logged = true;
                    save("logged");
                    return LoginResult.SESSION_RESTORED;
                }

                this.storageSessionMap.remove(this.member.getIpAddress());
                save("storageSessionMap");
                return LoginResult.SESSION_EXPIRED;
            }
        }

        this.logged = false;
        save("logged");
        return LoginResult.NOT_LOGGED;
    }

    public boolean isRegistered() {
        return !passWord.isEmpty();
    }

    public Cryptograph getCryptograph() {
        return Cryptograph.values()[encoder];
    }

    public void loadConfiguration(Member member) {
        this.member = member;
    }

    public void save(String... fields) {
        if (this.member != null) {
            this.member.save(
                    Stream.of(fields).map(fieldName -> "loginConfiguration." + fieldName).toArray(String[]::new));
        }
    }

    public void changePassword(String passWord) {
        this.passWord = getCryptograph().encode(passWord);
        this.logged = false;
        save("passWord", "logged");
    }

    public enum LoginResult {
        PREMIUM, SESSION_RESTORED, SESSION_EXPIRED, NOT_LOGGED;
    }

    public enum AccountType {
        CRACKED, PREMIUM;
    }
}
