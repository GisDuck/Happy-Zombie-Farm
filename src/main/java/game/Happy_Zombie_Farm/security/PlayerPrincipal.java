package game.Happy_Zombie_Farm.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class PlayerPrincipal implements UserDetails {

    private final Long playerId;

    public PlayerPrincipal(Long playerId) {
        this.playerId = playerId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // пока ролей нет
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return playerId.toString();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
