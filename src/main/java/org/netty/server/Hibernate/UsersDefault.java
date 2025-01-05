package org.netty.server.Hibernate;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class UsersDefault {


    public UsersDefault(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public UsersDefault() {
    }

    public UsersDefault(String login, String password, String role, Timestamp last_online) {
        this.login = login;
        this.password = password;
        this.role = role;
        this.last_online = last_online;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column
    private String login;

    @Column
    private String password;

    @Column
    private String role;

    @Column
    private Timestamp last_online;

    @ElementCollection
    private List<RefreshTokens> refreshTokens = new ArrayList<>();

    public List<RefreshTokens> getRefreshTokens() {
        return refreshTokens;
    }

    public void setRefreshTokens(List<RefreshTokens> refreshTokens) {
        this.refreshTokens = refreshTokens;
    }

    public RefreshTokens getRefreshTokenByMacAddress(String MacAddress) {
        for (RefreshTokens r : refreshTokens) {
            if (r.getMac_address().equals(MacAddress)) {
                return r;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullData() {
        return id + "/" + login + "/" + password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getLast_online() {
        return last_online;
    }

    public void setLast_online(Timestamp last_online) {
        this.last_online = last_online;
    }
}
