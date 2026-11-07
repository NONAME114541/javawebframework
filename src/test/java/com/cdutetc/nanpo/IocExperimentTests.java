package com.cdutetc.nanpo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.cdutetc.nanpo.common.JwtUtil;
import com.cdutetc.nanpo.dao.UserMapper;
import com.cdutetc.nanpo.dao.UserRepository;
import com.cdutetc.nanpo.entity.User;
import com.cdutetc.nanpo.service.RegistrationService;
import com.cdutetc.nanpo.service.UserService;
import com.cdutetc.nanpo.vo.UserVO;

@ExtendWith(OutputCaptureExtension.class)
class IocExperimentTests {

    private AnnotationConfigApplicationContext context(UserRepository repository) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(UserRepository.class, () -> repository);
        context.registerBean(JwtUtil.class, () -> mock(JwtUtil.class));
        context.registerBean(RegistrationService.class, () -> mock(RegistrationService.class));
        context.register(UserMapper.class, UserService.class);
        context.refresh();
        return context;
    }

    @Test
    void autowiredMapperQueriesRepositoryAndReturnsOnlyPublicFields() {
        UserRepository repository = mock(UserRepository.class);
        User user = new User();
        user.setId(7L);
        user.setUsername("exp07-demo");
        user.setNickname("IOC实验用户");
        user.setPassword("test-hash-must-not-be-returned");
        when(repository.findByUsername("exp07-demo")).thenReturn(Optional.of(user));
        when(repository.findByUsername("missing")).thenReturn(Optional.empty());
        try (AnnotationConfigApplicationContext context = context(repository)) {
            UserService service = context.getBean(UserService.class);
            assertThat(service.findByUsername("exp07-demo"))
                    .contains(new UserVO(7L, "exp07-demo", "IOC实验用户"));
            assertThat(service.findByUsername("missing")).isEmpty();
            verify(repository).findByUsername("exp07-demo");
            verify(repository).findByUsername("missing");
            assertThat(UserVO.class.getRecordComponents())
                    .extracting(component -> component.getName())
                    .containsExactly("id", "username", "nickname");
            System.out.println("[exp07-check] @Autowired query and safe UserVO: PASS");
        }
    }

    @Test
    void singletonIsSharedWithinAContextButNotAcrossContexts() {
        try (AnnotationConfigApplicationContext first = context(mock(UserRepository.class));
             AnnotationConfigApplicationContext second = context(mock(UserRepository.class))) {
            UserService a = first.getBean(UserService.class);
            UserService b = first.getBean(UserService.class);
            UserService c = first.getBean(UserService.class);
            assertThat(first.isSingleton("userService")).isTrue();
            assertThat(a).isSameAs(b).isSameAs(c);
            assertThat(second.getBean(UserService.class)).isNotSameAs(a);
            System.out.println("[exp07-check] same context a == b == c; different context differs: PASS");
        }
    }

    @Test
    void lifecycleCallbacksFollowConstructionInjectionInitializationAndClose(CapturedOutput output) {
        System.out.println("=== exp07 lifecycle start ===");
        try (AnnotationConfigApplicationContext context = context(mock(UserRepository.class))) {
            assertThat(context.getBean(UserService.class)).isNotNull();
            assertThat(output.getOut()).doesNotContain("UserService.PreDestroy");
        }
        String log = output.getOut();
        int constructor = log.indexOf("UserService.constructor");
        int injection = log.indexOf("UserService.Autowired mapperInjected=true");
        int initialized = log.indexOf("UserService.PostConstruct mapperReady=true");
        int destroyed = log.indexOf("UserService.PreDestroy");
        assertThat(constructor).isGreaterThanOrEqualTo(0);
        assertThat(injection).isGreaterThan(constructor);
        assertThat(initialized).isGreaterThan(injection);
        assertThat(destroyed).isGreaterThan(initialized);
        System.out.println("[exp07-check] constructor < @Autowired < @PostConstruct < close/@PreDestroy: PASS");
        System.out.println("=== exp07 lifecycle end ===");
    }
}
