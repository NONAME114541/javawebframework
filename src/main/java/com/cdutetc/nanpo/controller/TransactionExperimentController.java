package com.cdutetc.nanpo.controller;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.dao.UserRepository;
import com.cdutetc.nanpo.dao.UserRoleRepository;
import com.cdutetc.nanpo.dto.RegisterRequest;
import com.cdutetc.nanpo.service.TransactionExperimentService;

@Profile("exp08")
@RestController
@RequestMapping("/tx-demo")
public class TransactionExperimentController {
    private final TransactionExperimentService experiment;
    private final UserRepository users;
    private final UserRoleRepository roles;

    public TransactionExperimentController(TransactionExperimentService experiment,
            UserRepository users, UserRoleRepository roles) {
        this.experiment = experiment; this.users = users; this.roles = roles;
    }

    @PostMapping("/duplicate-role")
    public Result<Void> duplicate(@RequestBody RegisterRequest request) {
        experiment.duplicateRole(request);
        return Result.success();
    }

    @PostMapping("/outer-rollback")
    public Result<Void> propagation(@RequestBody RegisterRequest request,
            @RequestParam Long existingUserId, @RequestParam boolean requiresNew) {
        experiment.outerRollback(request, existingUserId, requiresNew);
        return Result.success();
    }

    @GetMapping("/state/{username}")
    public Result<Map<String, Object>> state(@PathVariable String username) {
        return Result.success(Map.of("username", username,
                "users", users.findByUsername(username).isPresent() ? 1 : 0,
                "roles", roles.countByUserUsername(username),
                "USER", roles.countByUserUsernameAndRoleName(username, "USER"),
                "REQUIRED_DEMO", roles.countByUserUsernameAndRoleName(username, "REQUIRED_DEMO"),
                "NEW_DEMO", roles.countByUserUsernameAndRoleName(username, "NEW_DEMO")));
    }
}
