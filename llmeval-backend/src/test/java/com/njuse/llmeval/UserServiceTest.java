package com.njuse.llmeval;

import com.njuse.llmeval.exception.LLMEvalException;
import com.njuse.llmeval.po.User;
import com.njuse.llmeval.repository.UserRepository;
import com.njuse.llmeval.service.impl.UserServiceImpl;
import com.njuse.llmeval.util.TokenUtil;
import com.njuse.llmeval.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Spy
    private TokenUtil tokenUtil = new TokenUtil();

    @Mock
    private UserRepository userRepo;

    @BeforeEach
    public void setup() {
        // @Spy 的依赖需要手动注入
        ReflectionTestUtils.setField(tokenUtil, "userRepository", userRepo);
    }

    @Test
    public void registerWithDuplicatePhone() {
        UserVO userVO = new UserVO();
        userVO.setPhone("12345678900");
        when(userRepo.existsByPhone("12345678900")).thenReturn(true);
        LLMEvalException exception = assertThrows(
                LLMEvalException.class,
                () -> userService.register(userVO)
        );
        assertEquals("手机号已存在！", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    public void normalRegister() {
        String phone = "12345678900";
        String password = "123";
        String username = "abc";

        UserVO userVO = new UserVO();
        userVO.setPhone(phone);
        userVO.setPassword(password);
        userVO.setUsername(username);
        when(userRepo.existsByPhone(phone)).thenReturn(false);
        when(userRepo.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User arg = invocation.getArgument(0);
                    if (arg.getId() == null)
                        arg.setId(1);
                    return arg;
                });

        UserVO result = assertDoesNotThrow(() -> userService.register(userVO));
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(phone, result.getPhone());
        assertEquals(password, result.getPassword());
        assertEquals(username, result.getUsername());
    }

    @Test
    public void loginWithoutRegister() {
        String phone = "12345678900";
        String password = "123";
        when(userRepo.findByPhone(phone)).thenReturn(null);
        LLMEvalException exception = assertThrows(
                LLMEvalException.class,
                () -> userService.login(phone, password)
        );
        assertEquals("手机号或密码错误！", exception.getMessage());
    }

    @Test
    public void loginWithWrongPassword() {
        String phone = "12345678900";
        String password = "123";
        when(userRepo.findByPhone(any(String.class)))
                .thenAnswer(invocation -> {
                    String arg = invocation.getArgument(0);
                    User user = new User();
                    user.setId(1);
                    user.setPassword("1234");
                    user.setPhone(arg);
                    user.setUsername("abc");
                    return user;
                });
        LLMEvalException exception = assertThrows(
                LLMEvalException.class,
                () -> userService.login(phone, password)
        );
        assertEquals("手机号或密码错误！", exception.getMessage());
    }

    @Test
    public void normalLogin() {
        String phone = "12345678900";
        String password = "123";
        when(userRepo.findByPhone(any(String.class)))
                .thenAnswer(invocation -> {
                    String arg = invocation.getArgument(0);
                    User user = new User();
                    user.setId(1);
                    user.setPassword("123");
                    user.setPhone(arg);
                    user.setUsername("abc");
                    return user;
                });
        String token = assertDoesNotThrow(() -> userService.login(phone, password));
        assertNotNull(token);

        User expected = new User();
        expected.setId(1);
        expected.setPassword(password);
        expected.setUsername("abc");
        expected.setPhone(phone);
        assertEquals(tokenUtil.generateToken(expected), token);
    }

    @Test
    public void normalGetCurrentUser() {
        User user = new User();
        user.setId(1);
        user.setPhone("12345678900");
        user.setUsername("abc");
        user.setPassword("123");
        String token = tokenUtil.generateToken(user);
        when(userRepo.findByPhone(any(String.class)))
                .thenAnswer(invocation -> {
                    String arg = invocation.getArgument(0);
                    User trueUser = new User();
                    trueUser.setId(1);
                    trueUser.setUsername("abc");
                    trueUser.setPhone(arg);
                    trueUser.setPassword("123");
                    return trueUser;
                });

        UserVO result = assertDoesNotThrow(() -> userService.getCurrentUser(token));
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getPhone(), result.getPhone());
        assertEquals(user.getPassword(), result.getPassword());
        assertEquals(user.getUsername(), result.getUsername());
    }
}
