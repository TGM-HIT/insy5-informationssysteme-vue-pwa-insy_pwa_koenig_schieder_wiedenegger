package tgm.ac.at.gk911_informationssysteme_rest_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            UserDetails user = userDetailsService.loadUserByUsername(username);

            if (passwordEncoder.matches(password, user.getPassword())) {
                String role = user.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

                Map<String, Object> response = new HashMap<>();
                response.put("username", username);
                response.put("role", role);
                response.put("token", "simple-token-" + username);

                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            // User nicht gefunden
        }

        return ResponseEntity.status(401).body(Map.of("message", "Ungültige Anmeldedaten"));
    }
}