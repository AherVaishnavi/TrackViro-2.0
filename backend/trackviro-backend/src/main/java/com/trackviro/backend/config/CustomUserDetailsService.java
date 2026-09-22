package com.trackviro.backend.config;

import com.trackviro.backend.model.User;
import com.trackviro.backend.repository.UserRepository;
import com.trackviro.backend.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * MODIFIED from com.example.demo.config.CustomUserDetailsService.
 *
 * The lookup itself is unchanged: findByEmailAndIsActiveTrue, so only
 * active users can authenticate, exactly as before.
 *
 * REMOVED entirely: the try/catch block that reached into
 * RequestContextHolder to grab the current HttpSession and write
 * "loggedUser"/"role" attributes into it. Per Step 5's explicit
 * instruction (no HttpSession), and because this project is a
 * stateless REST API, there is no session for it to write into in the
 * first place.
 *
 * CHANGED: returns this project's own CustomUserDetails instead of
 * the generic org.springframework.security.core.userdetails.User the
 * old app built via .withUsername(...).authorities(...). The old
 * generic User carried only email/password/authorities — nothing a
 * controller could use for the object-level authorization checks
 * built into the service layer in Step 4. CustomUserDetails carries
 * id and departmentId too, which is what makes those checks possible
 * without a session to fall back on.
 */
@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndIsActiveTrue(email);
        if (user == null) {
            throw new UsernameNotFoundException("No active user found with email: " + email);
        }
        return new CustomUserDetails(user);
    }
}
