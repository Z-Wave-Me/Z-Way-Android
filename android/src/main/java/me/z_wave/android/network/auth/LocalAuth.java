package me.z_wave.android.network.auth;

/**
 * Created by oleg on 02.09.15.
 */
public class LocalAuth {
    private int default_ui;
    private String login;
    private String password;
    private Boolean keepme;
    private Boolean form;

    public LocalAuth () {};
    public LocalAuth (Boolean form,
                      String login, String password,
                      Boolean keepme, Integer default_ui)
    {
        this.default_ui = default_ui;
        this.login = login;
        this.password = password;
        this.keepme = keepme;
        this.form = form;
    };
}
