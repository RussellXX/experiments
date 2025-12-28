package com.njuse.llmeval;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.njuse.llmeval.po.User;
import com.njuse.llmeval.repository.UserRepository;
import com.njuse.llmeval.util.TokenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenUtilTest {
    @InjectMocks
    private TokenUtil tokenUtil;

    @Mock
    private UserRepository userRepo;

    @Test
    public void normalGenerateToken() {
        User user = new User();
        user.setId(1);
        user.setUsername("abc");
        user.setPhone("12345678900");
        user.setPassword("123");
        String token = assertDoesNotThrow(() -> tokenUtil.generateToken(user));
        assertNotNull(token);
    }

    @Test
    public void normalDecodeToken() {
        User user = new User();
        user.setId(1);
        user.setUsername("abc");
        user.setPhone("12345678900");
        user.setPassword("123");
        String token = tokenUtil.generateToken(user);

        when(userRepo.findByPhone("12345678900"))
                .thenAnswer(invocation -> {
                    User newUser = new User();
                    newUser.setId(1);
                    newUser.setUsername("abc");
                    newUser.setPhone("12345678900");
                    newUser.setPassword("123");
                    return newUser;
                });
        User result = assertDoesNotThrow(() -> tokenUtil.decodeToken(token));
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getPhone(), result.getPhone());
        assertEquals(user.getPassword(), result.getPassword());
    }

    @Test
    public void tryDecodeWrongToken() {
        String token = "ijfe12jo4190j09m90100401j009mk0098n00nn98m09k";
        assertThrows(
                JWTVerificationException.class,
                () -> tokenUtil.decodeToken(token)
        );
    }
}
