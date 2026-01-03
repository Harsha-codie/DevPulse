package ai.cropplanner.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String language = body.getOrDefault("language", "en");
        User user = authService.signup(email, password, language);
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(Map.of(
            "token", token,
            "user", Map.of("email", user.getEmail(), "language", user.getLanguage())
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        User user = authService.login(email, password);
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(Map.of(
            "token", token,
            "user", Map.of("email", user.getEmail(), "language", user.getLanguage())
        ));
    }

    @PostMapping("/guest")
    public ResponseEntity<?> guest() {
        User guest = authService.createGuest();
        String token = jwtUtil.generateToken(guest);
        return ResponseEntity.ok(Map.of(
            "token", token,
            "user", Map.of("email", guest.getEmail(), "language", guest.getLanguage())
        ));
    }
}
