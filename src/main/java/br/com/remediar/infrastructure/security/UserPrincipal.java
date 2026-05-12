package br.com.remediar.infrastructure.security;

import br.com.remediar.domain.enums.UserRole;
import br.com.remediar.domain.model.AppUser;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final UserRole role;
    private final String actorDocument;
    private final boolean enabled;

    public UserPrincipal(Long id, String username, String password, UserRole role, String actorDocument, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.actorDocument = actorDocument;
        this.enabled = enabled;
    }

    public static UserPrincipal from(AppUser user) {
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole(),
                user.getActorDocument(),
                user.isVerified()
        );
    }

    public Long id() {
        return id;
    }

    public UserRole role() {
        return role;
    }

    public String actorDocument() {
        return actorDocument;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
