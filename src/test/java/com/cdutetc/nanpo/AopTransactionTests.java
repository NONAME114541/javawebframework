package com.cdutetc.nanpo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.cdutetc.nanpo.controller.HelloController;
import com.cdutetc.nanpo.controller.TransactionExperimentController;
import com.cdutetc.nanpo.controller.UserController;
import com.cdutetc.nanpo.dao.UserRepository;
import com.cdutetc.nanpo.dao.UserRoleRepository;
import com.cdutetc.nanpo.dto.LoginRequest;
import com.cdutetc.nanpo.dto.RegisterRequest;
import com.cdutetc.nanpo.entity.User;
import com.cdutetc.nanpo.service.RegistrationService;
import com.cdutetc.nanpo.service.TransactionExperimentService;

/** 测试方法不加 @Transactional：每次断言读取真实业务事务结束后的数据。 */
@SpringBootTest
@ActiveProfiles("exp08")
@ExtendWith(OutputCaptureExtension.class)
class AopTransactionTests {
    @Autowired RegistrationService registration;
    @Autowired TransactionExperimentService experiment;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository roles;
    @Autowired UserController userController;
    @Autowired HelloController helloController;
    @Autowired TransactionExperimentController experimentController;

    private RegisterRequest request(String username) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username); request.setPassword("Exp08TestSecret123");
        request.setNickname("事务实验用户"); return request;
    }

    @Test
    void registrationCommitsUserAndDefaultRole() {
        registration.register(request("test08-success"));
        assertThat(users.findByUsername("test08-success")).isPresent();
        assertThat(roles.countByUserUsernameAndRoleName("test08-success", "USER")).isEqualTo(1);
        System.out.println("[exp08-check] registration COMMIT: user=1 role=1 PASS");
    }

    @Test
    void databaseConstraintViolationRollsBackBothTables() {
        long usersBefore = users.count(), rolesBefore = roles.count();
        assertThatThrownBy(() -> experiment.duplicateRole(request("test08-duplicate")))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(users.findByUsername("test08-duplicate")).isEmpty();
        assertThat(roles.countByUserUsername("test08-duplicate")).isZero();
        assertThat(users.count()).isEqualTo(usersBefore);
        assertThat(roles.count()).isEqualTo(rolesBefore);
        System.out.println("[exp08-check] duplicate UNIQUE violation: user=0 role=0; both table counts unchanged PASS");
    }

    @Test
    void requiredJoinsOuterTransactionAndRollsBackWithIt() {
        User owner = registration.register(request("test08-owner-required"));
        assertThatThrownBy(() -> experiment.outerRollback(request("test08-outer-required"), owner.getId(), false))
                .isInstanceOf(IllegalStateException.class);
        assertThat(users.findByUsername("test08-outer-required")).isEmpty();
        assertThat(roles.countByUserUsername("test08-outer-required")).isZero();
        assertThat(roles.countByUserUsernameAndRoleName(owner.getUsername(), "REQUIRED_DEMO")).isZero();
        System.out.println("[exp08-check] REQUIRED outer rollback: newUser=0 newDefaultRole=0 innerRole=0 PASS");
    }

    @Test
    void requiresNewCommitsEvenWhenOuterTransactionRollsBack() {
        // 预先提交的用户可被独立事务看见，避免外键指向外层未提交的数据。
        User owner = registration.register(request("test08-owner-new"));
        assertThatThrownBy(() -> experiment.outerRollback(request("test08-outer-new"), owner.getId(), true))
                .isInstanceOf(IllegalStateException.class);
        assertThat(users.findByUsername("test08-outer-new")).isEmpty();
        assertThat(roles.countByUserUsername("test08-outer-new")).isZero();
        assertThat(roles.countByUserUsernameAndRoleName(owner.getUsername(), "NEW_DEMO")).isEqualTo(1);
        System.out.println("[exp08-check] REQUIRES_NEW outer rollback: newUser=0 newDefaultRole=0 innerRole=1 PASS");
    }

    @Test
    void aroundAdviceLogsParametersResultTimeAndRedactsCredentials(CapturedOutput output) {
        String secret = "NeverLog08Password";
        RegisterRequest request = request("test08-aop"); request.setPassword(secret);
        userController.register(request);
        LoginRequest login = new LoginRequest(); login.setUsername(request.getUsername()); login.setPassword(secret);
        var loginResult = userController.login(login);
        helloController.hello();
        String text = output.getOut();
        assertThat(text).contains("[exp08-aop] start=", "args=", "test08-aop", "result=", "elapsedMs=", "[REDACTED]");
        assertThat(text).doesNotContain(secret, loginResult.getData().getAccessToken(), loginResult.getData().getRefreshToken());
        System.out.println("[exp08-check] @Around args/result/time + password/token redaction PASS");
    }

    @Test
    void aroundAdviceRethrowsDatabaseFailure(CapturedOutput output) {
        assertThatThrownBy(() -> experimentController.duplicate(request("test08-aop-failure")))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(output.getOut()).contains("exception=DataIntegrityViolationException", "elapsedMs=");
        assertThat(users.findByUsername("test08-aop-failure")).isEmpty();
        assertThat(roles.countByUserUsername("test08-aop-failure")).isZero();
        System.out.println("[exp08-check] @Around rethrows failure; transaction remains rolled back PASS");
    }
}
