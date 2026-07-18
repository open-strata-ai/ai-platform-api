package cc.openstrata.platform.web;

import cc.openstrata.platform.application.UserAppService;
import cc.openstrata.platform.application.dto.ChangeRoleRequest;
import cc.openstrata.platform.application.dto.InviteUserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/users")
public class UserController {
    private final UserAppService userAppService;

    public UserController(UserAppService userAppService) {
        this.userAppService = userAppService;
    }

    @PostMapping
    public ResponseEntity<Void> invite(@PathVariable String tenantId, @RequestBody InviteUserRequest req) {
        userAppService.inviteUser(tenantId, req);
        return ResponseEntity.status(201).build();
    }

    @PatchMapping("/{userId}:role")
    public ResponseEntity<Void> changeRole(@PathVariable String tenantId, @PathVariable String userId,
                                           @RequestBody ChangeRoleRequest req) {
        userAppService.changeRole(tenantId, userId, req);
        return ResponseEntity.ok().build();
    }
}
